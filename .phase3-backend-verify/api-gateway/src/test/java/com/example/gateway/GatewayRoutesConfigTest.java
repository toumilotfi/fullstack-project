package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = ApiGatewayApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
class GatewayRoutesConfigTest {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Test
    void routeDefinitionsMatchPlannedOrderAndTargets() {
        assertEquals(
                List.of(
                        "auth-login",
                        "user-logout",
                        "admin-users",
                        "task-service",
                        "user-messages",
                        "admin-messages",
                        "websocket",
                        "notification-api"
                ),
                gatewayProperties.getRoutes().stream().map(route -> route.getId()).toList()
        );

        assertEquals(
                List.of(
                        "lb://auth-service",
                        "lb://auth-service",
                        "lb://auth-service",
                        "lb://task-service",
                        "lb://messaging-service",
                        "lb://messaging-service",
                        "lb:ws://messaging-service",
                        "lb://notification-service"
                ),
                gatewayProperties.getRoutes().stream().map(route -> route.getUri().toString()).toList()
        );
    }
}
