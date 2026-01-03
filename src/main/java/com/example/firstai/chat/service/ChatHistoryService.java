package com.example.firstai.chat.service;

import com.example.firstai.chat.entity.ChatMessage;
import com.example.firstai.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatHistoryService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Save a chat message (USER or AI)
     */
    public void saveMessage(String username, String message, String role) {

        ChatMessage msg = new ChatMessage();
        msg.setUsername(username);           // ✅ NOT token
        msg.setMessage(message);
        msg.setRole(role);                   // USER / AI
        msg.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(msg);
    }

    /**
     * Fetch chat history for logged-in user
     */
    public List<ChatMessage> getChatHistory(String username) {
        return chatMessageRepository
                .findByUsernameOrderByCreatedAtAsc(username);
    }
}
