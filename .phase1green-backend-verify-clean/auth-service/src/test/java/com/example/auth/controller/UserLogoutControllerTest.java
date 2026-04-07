package com.example.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserLogoutControllerTest {

    @Test
    void logoutReturnsSuccessMessage() {
        UserLogoutController controller = new UserLogoutController();

        ResponseEntity<String> response = controller.logout();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
    }
}
