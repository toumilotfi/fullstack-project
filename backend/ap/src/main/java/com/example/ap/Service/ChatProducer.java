package com.example.ap.Service;


import com.example.ap.models.ChatMessage;
import com.example.ap.rabbit.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ChatProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendToAdmin(ChatMessage message) {

        System.out.println("Sending to ADMIN queue: " + message.getContent());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "admin",
                message
        );
    }

    public void sendToUser(ChatMessage message) {

        System.out.println("Sending to USER queue: " + message.getContent());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "user",
                message
        );
    }
}
