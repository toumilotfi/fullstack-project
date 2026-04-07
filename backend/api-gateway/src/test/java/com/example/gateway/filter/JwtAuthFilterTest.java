package com.example.gateway.filter;

import com.example.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthFilterTest {

    private JwtAuthFilter filter;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("my-super-secret-key-that-is-at-least-32-characters-long");
        filter = new JwtAuthFilter(jwtUtil);
    }

    @Test
    void openAuthPathBypassesAuthentication() {
        MockServerWebExchange exchange = exchange("/api/v1/auth/login", null);
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, passThroughChain(invoked, new AtomicReference<>())).block();

        assertTrue(invoked.get());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void openChatPathBypassesAuthentication() {
        MockServerWebExchange exchange = exchange("/chat/info", null);
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, passThroughChain(invoked, new AtomicReference<>())).block();

        assertTrue(invoked.get());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void checkStatusPathBypassesAuthentication() {
        MockServerWebExchange exchange = exchange("/api/v1/auth/check-status", null);
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, passThroughChain(invoked, new AtomicReference<>())).block();

        assertTrue(invoked.get());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void missingAuthorizationHeaderReturnsUnauthorized() {
        MockServerWebExchange exchange = exchange("/api/v1/Task/tasks", null);
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, passThroughChain(invoked, new AtomicReference<>())).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(false, invoked.get());
    }

    @Test
    void invalidTokenReturnsUnauthorized() {
        MockServerWebExchange exchange = exchange("/api/v1/Task/tasks", "Bearer not-a-token");
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, passThroughChain(invoked, new AtomicReference<>())).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(false, invoked.get());
    }

    @Test
    void validTokenAddsDownstreamHeaders() {
        String token = jwtUtil.generateToken(12, "user@example.com", "USER");
        MockServerWebExchange exchange = exchange("/api/v1/Task/tasks", "Bearer " + token);
        AtomicBoolean invoked = new AtomicBoolean(false);
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        filter.filter(exchange, passThroughChain(invoked, forwardedExchange)).block();

        assertTrue(invoked.get());
        assertNull(exchange.getResponse().getStatusCode());
        assertEquals("12", forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("USER", forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Role"));
        assertEquals("user@example.com", forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Email"));
    }

    @Test
    void orderRunsBeforeOtherFilters() {
        assertEquals(-1, filter.getOrder());
    }

    private static MockServerWebExchange exchange(String path, String authHeader) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authHeader != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return MockServerWebExchange.from(builder.build());
    }

    private static GatewayFilterChain passThroughChain(AtomicBoolean invoked, AtomicReference<ServerWebExchange> forwardedExchange) {
        return exchange -> {
            invoked.set(true);
            forwardedExchange.set(exchange);
            return Mono.empty();
        };
    }
}
