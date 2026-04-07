package com.example.auth.controller;

import com.example.auth.model.User;
import com.example.auth.service.AuthService;
import com.example.shared.dto.AuthResponse;
import com.example.shared.dto.LoginRequest;
import com.example.shared.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    @Test
    void loginReturnsAuthResponse() {
        LoginRequest request = new LoginRequest("admin@example.com", "secret");
        AuthResponse response = new AuthResponse("token", new UserDTO(1, "admin@example.com", "Admin", "User", "ADMIN", true));
        when(authService.login("admin@example.com", "secret")).thenReturn(response);

        ResponseEntity<AuthResponse> result = controller.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("token", result.getBody().getToken());
    }

    @Test
    void registerReturnsCreatedUser() {
        User user = new User();
        UserDTO dto = new UserDTO(2, "new@example.com", "New", "User", "USER", false);
        when(authService.register(user)).thenReturn(dto);

        ResponseEntity<UserDTO> result = controller.register(user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().getId());
    }

    @Test
    void updateUserReturnsUpdatedDto() {
        User user = new User();
        UserDTO dto = new UserDTO(4, "updated@example.com", "Up", "Dated", "USER", true);
        when(authService.updateUser(4, user)).thenReturn(dto);

        ResponseEntity<UserDTO> result = controller.updateUser(4, user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("updated@example.com", result.getBody().getEmail());
    }

    @Test
    void forgotPasswordReturnsServiceMessage() {
        when(authService.forgotPassword("reset@example.com")).thenReturn("Temporary password sent to your email.");

        ResponseEntity<String> result = controller.forgotPassword("reset@example.com");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Temporary password sent to your email.", result.getBody());
    }
}
