package com.example.demo.config;


import com.example.demo.config.jwt.JwtAuthenticationFilter;
import com.example.demo.config.security.oauth.CustomOAuth2UserService;
import com.example.demo.config.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.demo.config.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.demo.config.security.oauth.OAuthAuthenticationFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuthAuthenticationFailureHandler oAuthAuthenticationFailureHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider, HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository, CustomOAuth2UserService customOAuth2UserService, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, @Lazy OAuthAuthenticationFailureHandler oAuthAuthenticationFailureHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuthAuthenticationFailureHandler = oAuthAuthenticationFailureHandler;
    }

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository(){
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .cors()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/auth/**", "/auth/authenticate")
                .permitAll()
//                .requestMatchers("/auth/authenticate").hasRole("USER")
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .anonymous().disable()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth/authorize")
                .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/oauth/callback/**")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuthAuthenticationFailureHandler)
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }
}
