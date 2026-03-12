package com.example.ap.Controllers;

import com.example.ap.AppConstants;
import com.example.ap.Repositories.TaskRepository;
import com.example.ap.Repositories.UserRepository;
import com.example.ap.Service.EmailService;
import com.example.ap.Service.NotificationService;
import com.example.ap.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(AppConstants.API_BASE_URL + "/admin")
@CrossOrigin
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskRepository taskRepository;
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
    // --- TASK CRUD ---

}
