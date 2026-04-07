package com.example.auth.service;

import com.example.auth.config.RabbitConfig;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.shared.dto.AuthResponse;
import com.example.shared.dto.UserDTO;
import com.example.shared.event.EmailEvent;
import com.example.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, rabbitTemplate);
    }

    @Test
    void loginReturnsTokenAndMappedUserForActiveAccount() {
        User user = user(7, "alice@example.com", "Alice", "Jones", "ADMIN", true, "encoded-secret");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);
        when(jwtUtil.generateToken(7, "alice@example.com", "ADMIN")).thenReturn("jwt-token");

        AuthResponse response = authService.login("alice@example.com", "secret");

        assertEquals("jwt-token", response.getToken());
        assertEquals(7, response.getUser().getId());
        assertEquals("alice@example.com", response.getUser().getEmail());
        assertEquals("ADMIN", response.getUser().getRole());
        assertEquals(true, response.getUser().getUserActive());
    }

    @Test
    void loginRejectsInactiveUsers() {
        User user = user(7, "alice@example.com", "Alice", "Jones", "USER", false, "encoded-secret");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login("alice@example.com", "secret"));

        assertEquals("Account is not active. Please wait for admin approval.", exception.getReason());
    }

    @Test
    void registerEncodesPasswordSetsDefaultsAndReturnsDto() {
        User user = user(null, "new@example.com", "New", "User", null, true, "plain-secret");
        when(passwordEncoder.encode("plain-secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(11);
            return saved;
        });

        UserDTO response = authService.register(user);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("encoded-secret", savedUser.getValue().getSecretPassword());
        assertEquals("USER", savedUser.getValue().getRole());
        assertEquals(false, savedUser.getValue().getUserActive());
        assertNotNull(savedUser.getValue().getCreatedAt());
        assertEquals(11, response.getId());
        assertEquals("USER", response.getRole());
    }

    @Test
    void updateUserEncodesNewPasswordsBeforeSaving() {
        User existing = user(4, "person@example.com", "Old", "Name", "USER", true, "old-encoded");
        User incoming = user(null, null, "New", "Surname", null, null, "plain-update");
        when(userRepository.findById(4)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("plain-update")).thenReturn("encoded-update");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO response = authService.updateUser(4, incoming);

        assertEquals("New", response.getFirstName());
        assertEquals("Surname", response.getLastName());
        assertEquals("encoded-update", existing.getSecretPassword());
    }

    @Test
    void forgotPasswordPersistsEncodedTempPasswordAndPublishesEmailEvent() {
        User existing = user(9, "reset@example.com", "Reset", "User", "USER", true, "old-secret");
        when(userRepository.findByEmail("reset@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-temp");

        String result = authService.forgotPassword("reset@example.com");

        ArgumentCaptor<String> tempPassword = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(tempPassword.capture());
        assertEquals(8, tempPassword.getValue().length());

        ArgumentCaptor<EmailEvent> emailEvent = ArgumentCaptor.forClass(EmailEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.email.forgot-password"),
                emailEvent.capture()
        );
        assertEquals("reset@example.com", emailEvent.getValue().getToEmail());
        assertEquals("Password Reset", emailEvent.getValue().getSubject());
        assertEquals(false, emailEvent.getValue().isHtml());
        assertEquals("Temporary password sent to your email.", result);
    }

    private static User user(Integer id, String email, String firstName, String lastName,
                             String role, Boolean active, String password) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setUserActive(active);
        user.setSecretPassword(password);
        return user;
    }
}
