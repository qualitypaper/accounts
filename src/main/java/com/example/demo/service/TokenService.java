package com.example.demo.service;

import com.example.demo.model.ConfirmationToken;
import com.example.demo.model.ForgetPasswordToken;
import com.example.demo.repository.ConfirmationTokenRepository;
import com.example.demo.repository.ForgetPasswordTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class TokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final ForgetPasswordTokenRepository forgetPasswordTokenRepository;
    public void addConfirmationToken(ConfirmationToken confirmationToken){
        confirmationTokenRepository.save(confirmationToken);
    }
    public void addPasswordForgetToken(ForgetPasswordToken forgetPasswordToken){
        forgetPasswordTokenRepository.save(forgetPasswordToken);
    }
    public void setResetDate(String token){
        ForgetPasswordToken forgetPasswordToken = forgetPasswordTokenRepository.findByToken(token).get();
        forgetPasswordToken.setResetAt(LocalDateTime.now());
        forgetPasswordTokenRepository.save(forgetPasswordToken);
    }


    public String generateToken(){
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 15;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
