package com.example.demo.config.security.oauth.user;

import com.example.demo.model.AuthenticationType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;



public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuthUserInfo(String registrationId, Map<String, Object> attributes){
        if(registrationId.equalsIgnoreCase(AuthenticationType.GOOGLE.name())){
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Sorry! Login with " +  registrationId + " is not supported yet");
        }
    }
}
