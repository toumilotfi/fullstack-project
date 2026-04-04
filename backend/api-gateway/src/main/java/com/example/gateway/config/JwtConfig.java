package com.example.gateway.config;

import com.example.shared.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret);
    }
}
