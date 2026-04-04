package com.example.auth.service;

import com.example.auth.config.RabbitConfig;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.shared.dto.AuthResponse;
import com.example.shared.dto.UserDTO;
import com.example.shared.event.EmailEvent;
import com.example.shared.security.JwtUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getSecretPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        if (!user.getUserActive()) {
            throw new RuntimeException("Account is not active. Please wait for admin approval.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, toDTO(user));
    }

    public UserDTO register(User user) {
        user.setSecretPassword(passwordEncoder.encode(user.getSecretPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUserActive(false);
        user.setRole("USER");
        User saved = userRepository.save(user);
        return toDTO(saved);
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
        if (updatedUser.getSecretPassword() != null && !updatedUser.getSecretPassword().isEmpty()) {
            existing.setSecretPassword(passwordEncoder.encode(updatedUser.getSecretPassword()));
        }

        User saved = userRepository.save(existing);
        return toDTO(saved);
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setSecretPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        EmailEvent event = new EmailEvent(email, "Password Reset",
                "Your temporary password is: " + tempPassword, false);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.email.forgot-password", event);

        return "Temporary password sent to your email.";
    }

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getUserActive()
        );
    }
}
