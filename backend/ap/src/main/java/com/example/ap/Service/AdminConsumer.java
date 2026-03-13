package com.example.ap.Service;


import com.example.ap.models.ChatMessage;
import com.example.ap.rabbit.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


    @Service
    public class AdminConsumer {

        @RabbitListener(queues = RabbitConfig.ADMIN_QUEUE)
        public void receiveFromUser(ChatMessage message) {
            System.out.println("ADMIN RECEIVED: " + message);
        }
}
