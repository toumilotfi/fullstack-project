package com.example.ap.Controllers;


import com.example.ap.AppConstants;
import com.example.ap.Service.AuthService;
import com.example.ap.Service.NotificationService;
import com.example.ap.Service.TaskService;
import com.example.ap.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(AppConstants.API_BASE_URL + "/auth")
public class AuthenticationController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private NotificationService notificationService;

    // LOGIN
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password) {
        boolean valid = authService.authenticate(email, password);
        return valid ? "Authentication successful!" : "Invalid email or password.";
    }

    // Update a user
    @PutMapping("/update/{id}")
    public com.example.ap.models.User updateUser(@PathVariable Integer id, @RequestBody com.example.ap.models.User updatedUser) {
        Optional<com.example.ap.models.User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            com.example.ap.models.User user = optionalUser.get();
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setSecretPassword(updatedUser.getSecretPassword());
            return userRepository.save(user);
        }
        return null;
    }
    // REGISTER
    @PostMapping("/register")
    public com.example.ap.models.User register(@RequestBody com.example.ap.models.User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUserActive(false);
        return userRepository.save(user);
    }
}
