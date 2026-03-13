package com.example.ap.Controllers;

import com.example.ap.AppConstants;
import com.example.ap.Repositories.ChatMessageRepository;
import com.example.ap.Repositories.UserRepository;
import com.example.ap.Service.AuthService;
import com.example.ap.Service.ChatProducer;
import com.example.ap.Service.NotificationService;
import com.example.ap.Service.TaskService;
import com.example.ap.models.ChatMessage;
import com.example.ap.models.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
    @CrossOrigin
    @RequestMapping(AppConstants.API_BASE_URL + "/User")
    public class UserControler {
        @Autowired
        private  TaskService taskService;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private ChatMessageRepository chatMessageRepository;
        @Autowired
        private AuthService authService;
        @Autowired
        private NotificationService notificationService;

    private final ChatProducer chatProducer;
    public UserControler(ChatProducer chatProducer) {
        this.chatProducer = chatProducer;
    }
    // VIEW NOTIFICATIONS
    @GetMapping("/not/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Integer userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PostMapping("/logout")
    public String logout() {
        return "Logout successful";
    }
    // MARK NOTIFICATION AS READ
    @PutMapping("/read/{id}")
    public String markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return "Notification marked as read";
    }
    @PostMapping("/message/admin")
    public String userToAdmin(
            @RequestParam Integer userId,
            @RequestParam String message
    ) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create chat message
        ChatMessage chat = new ChatMessage(
                userId,

                1, // admin ID
                "USER",
                message,
                LocalDateTime.now()
        );
        // Save message so user can see it later
        chatMessageRepository.save(chat);
        // Send to RabbitMQ
        chatProducer.sendToAdmin(chat);

        return "Message sent to admin";
    }

    @GetMapping("/messages/{userId}")
    public List<ChatMessage> getUserMessages(@PathVariable Integer userId) {
        return chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId);
    }

    @GetMapping("/messages/inbox/{userId}")
    public List<ChatMessage> getUserInbox(@PathVariable Integer userId) {
        return chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId);
    }

}
