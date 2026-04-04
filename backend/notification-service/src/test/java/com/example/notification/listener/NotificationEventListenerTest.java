package com.example.notification.listener;

import com.example.notification.service.NotificationService;
import com.example.shared.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(notificationService);
    }

    @Test
    void handleNotificationEventUsesEventUserWhenPresent() {
        NotificationEvent event = new NotificationEvent(8, "Assigned", "Task assigned", "TASK_ASSIGNED");

        listener.handleNotificationEvent(event);

        verify(notificationService).createNotification(8, "Assigned", "Task assigned");
    }

    @Test
    void handleNotificationEventFallsBackToDefaultAdmin() {
        NotificationEvent event = new NotificationEvent(null, "Admin", "Review task", "TASK_RESPONDED");

        listener.handleNotificationEvent(event);

        verify(notificationService).createNotification(1, "Admin", "Review task");
    }
}
