package com.example.firstai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

//@Service
//public class JwtService {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${auth.service.url}")
//    private String authServiceUrl; // http://localhost:8081
//
//    public JwtService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public boolean validateToken(String token) {
//
//        if (token == null || token.isBlank())
//            return false;
//
//        String url = authServiceUrl + "/auth/validate";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url, HttpMethod.POST, entity, Map.class);
//
//            Boolean valid = (Boolean) response.getBody().get("valid");
//            return valid != null && valid;
//
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//
//        public String extractUsername(String token) {
//
//            String url = authServiceUrl + "/auth/me";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + token);
//
//            HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url, HttpMethod.POST, entity, Map.class);
//
//            return (String) response.getBody().get("username");
//        }
//    }

//    public String extractUsername(String token) {
//        return token;
//    }

@Service
public class JwtService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public JwtService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String validateAndGetUsername(String token) {

        if (token == null || token.isBlank()) return null;

        String url = authServiceUrl + "/auth/validate";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Boolean valid = (Boolean) response.getBody().get("valid");

            if (Boolean.TRUE.equals(valid)) {
                return (String) response.getBody().get("username");
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }


    public boolean validateToken(String token) {

        if (token == null || token.isBlank())
            return false;

        String url = authServiceUrl + "/auth/validate";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Boolean valid = (Boolean) response.getBody().get("valid");
            return valid != null && valid;

        } catch (Exception e) {
            return false;
        }
    }


    public String extractUsername(String token) {

          String url = authServiceUrl + "/auth/me";

           HttpHeaders headers = new HttpHeaders();
           headers.set("Authorization", "Bearer " + token);
         HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> response = restTemplate.exchange(
                   url, HttpMethod.POST, entity, Map.class);

          return (String) response.getBody().get("username");
       }
    }





