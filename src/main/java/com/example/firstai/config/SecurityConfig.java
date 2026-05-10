package com.example.firstai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 🔓 PUBLIC ENDPOINTS
                .requestMatchers(
                    "/auth/**",
                    "/api/ai/**"   // ✅ FIXED HERE
                ).permitAll()

                // 🔐 EVERYTHING ELSE NEEDS JWT
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
