package com.example.messaging.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    private WebSocketConfig config;

    @BeforeEach
    void setUp() {
        config = new WebSocketConfig();
    }

    @Test
    void configureMessageBrokerUsesTopicBrokerAndAppPrefix() {
        config.configureMessageBroker(messageBrokerRegistry);

        verify(messageBrokerRegistry).enableSimpleBroker("/topic");
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void registerStompEndpointsExposesChatEndpointWithSockJs() {
        when(stompEndpointRegistry.addEndpoint("/chat")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*")).thenReturn(endpointRegistration);

        config.registerStompEndpoints(stompEndpointRegistry);

        verify(stompEndpointRegistry).addEndpoint("/chat");
        verify(endpointRegistration).setAllowedOriginPatterns("*");
        verify(endpointRegistration).withSockJS();
    }
}
