package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import com.example.shared.dto.UserDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:}")
    private String gatewaySecret;

    public NotificationController(NotificationService notificationService, RestTemplate restTemplate) {
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
    }

    private boolean isUnauthorized(String role, String secret) {
        return !"ADMIN".equals(role) || !gatewaySecret.equals(secret);
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> createNotification(@RequestParam("userId") Integer userId,
                                                   @RequestParam("title") String title,
                                                   @RequestParam("message") String message,
                                                   @RequestHeader(value = "X-User-Role", required = false) String role,
                                                   @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.createNotification(userId, title, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/{userId}")
    public ResponseEntity<String> notifyUser(@PathVariable("userId") Integer userId,
                                             @RequestParam("message") String message,
                                             @RequestHeader(value = "X-User-Role", required = false) String role,
                                             @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        notificationService.createNotification(userId, "Admin Notification", message);
        return ResponseEntity.ok("Notification sent to user " + userId);
    }

    @PostMapping("/notify/all")
    public ResponseEntity<String> notifyAll(@RequestParam("message") String message,
                                            @RequestHeader(value = "X-User-Role", required = false) String role,
                                            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", "ADMIN");
        headers.set("X-Gateway-Secret", gatewaySecret);

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
    public ResponseEntity<List<Notification>> getNotificationStatus(@PathVariable("userId") Integer userId,
                                                                    @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId,
                                                                    @RequestHeader(value = "X-User-Role", required = false) String role) {
        boolean isAdmin = "ADMIN".equals(role);
        boolean isOwner = headerUserId != null && headerUserId.equals(userId);
        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Integer id,
                                             @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}
