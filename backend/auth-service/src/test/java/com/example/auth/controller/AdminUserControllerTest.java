package com.example.auth.controller;

import com.example.auth.model.User;
import com.example.auth.service.UserService;
import com.example.shared.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(userService);
        ReflectionTestUtils.setField(controller, "gatewaySecret", "gw-secret");
    }

    @Test
    void getAllUsersRejectsNonAdmins() {
        ResponseEntity<List<UserDTO>> response = controller.getAllUsers("USER", "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllUsersReturnsDtosForAdmins() {
        when(userService.getAllUsers()).thenReturn(List.of(new UserDTO(1, "one@example.com", "One", "User", "USER", true)));

        ResponseEntity<List<UserDTO>> response = controller.getAllUsers("ADMIN", "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAllUsersRejectsMissingGatewaySecret() {
        ResponseEntity<List<UserDTO>> response = controller.getAllUsers("ADMIN", null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getUserByIdRejectsNonAdmins() {
        ResponseEntity<UserDTO> response = controller.getUserById(1, null, "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getUserByIdReturnsDtoForAdmins() {
        when(userService.getUserById(2)).thenReturn(new UserDTO(2, "two@example.com", "Two", "User", "USER", true));

        ResponseEntity<UserDTO> response = controller.getUserById(2, "ADMIN", "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getId());
    }

    @Test
    void createUserRejectsNonAdmins() {
        ResponseEntity<UserDTO> response = controller.createUser(new User(), "USER", "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createUserReturnsDtoForAdmins() {
        User user = new User();
        when(userService.createUser(user)).thenReturn(new UserDTO(3, "three@example.com", "Three", "User", "USER", true));

        ResponseEntity<UserDTO> response = controller.createUser(user, "ADMIN", "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getId());
    }

    @Test
    void updateUserRejectsNonAdmins() {
        ResponseEntity<UserDTO> response = controller.updateUser(4, new User(), "USER", "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateUserReturnsDtoForAdmins() {
        User user = new User();
        when(userService.updateUser(4, user)).thenReturn(new UserDTO(4, "four@example.com", "Four", "User", "USER", true));

        ResponseEntity<UserDTO> response = controller.updateUser(4, user, "ADMIN", "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, response.getBody().getId());
    }

    @Test
    void deleteUserRejectsNonAdmins() {
        ResponseEntity<String> response = controller.deleteUser(5, "USER", "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteUserReturnsSuccessForAdmins() {
        ResponseEntity<String> response = controller.deleteUser(5, "ADMIN", "gw-secret");

        verify(userService).deleteUser(5);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    void approveUserRejectsNonAdmins() {
        ResponseEntity<UserDTO> response = controller.approveUser(6, "USER", "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void approveUserReturnsDtoForAdmins() {
        when(userService.approveUser(6)).thenReturn(new UserDTO(6, "six@example.com", "Six", "User", "USER", true));

        ResponseEntity<UserDTO> response = controller.approveUser(6, "ADMIN", "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, response.getBody().getId());
    }

    @Test
    void testEmailRejectsNonAdmins() {
        ResponseEntity<String> response = controller.testEmail("USER", "gw-secret");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testEmailReturnsMessageForAdmins() {
        when(userService.testEmail()).thenReturn("Test email event published.");

        ResponseEntity<String> response = controller.testEmail("ADMIN", "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test email event published.", response.getBody());
    }
}
