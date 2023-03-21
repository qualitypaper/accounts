package com.example.demo.config.security.oauth;

import com.example.demo.config.security.UserPrincipal;
import com.example.demo.config.security.oauth.user.OAuth2UserInfo;
import com.example.demo.config.security.oauth.user.OAuth2UserInfoFactory;
import com.example.demo.model.AuthenticationType;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuthUserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findUserByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getAuthType().name().equals(AuthenticationType.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()).toString().toUpperCase())) {
                throw new OAuth2AuthenticationException("Looks like you're signed up with " +
                        user.getAuthType().name() + " account. Please use your " + user.getAuthType().name() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest request, OAuth2UserInfo info){
        User user = new User();

        user.setEmail(info.getEmail());
        user.setImageUrl(info.getImageUrl());
        user.setFullName(info.getName());
        user.setAuthType(AuthenticationType.valueOf(request.getClientRegistration().getRegistrationId()));
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo auth2UserInfo){
        existingUser.setFullName(auth2UserInfo.getName());
        existingUser.setImageUrl(auth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
