package com.example.ap.Service;

import com.example.ap.models.ChatMessage;
import com.example.ap.rabbit.RabbitConfig;
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

        System.out.println("USER MESSAGE RECEIVED → " + message.getContent());

        // Send message to frontend
        messagingTemplate.convertAndSend("/topic/user", message);
    }

    @RabbitListener(queues = RabbitConfig.ADMIN_QUEUE)
    public void receiveAdmin(ChatMessage message) {

        System.out.println("ADMIN MESSAGE RECEIVED → " + message.getContent());

        // Send message to frontend
        messagingTemplate.convertAndSend("/topic/admin", message);
    }
}