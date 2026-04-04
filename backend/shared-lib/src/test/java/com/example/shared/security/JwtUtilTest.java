package com.example.shared.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "my-super-secret-key-that-is-at-least-32-characters-long";

    @Test
    void generatesTokenWithExpectedClaims() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);

        String token = jwtUtil.generateToken(7, "alice@example.com", "ADMIN");
        Claims claims = jwtUtil.extractClaims(token);

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("alice@example.com", claims.getSubject());
        assertEquals(7, jwtUtil.extractUserId(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertEquals("alice@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void rejectsMalformedTokens() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);

        assertFalse(jwtUtil.isTokenValid("not-a-jwt"));
    }
}
