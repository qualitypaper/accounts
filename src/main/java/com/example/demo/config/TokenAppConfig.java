package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Data
@EnableAsync
@ConfigurationProperties(prefix = "app")
public class TokenAppConfig {
    private List<String> authorizedRedirectUris = new ArrayList<>();
    private String tokenSecret;
    private long tokenExpirationMsec;
}
