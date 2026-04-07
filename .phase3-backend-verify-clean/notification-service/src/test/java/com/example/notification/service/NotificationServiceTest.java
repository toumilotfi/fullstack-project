package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void createNotificationBuildsUnreadNotificationWithTimestamp() {
        notificationService.createNotification(7, "Title", "Body");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(7, saved.getUserId());
        assertEquals("Title", saved.getTitle());
        assertEquals("Body", saved.getMessage());
        assertEquals(false, saved.isRead());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void getUserNotificationsDelegatesToRepository() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(9))
                .thenReturn(List.of(notification(1, 9, false)));

        List<Notification> notifications = notificationService.getUserNotifications(9);

        assertEquals(1, notifications.size());
        assertEquals(9, notifications.get(0).getUserId());
    }

    @Test
    void markAsReadUpdatesNotificationState() {
        Notification notification = notification(3, 5, false);
        when(notificationRepository.findById(3)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        notificationService.markAsRead(3);

        assertEquals(true, notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsWhenNotificationMissing() {
        when(notificationRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationService.markAsRead(99));

        assertEquals("Notification not found", exception.getMessage());
    }

    private static Notification notification(Integer id, Integer userId, boolean isRead) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setRead(isRead);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        return notification;
    }
}
