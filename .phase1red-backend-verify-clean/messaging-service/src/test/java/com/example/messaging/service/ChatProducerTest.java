package com.example.messaging.service;

import com.example.messaging.config.RabbitConfig;
import com.example.messaging.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ChatProducer chatProducer;

    @BeforeEach
    void setUp() {
        chatProducer = new ChatProducer(rabbitTemplate);
    }

    @Test
    void sendToAdminPublishesToAdminRoutingKey() {
        ChatMessage message = message(3, 1, "USER", "hello admin");

        chatProducer.sendToAdmin(message);

        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, "admin", message);
    }

    @Test
    void sendToUserPublishesToUserRoutingKey() {
        ChatMessage message = message(1, 3, "ADMIN", "hello user");

        chatProducer.sendToUser(message);

        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, "user", message);
    }

    private static ChatMessage message(Integer senderId, Integer receiverId, String senderRole, String content) {
        return new ChatMessage(senderId, receiverId, senderRole, content, LocalDateTime.now());
    }
}
