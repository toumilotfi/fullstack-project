package com.example.messaging.service;

import com.example.messaging.config.RabbitConfig;
import com.example.messaging.model.ChatMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitConfig.USER_QUEUE)
    public void receiveUser(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/user", message);
    }

    @RabbitListener(queues = RabbitConfig.ADMIN_QUEUE)
    public void receiveAdmin(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/admin", message);
    }
}
