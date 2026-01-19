
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/ai/chat")
public class AiPublicController {
    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    @Value("${GROQ_API_KEY}")
    private String GROQ_API_KEY;

    @Value("${telegram.internal.token}")
    private String telegramInternalToken;

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final ChatHistoryService chatHistoryService;

    public AiPublicController(RestTemplate restTemplate, JwtService jwtService, ChatHistoryService chatHistoryService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping("/public")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {

        String userMessage = request.get("message");

        // TEMP dummy AI (replace later)
        String aiReply = "🤖 AI received: " + userMessage;

        Map<String, String> response = new HashMap<>();
        response.put("reply", aiReply);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/telegram")
    public ResponseEntity<?> telegramChat(
            @RequestHeader("X-INTERNAL-TOKEN") String internalToken,
            @RequestBody Map<String, String> body) {

        if (!telegramInternalToken.equals(internalToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid internal token"));
        }

        String telegramUserId = body.get("userId");
        String message = body.get("message");

        if (telegramUserId == null || message == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "userId and message required"));
        }

        String username = "telegram_" + telegramUserId;
        return processChat(username, message);
    }

    /* ===========================
       COMMON GROQ LOGIC
       =========================== */
    private ResponseEntity<?> processChat(String username, String userMessage) {

        chatHistoryService.saveMessage(username, userMessage, "USER");

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

        HttpEntity<String> entity =
                new HttpEntity<>(requestJson.toString(), headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

            JSONObject json = new JSONObject(response.getBody());
            String aiMessage = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            chatHistoryService.saveMessage(username, aiMessage, "AI");

            return ResponseEntity.ok(Map.of("response", aiMessage));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
