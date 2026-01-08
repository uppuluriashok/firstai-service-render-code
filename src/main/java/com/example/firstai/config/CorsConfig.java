package com.example.firstai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allowed origins (Ionic + Render)
        config.setAllowedOrigins(List.of(
                "http://localhost:8100",
                "https://localhost",
                "capacitor://localhost",
                "ionic://localhost",
                "https://firstai-service-render-code.onrender.com"
        ));

        // ✅ Allowed methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // ✅ Allow all headers (Authorization included)
        config.setAllowedHeaders(List.of("*"));

        // ✅ Allow JWT cookies / headers
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
