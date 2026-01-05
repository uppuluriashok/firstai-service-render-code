package com.example.firstai;

import com.example.firstai.chat.entity.ChatMessage;
import com.example.firstai.chat.service.ChatHistoryService;
import com.example.firstai.security.JwtService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api.key}")
    private String GROQ_API_KEY;

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final ChatHistoryService chatHistoryService;

    public AIController(RestTemplate restTemplate,
                        JwtService jwtService,
                        ChatHistoryService chatHistoryService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.chatHistoryService = chatHistoryService;
    }

    // =========================
    // CHAT
    // =========================
    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing Authorization header"));
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        String userMessage = body.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        // Save USER message
        chatHistoryService.saveMessage(username, userMessage, "USER");

        // Groq request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GROQ_API_KEY);

        JSONObject req = new JSONObject();
        req.put("model", "llama-3.3-70b-versatile");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        req.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(req.toString(), headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

            JSONObject json = new JSONObject(response.getBody());
            String aiMessage = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // Save AI message
            chatHistoryService.saveMessage(username, aiMessage, "AI");

            return ResponseEntity.ok(Map.of("response", aiMessage));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // HISTORY
    // =========================
    @GetMapping("/history")
    public List<ChatMessage> history(
            @RequestHeader("Authorization") String auth) {

        String token = auth.substring(7);
        String username = jwtService.extractUsername(token);
        return chatHistoryService.getChatHistory(username);
    }
}
