package com.example.firstai.config;

import com.example.firstai.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Disable CSRF (JWT based)
                .csrf(csrf -> csrf.disable())

                // ✅ Enable CORS (uses CorsConfig bean)
                .cors(cors -> {})

                // ✅ Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ✅ Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // VERY IMPORTANT for browser & Ionic
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Protect AI APIs
                        .requestMatchers("/auth/**","/api/ai/**","/internal/ai/**").authenticated()

                        // Allow everything else
                        .anyRequest().permitAll()
                )

                // ✅ JWT filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
