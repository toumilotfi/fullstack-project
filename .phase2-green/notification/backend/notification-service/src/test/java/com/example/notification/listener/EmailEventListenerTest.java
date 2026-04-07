package com.example.notification.listener;

import com.example.notification.service.EmailService;
import com.example.shared.event.EmailEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EmailEventListenerTest {

    @Mock
    private EmailService emailService;

    private EmailEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new EmailEventListener(emailService);
    }

    @Test
    void handleEmailEventUsesApprovalEmailForHtmlEvents() {
        EmailEvent event = new EmailEvent("user@example.com", "Subject", "Body", true);

        listener.handleEmailEvent(event);

        verify(emailService).sendApprovalEmail("user@example.com");
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void handleEmailEventUsesPlainEmailForNonHtmlEvents() {
        EmailEvent event = new EmailEvent("user@example.com", "Subject", "Body", false);

        listener.handleEmailEvent(event);

        verify(emailService).sendEmail("user@example.com", "Subject", "Body");
        verifyNoMoreInteractions(emailService);
    }
}
