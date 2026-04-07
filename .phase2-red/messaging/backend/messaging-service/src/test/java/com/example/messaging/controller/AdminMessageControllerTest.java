package com.example.messaging.controller;

import com.example.messaging.model.ChatMessage;
import com.example.messaging.repository.ChatMessageRepository;
import com.example.messaging.service.ChatProducer;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMessageControllerTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatProducer chatProducer;

    private AdminMessageController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminMessageController(chatMessageRepository, chatProducer);
        ReflectionTestUtils.setField(controller, "gatewaySecret", "gw-secret");
    }

    @Test
    void adminToUserRejectsNonAdminRole() {
        ResponseEntity<String> response = controller.adminToUser("USER", "gw-secret", 1, 8, "Denied");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        verify(chatProducer, never()).sendToUser(any(ChatMessage.class));
    }

    @Test
    void adminToUserRejectsMissingGatewaySecret() {
        ResponseEntity<String> response = controller.adminToUser("ADMIN", null, 1, 8, "Denied");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        verify(chatProducer, never()).sendToUser(any(ChatMessage.class));
    }

    @Test
    void adminToUserSavesMessageAndPublishesItForAdmins() {
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(23);
            return saved;
        });

        ResponseEntity<String> response = controller.adminToUser("ADMIN", "gw-secret", 2, 8, "Approved");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message sent to user", response.getBody());
        verify(chatProducer).sendToUser(any(ChatMessage.class));
    }

    @Test
    void getAdminMessagesUsesProvidedUserIdHeader() {
        when(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(4))
                .thenReturn(List.of(message(1, 4, 8, "ADMIN", "outbound")));

        ResponseEntity<List<ChatMessage>> response = controller.getAdminMessages(4, "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(4, response.getBody().get(0).getSenderId());
    }

    @Test
    void getAdminMessagesRejectsRequestsWithoutGatewaySecret() {
        ResponseEntity<List<ChatMessage>> response = controller.getAdminMessages(4, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAdminMessagesFallsBackToDefaultAdminId() {
        ResponseEntity<List<ChatMessage>> response = controller.getAdminMessages(null, "gw-secret");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void getAdminInboxUsesProvidedHeader() {
        when(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(6))
                .thenReturn(List.of(message(2, 9, 6, "USER", "hello")));

        ResponseEntity<List<ChatMessage>> response = controller.getAdminInbox(6, "gw-secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(6, response.getBody().get(0).getReceiverId());
    }

    @Test
    void getAdminInboxFallsBackToDefaultAdminId() {
        ResponseEntity<List<ChatMessage>> response = controller.getAdminInbox(null, "gw-secret");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    private static ChatMessage message(Integer id, Integer senderId, Integer receiverId, String senderRole, String content) {
        ChatMessage message = new ChatMessage(senderId, receiverId, senderRole, content, java.time.LocalDateTime.now());
        message.setId(id);
        return message;
    }
}
