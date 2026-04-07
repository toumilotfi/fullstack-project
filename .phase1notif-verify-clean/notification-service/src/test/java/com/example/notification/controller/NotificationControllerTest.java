package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import com.example.shared.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService, restTemplate);
        ReflectionTestUtils.setField(controller, "gatewaySecret", "gw-secret");
    }

    @Test
    void createNotificationDelegatesToService() {
        ResponseEntity<Void> response = controller.createNotification(7, "Title", "Message", "ADMIN", "gw-secret");

        verify(notificationService).createNotification(7, "Title", "Message");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void notifyUserUsesAdminNotificationTitle() {
        ResponseEntity<String> response = controller.notifyUser(9, "Check this", "ADMIN", "gw-secret");

        verify(notificationService).createNotification(9, "Admin Notification", "Check this");
        assertEquals("Notification sent to user 9", response.getBody());
    }

    @Test
    void notifyAllFetchesUsersFromAuthServiceAndCreatesNotifications() {
        UserDTO first = new UserDTO(1, "a@example.com", "A", "One", "USER", true);
        UserDTO second = new UserDTO(2, "b@example.com", "B", "Two", "USER", true);
        when(restTemplate.exchange(
                eq("http://auth-service/api/v1/admin/users"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserDTO[].class)
        )).thenReturn(ResponseEntity.ok(new UserDTO[]{first, second}));

        ResponseEntity<String> response = controller.notifyAll("Broadcast", "ADMIN", "gw-secret");

        verify(notificationService).createNotification(1, "Admin Notification", "Broadcast");
        verify(notificationService).createNotification(2, "Admin Notification", "Broadcast");
        assertEquals("Notification sent to all users", response.getBody());
    }

    @Test
    void getNotificationStatusReturnsUserNotifications() {
        when(notificationService.getUserNotifications(4)).thenReturn(List.of(notification(11, 4)));

        ResponseEntity<List<Notification>> response = controller.getNotificationStatus(4, 4, "USER");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void markAsReadReturnsConfirmation() {
        ResponseEntity<String> response = controller.markAsRead(12, 4);

        verify(notificationService).markAsRead(12);
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
