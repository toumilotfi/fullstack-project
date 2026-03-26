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
    // SEND MESSAGE USER → ADMIN
    @PostMapping("/message/admin")
    public ChatMessage userToAdmin(
            @RequestParam Integer userId,
            @RequestParam String message
    ) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage chat = new ChatMessage(
                userId,
                1, // admin ID
                "USER",
                message,
                LocalDateTime.now()
        );

        chatMessageRepository.save(chat);
        chatProducer.sendToAdmin(chat);

        return chat; // <-- FIX: return the message object
    }

    // SENT MESSAGES (user → admin)
    @GetMapping("/messages/sent/{userId}")
    public List<ChatMessage> getSentMessages(@PathVariable Integer userId) {
        return chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId);
    }

    // INBOX (admin → user)
    @GetMapping("/messages/inbox/{userId}")
    public List<ChatMessage> getUserInbox(@PathVariable Integer userId) {
        return chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId);
    }
}
