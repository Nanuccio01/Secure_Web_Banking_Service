package com.example.secure_web_banking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disabilita CSRF per POST JSON
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()       // register/login liberi
                        .requestMatchers("/h2-console/**").permitAll() // console H2 libera
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions().disable()); // necessario per H2 console

        return http.build();
    }
}
