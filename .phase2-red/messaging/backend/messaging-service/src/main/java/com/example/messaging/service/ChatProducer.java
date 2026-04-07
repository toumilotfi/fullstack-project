package com.example.messaging.service;

import com.example.messaging.config.RabbitConfig;
import com.example.messaging.model.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatProducer {

    private final RabbitTemplate rabbitTemplate;

    public ChatProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToAdmin(ChatMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "admin", message);
    }

    public void sendToUser(ChatMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "user", message);
    }
}
