package com.example.ap.Controllers;
import com.example.ap.Service.ChatProducer;
import com.example.ap.AppConstants;
import com.example.ap.Repositories.ChatMessageRepository;
import com.example.ap.Repositories.TaskRepository;
import com.example.ap.Repositories.UserRepository;
import com.example.ap.Service.EmailService;
import com.example.ap.Service.NotificationService;
import com.example.ap.models.ChatMessage;
import com.example.ap.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(AppConstants.API_BASE_URL + "/admin")
public class AdminController {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskRepository taskRepository;
    
    private final ChatProducer chatProducer;

    public AdminController(ChatProducer chatProducer) {
        this.chatProducer = chatProducer;
    }
    // Approve a user
    @PutMapping("/users/approve/{id}")
    public User approveUser(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUserActive(true);
        User savedUser = userRepository.save(user);

        // send approval email
        try {
            emailService.sendApprovalEmail(savedUser.getEmail());
        } catch (Exception e) {
            e.printStackTrace(); // check console for errors
        }

        return savedUser;
    }
    @GetMapping("/test-email")
    public String testEmail() {
        emailService.sendApprovalEmail("lotfitoumi56@gmail.com");
        return "Email sent";
    }

    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by ID
    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }


    // Update a user
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setEmail(updatedUser.getEmail());
            user.setSecretPassword(updatedUser.getSecretPassword());
            user.setUserActive(updatedUser.getUserActive());
            return userRepository.save(user);
        }
        return null;
    }

    // Create user
    @PostMapping("/users/new")
    public User createUser(@RequestBody User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUserActive(true); // mark as active immediately
        return userRepository.save(user);

    }


    //  Delete a user
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "User deleted successfully!";
        }
        return "User not found!";
    }
    // --- message CRUD ---
    @PostMapping("/message/user")
    public String adminToUser(
            @RequestParam Integer adminId,
            @RequestParam Integer userId,
            @RequestParam String message
    ) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage chat = new ChatMessage(
                adminId,
                userId,
                "ADMIN",
                message,
                LocalDateTime.now()
        );
        // Save message so user can see it later
        chatMessageRepository.save(chat);
        // Send to RabbitMQ
        chatProducer.sendToUser(chat);

        return "Message sent to user";
    }

    @GetMapping("/messages")
    public List<ChatMessage> getAdminMessages() {
        return chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(1);
    }

    @GetMapping("/messages/inbox")
    public List<ChatMessage> getAdminInbox() {
        return chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(1);
    }
}
