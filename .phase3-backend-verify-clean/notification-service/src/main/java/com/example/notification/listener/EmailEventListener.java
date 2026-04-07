package com.example.notification.listener;

import com.example.notification.config.RabbitConfig;
import com.example.notification.service.EmailService;
import com.example.shared.event.EmailEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailEventListener {

    private final EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void handleEmailEvent(EmailEvent event) {
        if (event.isHtml()) {
            emailService.sendApprovalEmail(event.getToEmail());
        } else {
            emailService.sendEmail(event.getToEmail(), event.getSubject(), event.getBody());
        }
    }
}
