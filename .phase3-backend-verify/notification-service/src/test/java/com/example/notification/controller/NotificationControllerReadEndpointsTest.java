package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerReadEndpointsTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService, restTemplate);
    }

    @Test
    void getNotificationStatusReturnsNotifications() {
        when(notificationService.getUserNotifications(6)).thenReturn(List.of(notification(1, 6)));

        ResponseEntity<List<Notification>> response = controller.getNotificationStatus(6);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void markAsReadReturnsConfirmation() {
        ResponseEntity<String> response = controller.markAsRead(7);

        verify(notificationService).markAsRead(7);
        assertEquals("Notification marked as read", response.getBody());
    }

    private static Notification notification(Integer id, Integer userId) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setTitle("Title");
        notification.setMessage("Body");
        notification.setRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        return notification;
    }
}
