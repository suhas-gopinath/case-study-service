package com.example.casestudy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // @Value("${app.cors.allowed-mf1}")
    // private String allowedMf1;

    // @Value("${app.cors.allowed-mf2}")
    // private String allowedMf2;

    @Value("${app.cors.allowed-mf-container}")
    private String allowedMfContainer;

    @Value("${app.cors.allowed-nginx}")
    private String nginxPort;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = { allowedMfContainer, nginxPort };
        registry.addMapping("/**")
            .allowedOriginPatterns(allowedOrigins)
            .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")
            .allowedHeaders("Content-Type", "Authorization", "Accept")
            .exposedHeaders("Authorization")
            .allowCredentials(true)
            .maxAge(3600);
    }
}

