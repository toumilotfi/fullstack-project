package com.example.messaging.controller;

import com.example.messaging.model.ChatMessage;
import com.example.messaging.repository.ChatMessageRepository;
import com.example.messaging.service.ChatProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminMessageController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatProducer chatProducer;

    @org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:}")
    private String gatewaySecret;

    public AdminMessageController(ChatMessageRepository chatMessageRepository, ChatProducer chatProducer) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatProducer = chatProducer;
    }

    @PostMapping("/message/user")
    public ResponseEntity<String> adminToUser(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret,
            @RequestParam("adminId") Integer adminId,
            @RequestParam("userId") Integer userId,
            @RequestParam("message") String message
    ) {
        if (!"ADMIN".equalsIgnoreCase(userRole) || !gatewaySecret.equals(gwSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        ChatMessage chatMessage = new ChatMessage(adminId, userId, "ADMIN", message, LocalDateTime.now());
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        chatProducer.sendToUser(savedMessage);
        return ResponseEntity.ok("Message sent to user");
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getAdminMessages(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
    ) {
        if (!gatewaySecret.equals(gwSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId));
    }

    @GetMapping("/messages/inbox")
    public ResponseEntity<List<ChatMessage>> getAdminInbox(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
    ) {
        if (!gatewaySecret.equals(gwSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId));
    }
}
