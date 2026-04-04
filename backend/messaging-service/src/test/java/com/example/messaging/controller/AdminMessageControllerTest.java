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
    }

    @Test
    void adminToUserRejectsNonAdminRole() {
        ResponseEntity<String> response = controller.adminToUser("USER", 1, 8, "Denied");

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

        ResponseEntity<String> response = controller.adminToUser("ADMIN", 2, 8, "Approved");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message sent to user", response.getBody());
        verify(chatProducer).sendToUser(any(ChatMessage.class));
    }

    @Test
    void getAdminMessagesUsesProvidedUserIdHeader() {
        when(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(4))
                .thenReturn(List.of(message(1, 4, 8, "ADMIN", "outbound")));

        ResponseEntity<List<ChatMessage>> response = controller.getAdminMessages(4);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(4, response.getBody().get(0).getSenderId());
    }

    @Test
    void getAdminMessagesFallsBackToDefaultAdminId() {
        when(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(1))
                .thenReturn(List.of(message(1, 1, 8, "ADMIN", "default")));

        ResponseEntity<List<ChatMessage>> response = controller.getAdminMessages(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("default", response.getBody().get(0).getContent());
    }

    @Test
    void getAdminInboxUsesProvidedHeader() {
        when(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(6))
                .thenReturn(List.of(message(2, 9, 6, "USER", "hello")));

        ResponseEntity<List<ChatMessage>> response = controller.getAdminInbox(6);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(6, response.getBody().get(0).getReceiverId());
    }

    @Test
    void getAdminInboxFallsBackToDefaultAdminId() {
        when(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(1))
                .thenReturn(List.of(message(3, 9, 1, "USER", "fallback")));

        ResponseEntity<List<ChatMessage>> response = controller.getAdminInbox(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fallback", response.getBody().get(0).getContent());
    }

    private static ChatMessage message(Integer id, Integer senderId, Integer receiverId, String senderRole, String content) {
        ChatMessage message = new ChatMessage(senderId, receiverId, senderRole, content, java.time.LocalDateTime.now());
        message.setId(id);
        return message;
    }
}
