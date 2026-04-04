package com.example.auth.service;

import com.example.auth.config.RabbitConfig;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.shared.dto.UserDTO;
import com.example.shared.event.EmailEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final AuthService authService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RabbitTemplate rabbitTemplate,
                       AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
        this.authService = authService;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return authService.toDTO(user);
    }

    public UserDTO createUser(User user) {
        user.setSecretPassword(passwordEncoder.encode(user.getSecretPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUserActive(true);
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        User saved = userRepository.save(user);
        return authService.toDTO(saved);
    }

    public UserDTO updateUser(Integer id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getFirstName() != null) {
            existing.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existing.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getEmail() != null) {
            existing.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getSecretPassword() != null && !updatedUser.getSecretPassword().isEmpty()) {
            existing.setSecretPassword(passwordEncoder.encode(updatedUser.getSecretPassword()));
        }
        if (updatedUser.getUserActive() != null) {
            existing.setUserActive(updatedUser.getUserActive());
        }

        User saved = userRepository.save(existing);
        return authService.toDTO(saved);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public UserDTO approveUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserActive(true);
        userRepository.save(user);

        String htmlBody = "<html><body><h1>Account Approved</h1>"
                + "<p>Your account has been approved. You can now login.</p>"
                + "<a href='http://localhost:3000/login'>Login Here</a></body></html>";
        EmailEvent emailEvent = new EmailEvent(user.getEmail(), "Account Approved", htmlBody, true);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.email.approval", emailEvent);

        return authService.toDTO(user);
    }

    public String testEmail() {
        EmailEvent event = new EmailEvent("test@test.com", "Test Email", "This is a test email.", false);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.email.test", event);
        return "Test email event published.";
    }
}
