package com.example.notification.service;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
    }

    @Test
    void sendApprovalEmailBuildsHtmlMimeMessage() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendApprovalEmail("user@example.com");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sent.writeTo(output);
        String rawMessage = output.toString(StandardCharsets.UTF_8);
        assertArrayEquals(new InternetAddress[]{new InternetAddress("user@example.com")}, sent.getRecipients(MimeMessage.RecipientType.TO));
        assertEquals("Your Account Has Been Approved ✅", sent.getSubject());
        assertTrue(rawMessage.contains("Account Approved"));
        assertTrue(rawMessage.contains("http://localhost:3000/login"));
    }

    @Test
    void sendEmailBuildsSimpleMailMessage() {
        emailService.sendEmail("user@example.com", "Subject", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertArrayEquals(new String[]{"user@example.com"}, sent.getTo());
        assertEquals("Subject", sent.getSubject());
        assertEquals("Body", sent.getText());
    }
}
