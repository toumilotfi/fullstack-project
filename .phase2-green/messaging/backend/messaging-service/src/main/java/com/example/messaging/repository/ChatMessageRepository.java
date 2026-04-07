package com.example.messaging.repository;

import com.example.messaging.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findBySenderIdOrderByCreatedAtAsc(Integer senderId);

    List<ChatMessage> findByReceiverIdOrderByCreatedAtAsc(Integer receiverId);
}
