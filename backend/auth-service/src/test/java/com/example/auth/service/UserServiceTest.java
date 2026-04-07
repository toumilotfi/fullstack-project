package com.example.auth.service;

import com.example.auth.config.RabbitConfig;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.shared.dto.UserDTO;
import com.example.shared.event.EmailEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private AuthService authService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, rabbitTemplate, authService);
        ReflectionTestUtils.setField(userService, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void getAllUsersMapsRepositoryResultsToDtos() {
        User first = user(1, "first@example.com", "First", "User", "USER", true, "pwd");
        User second = user(2, "second@example.com", "Second", "User", "ADMIN", true, "pwd");
        when(userRepository.findAll()).thenReturn(List.of(first, second));
        when(authService.toDTO(first)).thenReturn(new UserDTO(1, "first@example.com", "First", "User", "USER", true));
        when(authService.toDTO(second)).thenReturn(new UserDTO(2, "second@example.com", "Second", "User", "ADMIN", true));

        List<UserDTO> results = userService.getAllUsers();

        assertEquals(2, results.size());
        assertEquals("ADMIN", results.get(1).getRole());
    }

    @Test
    void createUserEncodesPasswordSetsDefaultsAndReturnsDto() {
        User user = user(null, "created@example.com", "Created", "User", null, null, "plain-secret");
        User saved = user(null, "created@example.com", "Created", "User", "USER", true, "encoded-secret");
        saved.setId(44);
        when(passwordEncoder.encode("plain-secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(authService.toDTO(saved)).thenReturn(new UserDTO(44, "created@example.com", "Created", "User", "USER", true));

        UserDTO response = userService.createUser(user);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("encoded-secret", savedUser.getValue().getSecretPassword());
        assertEquals(true, savedUser.getValue().getUserActive());
        assertEquals("USER", savedUser.getValue().getRole());
        assertNotNull(savedUser.getValue().getCreatedAt());
        assertEquals(44, response.getId());
    }

    @Test
    void updateUserEncodesNewPasswordsAndPersistsChanges() {
        User existing = user(7, "current@example.com", "Current", "Name", "USER", true, "encoded-old");
        User incoming = user(null, "new@example.com", "New", "Name", null, false, "plain-new");
        User saved = user(7, "new@example.com", "New", "Name", "USER", false, "encoded-new");
        when(userRepository.findById(7)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("plain-new")).thenReturn("encoded-new");
        when(userRepository.save(existing)).thenReturn(saved);
        when(authService.toDTO(saved)).thenReturn(new UserDTO(7, "new@example.com", "New", "Name", "USER", false));

        UserDTO response = userService.updateUser(7, incoming);

        assertEquals("new@example.com", response.getEmail());
        assertEquals(false, response.getUserActive());
        assertEquals("encoded-new", existing.getSecretPassword());
    }

    @Test
    void deleteUserDelegatesToRepository() {
        userService.deleteUser(12);

        verify(userRepository).deleteById(12);
    }

    @Test
    void approveUserActivatesUserAndPublishesApprovalEmail() {
        User existing = user(5, "approved@example.com", "Approve", "Me", "USER", false, "pwd");
        when(userRepository.findById(5)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(authService.toDTO(existing)).thenReturn(new UserDTO(5, "approved@example.com", "Approve", "Me", "USER", true));

        UserDTO response = userService.approveUser(5);

        assertEquals(true, existing.getUserActive());
        assertEquals(true, response.getUserActive());

        ArgumentCaptor<EmailEvent> eventCaptor = ArgumentCaptor.forClass(EmailEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.email.approval"),
                eventCaptor.capture()
        );
        assertEquals("approved@example.com", eventCaptor.getValue().getToEmail());
        assertEquals("Account Approved", eventCaptor.getValue().getSubject());
        assertEquals(true, eventCaptor.getValue().isHtml());
        assertEquals(true, eventCaptor.getValue().getBody().contains("http://localhost:4200/login"));
    }

    @Test
    void testEmailPublishesTestEvent() {
        String result = userService.testEmail();

        ArgumentCaptor<EmailEvent> eventCaptor = ArgumentCaptor.forClass(EmailEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.email.test"),
                eventCaptor.capture()
        );
        assertEquals("test@test.com", eventCaptor.getValue().getToEmail());
        assertEquals("Test email event published.", result);
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
