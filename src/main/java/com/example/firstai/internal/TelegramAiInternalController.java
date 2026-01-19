package com.example.firstai.internal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/internal/ai")
public class TelegramAiInternalController {

    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    @Value("${GROQ_API_KEY}")
    private String groqApiKey;

    private final RestTemplate restTemplate;

    public TelegramAiInternalController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/telegram-chat")
    public ResponseEntity<?> chatFromTelegram(
            @RequestBody Map<String, String> body) {

        String userMessage = body.get("message");
        String systemPrompt = body.get("systemPrompt");

        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        // ✅ Default system prompt (VERY IMPORTANT)
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt =
                    "You are a helpful shop assistant. " +
                            "Answer politely, clearly, and concisely.";
        }

        // 🔐 Groq headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        // 🧠 Build Groq request JSON
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", "llama-3.3-70b-versatile");

        JSONArray messages = new JSONArray();

        // 1️⃣ SYSTEM MESSAGE
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt)
        );

        // 2️⃣ USER MESSAGE
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage)
        );

        requestJson.put("messages", messages);

        HttpEntity<String> entity =
                new HttpEntity<>(requestJson.toString(), headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            GROQ_API_URL,
                            entity,
                            String.class
                    );

            JSONObject json = new JSONObject(response.getBody());

            String aiMessage = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return ResponseEntity.ok(
                    Map.of("response", aiMessage)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Groq AI error"));
        }
    }
}
