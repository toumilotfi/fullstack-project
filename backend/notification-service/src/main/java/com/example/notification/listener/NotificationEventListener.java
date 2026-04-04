package com.example.notification.listener;

import com.example.notification.config.RabbitConfig;
import com.example.notification.service.NotificationService;
import com.example.shared.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        if (event.getUserId() != null) {
            notificationService.createNotification(event.getUserId(), event.getTitle(), event.getMessage());
        } else {
            notificationService.createNotification(1, event.getTitle(), event.getMessage());
        }
    }
}
