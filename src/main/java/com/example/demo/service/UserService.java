package com.example.demo.service;

import com.example.demo.config.jwt.JwtResponse;
import com.example.demo.config.jwt.JwtService;
import com.example.demo.controller.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.ConfirmationTokenRepository;
import com.example.demo.repository.ForgetPasswordTokenRepository;
import com.example.demo.repository.UserAuthenticationRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final ForgetPasswordTokenRepository forgetPasswordTokenRepository;


    @Transactional
    public ResponseEntity<Map<String, String>> register(RegisterRequest signupRequest){
        User user = User.builder()
                .fullName(signupRequest.getFullName())
                .email(signupRequest.getEmail())
                .created(LocalDateTime.now())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .roles(Role.ROLE_USER)
                .authType(AuthenticationType.DATABASE)
                .build();
        userRepository.save(user);

        String token = tokenService.generateToken();
        confirmationTokenRepository.save(new ConfirmationToken(null, token, LocalDateTime.now(), null, LocalDateTime.now().plusMinutes(15), user));
        emailService.send(user.getEmail(), buildEmail(user.getFullName(), "activate your account", "Activate now", "localhost:8080/auth/confirm?token" + token), "Email Confirmation");
        Map<String, String> result = new HashMap<>();
        result.put("success", "true");
        result.put("token", token);
        return ResponseEntity.ok().body(result);
    }

    @Transactional
    public ResponseEntity<JwtResponse> authenticate(AuthenticationRequest loginRequest){
        Authentication authentication  = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        var user = userRepository.findUserByEmail(loginRequest.getEmail()).get();
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String jwtToken  = jwtService.generateToken(claims, user.getEmail());
        userAuthenticationRepository.save(new UserAuthentication(null, jwtToken, LocalDateTime.now(), user));

        return ResponseEntity.ok().body(new JwtResponse(jwtToken));
    }

    @Transactional
    public ResponseEntity<Map<String, String>> forgetPassword(ForgetRequest forgetRequest){
        User user = userRepository.findUserByEmail(forgetRequest.getEmail()).get();
        Map<String, String> result = new HashMap<>();

        if(user.getUsername() != null){
            String token = tokenService.generateToken();
            tokenService.addPasswordForgetToken(new ForgetPasswordToken(null, token, LocalDateTime.now(), null, user));
            emailService.send(user.getEmail(), buildEmail(user.getFullName(), "reset your password", "Reset now", "localhost:8080/auth/reset?token=" + token), "Password reset");

            result.put("success", "true");
            result.put("token", token);
            return ResponseEntity.ok().body(result);
        } else{
            result.put("success", "false");
            return ResponseEntity.ok().body(result);
        }
    }

    @Transactional
    public ResponseEntity<?> updateForgetPassword(ResetRequest request){
        ForgetPasswordToken forgetPasswordToken = forgetPasswordTokenRepository.findByToken(request.getToken()).get();
        Map<String, String> result = new HashMap<>();
        if(forgetPasswordToken.getToken() == null){
            result.put("success", "false");
            result.put("error", "exists");
            return ResponseEntity.ok().body(result);
        }
        var user = forgetPasswordToken.getUser();
        forgetPasswordToken.setResetAt(LocalDateTime.now());
        forgetPasswordTokenRepository.save(forgetPasswordToken);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        result.put("success", "true");
        return ResponseEntity.ok().body(result);
    }

    @Transactional
    public ResponseEntity<?> updatePassword(UpdatePasswordRequest request){
        var user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        HashMap<String, String> result = new HashMap<>();
        if(user.getPassword().equals(request.getOldPassword())){
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            result.put("success", "true");
            return ResponseEntity.ok().body(result);
        }
        result.put("success", "false");
        result.put("error", "match");
        return ResponseEntity.ok().body(result);
    }

    public ResponseEntity<HashMap<String, String>> checkUserSession(String token){
        HashMap<String, String> result = new HashMap<>();
        if(jwtService.getExpirationDateFromToken(token).before(new Date())){
            result.put("logged_in", "false");
            return ResponseEntity.ok().body(result);
        } else{
            result.put("logged_in", "true");
            return ResponseEntity.ok().body(result);
        }
    }

    @Transactional
    public ResponseEntity<?> setConfirmed(String token){
        var confirmationToken = confirmationTokenRepository.findByToken(token).get();
        Map<String, String> result = new HashMap<>();
        if(confirmationToken.getConfirmedAt() != null){
            result.put("success", "false");
            result.put("error" , "confirmed");
            return ResponseEntity.ok().body(result);
        }
        if(confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())){
            result.put("success", "false");
            result.put("error", "expired");
            return ResponseEntity.ok().body(result);
        }
        var user  = confirmationToken.getUser();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
        user.setConfirmed(true);
        userRepository.save(user);
        result.put("success", "true");
        return ResponseEntity.ok().body(result);
    }

    public void updateAuthenticationType(String username, String oauthClientName){
        AuthenticationType authenticationType = AuthenticationType.valueOf(oauthClientName.toUpperCase());
        userRepository.updateAuthenticationType(username, authenticationType);
    }
    public List<User> getAll(){
        return userRepository.findAll();
    }
    private String buildEmail(String name, String reason, String buttonText,  String link) {
        return "<html>" +
                "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to "+reason+": </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">"+ buttonText +"</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>" +
                "</html";
    }

}
