package com.example.auth.controller;

import com.example.auth.model.User;
import com.example.auth.service.UserService;
import com.example.shared.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService userService;

    @org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:}")
    private String gatewaySecret;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    private boolean isUnauthorized(String role, String secret) {
        return !"ADMIN".equals(role) || !gatewaySecret.equals(secret);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
    ) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Integer id,
                                               @RequestHeader(value = "X-User-Role", required = false) String role,
                                               @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users/new")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user,
                                              @RequestHeader(value = "X-User-Role", required = false) String role,
                                              @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Integer id,
                                              @RequestBody User user,
                                              @RequestHeader(value = "X-User-Role", required = false) String role,
                                              @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Integer id,
                                             @RequestHeader(value = "X-User-Role", required = false) String role,
                                             @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/users/approve/{id}")
    public ResponseEntity<UserDTO> approveUser(@PathVariable("id") Integer id,
                                               @RequestHeader(value = "X-User-Role", required = false) String role,
                                               @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.approveUser(id));
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
    ) {
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.testEmail());
    }
}
