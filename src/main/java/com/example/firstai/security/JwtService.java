package com.example.firstai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public JwtService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 🔐 Validate token with Auth Service and return username
     */
    public String validateAndGetUsername(String token) {

        if (token == null || token.isBlank()) return null;

        String url = authServiceUrl + "/auth/validate";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Boolean valid = (Boolean) response.getBody().get("valid");

            if (Boolean.TRUE.equals(valid)) {
                return (String) response.getBody().get("username");
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 🔍 Only validate token
     */
    public boolean validateToken(String token) {
        return validateAndGetUsername(token) != null;
    }

    /**
     * 👤 Extract username (used by controllers)
     */
    public String extractUsername(String token) {

        if (token == null || token.isBlank()) return null;

        String url = authServiceUrl + "/auth/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            return (String) response.getBody().get("username");

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ✅ REQUIRED BY SPRING SECURITY
     */
    public Authentication getAuthentication(String token) {

        String username = validateAndGetUsername(token);

        if (username == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
