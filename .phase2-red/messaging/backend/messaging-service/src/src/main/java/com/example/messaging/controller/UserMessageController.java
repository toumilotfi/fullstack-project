package com.example.messaging.controller;

import com.example.messaging.model.ChatMessage;
import com.example.messaging.repository.ChatMessageRepository;
import com.example.messaging.service.ChatProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/User")
public class UserMessageController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatProducer chatProducer;

    public UserMessageController(ChatMessageRepository chatMessageRepository, ChatProducer chatProducer) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatProducer = chatProducer;
    }

    @PostMapping("/message/admin")
    public ResponseEntity<ChatMessage> userToAdmin(@RequestParam("userId") Integer userId,
                                                   @RequestParam("message") String message) {
        ChatMessage chatMessage = new ChatMessage(userId, 1, "USER", message, LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        chatProducer.sendToAdmin(savedMessage);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/messages/sent/{userId}")
    public ResponseEntity<List<ChatMessage>> getSentMessages(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId));
    }

    @GetMapping("/messages/inbox/{userId}")
    public ResponseEntity<List<ChatMessage>> getUserInbox(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId));
    }
}
