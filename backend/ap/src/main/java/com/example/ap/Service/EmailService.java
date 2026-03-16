package com.example.ap.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendApprovalEmail(String toEmail) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("lotfitoumi56@gmail.com");
            helper.setSubject("Your Account Has Been Approved ✅");

            String htmlContent =
                    "<div style='font-family:Arial,sans-serif;background:#f4f6f8;padding:40px'>" +
                            "<div style='max-width:600px;margin:auto;background:white;padding:30px;border-radius:10px'>" +

                            "<h2 style='color:#2c3e50'>Account Approved</h2>" +

                            "<p style='font-size:16px;color:#555'>Hello,</p>" +

                            "<p style='font-size:16px;color:#555'>" +
                            "Your account has been <b>approved by the administrator</b>." +
                            "</p>" +

                            "<p style='font-size:16px;color:#555'>" +
                            "You can now log in and start using the platform." +
                            "</p>" +

                            "<div style='text-align:center;margin:30px 0'>" +
                            "<a href='http://localhost:3000/login' " +
                            "style='background:#4CAF50;color:white;padding:12px 25px;text-decoration:none;border-radius:6px;font-weight:bold'>" +
                            "Login to your account</a>" +
                            "</div>" +

                            "<hr>" +

                            "<p style='font-size:14px;color:#888'>" +
                            "Best regards,<br><b>Admin</b>" +
                            "</p>" +

                            "</div></div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void sendEmail(String to, String subject, String message) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);
    }
}