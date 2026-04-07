package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import com.example.shared.dto.UserDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/v1/Not")
public class NotificationController {

    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    public NotificationController(NotificationService notificationService, RestTemplate restTemplate) {
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> createNotification(@RequestParam("userId") Integer userId,
                                                   @RequestParam("title") String title,
                                                   @RequestParam("message") String message) {
        notificationService.createNotification(userId, title, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/{userId}")
    public ResponseEntity<String> notifyUser(@PathVariable("userId") Integer userId,
                                             @RequestParam("message") String message) {
        notificationService.createNotification(userId, "Admin Notification", message);
        return ResponseEntity.ok("Notification sent to user " + userId);
    }

    @PostMapping("/notify/all")
    public ResponseEntity<String> notifyAll(@RequestParam("message") String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", "ADMIN");

        ResponseEntity<UserDTO[]> response = restTemplate.exchange(
                "http://auth-service/api/v1/admin/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserDTO[].class
        );

        UserDTO[] users = response.getBody();
        if (users != null) {
            for (UserDTO user : users) {
                notificationService.createNotification(user.getId(), "Admin Notification", message);
            }
        }

        return ResponseEntity.ok("Notification sent to all users");
    }

    @GetMapping("/notifications/status/{userId}")
    public ResponseEntity<List<Notification>> getNotificationStatus(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}
