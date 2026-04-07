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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMessageControllerTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatProducer chatProducer;

    private UserMessageController controller;

    @BeforeEach
    void setUp() {
        controller = new UserMessageController(chatMessageRepository, chatProducer);
    }

    @Test
    void userToAdminSavesMessageAndPublishesIt() {
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(17);
            return saved;
        });

        ResponseEntity<ChatMessage> response = controller.userToAdmin(7, "Need help", 7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(17, response.getBody().getId());
        assertEquals(7, response.getBody().getSenderId());
        assertEquals(1, response.getBody().getReceiverId());
        assertEquals("USER", response.getBody().getSenderRole());
        assertEquals("Need help", response.getBody().getContent());
        assertNotNull(response.getBody().getCreatedAt());
        assertFalse(response.getBody().getRead());
        verify(chatProducer).sendToAdmin(response.getBody());
    }

    @Test
    void getSentMessagesReturnsRepositoryResults() {
        when(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(9))
                .thenReturn(List.of(message(1, 9, 1, "USER", "first"), message(2, 9, 1, "USER", "second")));

        ResponseEntity<List<ChatMessage>> response = controller.getSentMessages(9, 9);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getUserInboxReturnsRepositoryResults() {
        when(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(9))
                .thenReturn(List.of(message(3, 1, 9, "ADMIN", "reply")));

        ResponseEntity<List<ChatMessage>> response = controller.getUserInbox(9, 9);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("reply", response.getBody().get(0).getContent());
    }

    private static ChatMessage message(Integer id, Integer senderId, Integer receiverId, String senderRole, String content) {
        ChatMessage message = new ChatMessage(senderId, receiverId, senderRole, content, java.time.LocalDateTime.now());
        message.setId(id);
        return message;
    }
}
