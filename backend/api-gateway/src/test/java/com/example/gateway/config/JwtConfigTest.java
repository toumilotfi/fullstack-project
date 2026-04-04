package com.example.gateway.config;

import com.example.shared.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtConfigTest {

    @Test
    void jwtUtilBeanUsesConfiguredSecret() {
        JwtConfig config = new JwtConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", "my-super-secret-key-that-is-at-least-32-characters-long");

        JwtUtil jwtUtil = config.jwtUtil();
        String token = jwtUtil.generateToken(4, "admin@example.com", "ADMIN");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(4, jwtUtil.extractUserId(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertEquals("admin@example.com", jwtUtil.extractEmail(token));
    }
}
