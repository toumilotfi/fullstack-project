package com.example.ap.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendApprovalEmail(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Account Approved");
        message.setText(
                "Hello,\n\n" +
                        "Your account has been approved by the admin.\n" +
                        "You can now log in.\n\n" +
                        "Best regards,\nAdmin"
        );
        message.setFrom("lotfitoumi56@gmail.com");

        mailSender.send(message);
    }
}
