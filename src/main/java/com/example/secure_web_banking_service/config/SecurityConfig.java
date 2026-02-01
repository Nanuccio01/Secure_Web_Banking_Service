package com.example.secure_web_banking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login.html",
                                "/register.html",
                                "/css/**",
                                "/js/**",
                                "/auth/register",
                                "/auth/login",
                                "/h2-console/**",
                                "/api/**"
                        ).permitAll()
                        // attenzione: /home lo gestiamo noi con controller (redirect). Lo lasciamo accessibile.
                        .requestMatchers("/home").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

