package com.example.demo.config.oauth;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class CustomOAuthUser implements OAuth2User {
    private String oauthClientName;
    private OAuth2User oAuth2User;

    @Override
    public <A> A getAttribute(String name) {
        return oAuth2User.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    public String getEmail(){
        return oAuth2User.<String>getAttribute("email");
    }

    public String getOauthClientName(){
        return this.oauthClientName;
    }

    @Override
    public String getName() {
        System.out.println(oAuth2User.<String>getAttribute("email"));
        return oAuth2User.getAttribute("name");
    }
}
