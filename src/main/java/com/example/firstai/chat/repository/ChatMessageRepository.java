package com.example.firstai.chat.repository;

import com.example.firstai.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUsernameOrderByCreatedAtAsc(String username);

}
