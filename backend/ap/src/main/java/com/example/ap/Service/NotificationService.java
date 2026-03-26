package com.example.ap.Service;


import com.example.ap.models.Notification;
import com.example.ap.Repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {


    @Autowired
    private NotificationRepository notificationRepository;

    // Create notification
    public void createNotification(Integer userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false); // par défaut non lu
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Get user notifications
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Mark notification as read
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
    public Optional<Notification> getById(Integer id) {
        return notificationRepository.findById(id);
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

}