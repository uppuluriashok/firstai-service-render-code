package com.example.firstai.internal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> chatFromTelegram(@RequestBody Map<String, String> body) {

        String userMessage = body.get("message");
        String systemPrompt = body.get("systemPrompt");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        JSONObject req = new JSONObject();
        req.put("model", "llama-3.3-70b-versatile");

        JSONArray messages = new JSONArray();

        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        req.put("messages", messages);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            GROQ_API_URL,
                            new HttpEntity<>(req.toString(), headers),
                            String.class);

            JSONObject json = new JSONObject(response.getBody());
            String aiText = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return ResponseEntity.ok(Map.of("response", aiText));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("response", "Groq AI error"));
        }
    }
}
