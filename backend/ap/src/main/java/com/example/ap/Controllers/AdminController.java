package com.example.ap.Controllers;

import com.example.ap.AppConstants;
import com.example.ap.Repositories.TaskRepository;
import com.example.ap.Repositories.UserRepository;
import com.example.ap.Service.EmailService;
import com.example.ap.Service.NotificationService;
import com.example.ap.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
