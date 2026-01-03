//package com.example.firstai;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/ai")
//@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
//public class AIController {
//
//    private final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
//
//    private final RestTemplate restTemplate;
//
//    public AIController(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
////    @Bean
////    public RestTemplate restTemplate() {
////        return new RestTemplate();
////    }
//
//    @PostMapping("/chat")
//    public ResponseEntity<?> chat(
//            @RequestHeader(value = "Authorization", required = false) String auth,
//            @RequestBody Map<String, String> body) {
//
//        String userMessage = body.get("message");
//
//        if (userMessage == null || userMessage.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "Message content is required"
//            ));
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY);
//
//        JSONObject requestBody = new JSONObject();
//        requestBody.put("model", "llama-3.3-70b-versatile");
//
//        JSONArray messages = new JSONArray();
//        messages.put(new JSONObject()
//                .put("role", "user")
//                .put("content", userMessage)
//        );
//
//        requestBody.put("messages", messages);
//
//        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
//            JSONObject json = new JSONObject(response.getBody());
//
//            String assistantMessage = json
//                    .getJSONArray("choices")
//                    .getJSONObject(0)
//                    .getJSONObject("message")
//                    .getString("content");
//
//            return ResponseEntity.ok(Map.of("response", assistantMessage));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("response", "⚠ Error: " + e.getMessage()));
//        }
//    }
//
//}
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

    private final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    @Value("${GROQ_API_KEY}")
    private String GROQ_API_KEY;
    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final ChatHistoryService chatHistoryService;

    public AIController(
            RestTemplate restTemplate,
            JwtService jwtService,
            ChatHistoryService chatHistoryService) {

        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * Main Chat API
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        // 1️⃣ Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing Authorization header"));
        }

        String token = authHeader.substring(7);

//        if (!jwtService.validateToken(token)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("error", "Invalid token"));
//        }

        // 2️⃣ Extract username (TEMP: token itself or from header)
        // Best practice: auth service should return username
      //  String username = token; // ⚠ replace later with real username
        String username = jwtService.validateAndGetUsername(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        // 3️⃣ Validate request body
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        // 4️⃣ Save USER message
        chatHistoryService.saveMessage(username, userMessage, "USER");

        // 5️⃣ Prepare Groq request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GROQ_API_KEY);

        JSONObject requestJson = new JSONObject();
        requestJson.put("model", "llama-3.3-70b-versatile");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        requestJson.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers);

        try {
            // 6️⃣ Call Groq
            ResponseEntity<String> response =
                    restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

            JSONObject json = new JSONObject(response.getBody());
            String aiMessage = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 7️⃣ Save AI message
            chatHistoryService.saveMessage(username, aiMessage, "AI");

            // 8️⃣ Send response to frontend
            return ResponseEntity.ok(Map.of("response", aiMessage));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI error: " + e.getMessage()));
        }
    }
    @GetMapping("/history")
    public List<ChatMessage> getHistory(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        String username = jwtService.extractUsername(token);
        return chatHistoryService.getChatHistory(username);
    }
}
