package com.example.messaging.service;

import com.example.messaging.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChatConsumer chatConsumer;

    @BeforeEach
    void setUp() {
        chatConsumer = new ChatConsumer(messagingTemplate);
    }

    @Test
    void receiveUserForwardsToUserTopic() {
        ChatMessage message = message(1, 5, "ADMIN", "update");

        chatConsumer.receiveUser(message);

        verify(messagingTemplate).convertAndSend("/topic/user", message);
    }

    @Test
    void receiveAdminForwardsToAdminTopic() {
        ChatMessage message = message(5, 1, "USER", "ping");

        chatConsumer.receiveAdmin(message);

        verify(messagingTemplate).convertAndSend("/topic/admin", message);
    }

    private static ChatMessage message(Integer senderId, Integer receiverId, String senderRole, String content) {
        return new ChatMessage(senderId, receiverId, senderRole, content, LocalDateTime.now());
    }
}
