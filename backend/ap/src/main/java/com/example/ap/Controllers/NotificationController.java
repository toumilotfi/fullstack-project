package com.example.ap.Controllers;
import com.example.ap.AppConstants;
import com.example.ap.Repositories.ChatMessageRepository;
import com.example.ap.Repositories.TaskRepository;
import com.example.ap.Repositories.UserRepository;
import com.example.ap.Service.ChatProducer;
import com.example.ap.Service.EmailService;
import com.example.ap.Service.NotificationService;
import com.example.ap.models.Notification;
import com.example.ap.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(AppConstants.API_BASE_URL + "/Not")
@CrossOrigin
public class NotificationController {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskRepository taskRepository;

    // --- ADMIN SEND CUSTOM NOTIFICATION ---

    @PostMapping("/notifications")
    public void sendNotification(@RequestParam Integer userId,
                                 @RequestParam String title,
                                 @RequestParam String message) {
        notificationService.createNotification(userId, title, message);
    }

    // --- Anotify --- user
    @PostMapping("/notify/{userId}")
    public String notifyUser(
            @PathVariable Integer userId,
            @RequestParam String message
    ) {
        notificationService.createNotification(userId, "Admin Message", message);
        return "Notification sent to user " + userId;
    }

    @PostMapping("/notify/all")
    public String notifyAllUsers(@RequestParam String message) {
        List<User> users =userRepository.findAll();
        for (User user : users) {
            notificationService.createNotification(
                    user.getId(),
                    "Admin Message",
                    message
            );
        }
        return "Notifications sent to all users";
    }

    @GetMapping("/notifications/status/{userId}")
    public List<Notification> getUserNotificationsStatus(@PathVariable Integer userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PutMapping("/read/{id}")
    public String markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return "Notification marked as read";
    } }
