package com.example.demo.config;

import com.example.demo.config.jwt.JwtAuthenticationFilter;
import com.example.demo.config.security.oauth.CustomOAuth2UserService;
import com.example.demo.config.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.demo.config.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.demo.config.security.oauth.OAuthAuthenticationFailureHandler;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AppConfig {

    private final com.example.demo.service.UserDetailsService userService;


    @Bean
    public UserDetailsService userDetailsService(){
        return userService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


}
