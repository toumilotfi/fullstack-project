package com.example.messaging.controller;

import com.example.messaging.model.ChatMessage;
import com.example.messaging.repository.ChatMessageRepository;
import com.example.messaging.service.ChatProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserMessageControllerWebMvcTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatProducer chatProducer;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserMessageController controller = new UserMessageController(chatMessageRepository, chatProducer);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void postMessageBindsRequestParameters() throws Exception {
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage saved = invocation.getArgument(0);
            saved.setId(27);
            return saved;
        });

        mockMvc.perform(post("/api/v1/User/message/admin")
                        .param("userId", "7")
                        .param("message", "Need help"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(27))
                .andExpect(jsonPath("$.senderId").value(7))
                .andExpect(jsonPath("$.receiverId").value(1))
                .andExpect(jsonPath("$.content").value("Need help"));

        verify(chatProducer).sendToAdmin(any(ChatMessage.class));
    }

    @Test
    void postMessageRejectsMismatchedHeaderUserId() throws Exception {
        mockMvc.perform(post("/api/v1/User/message/admin")
                        .param("userId", "7")
                        .param("message", "Need help")
                        .header("X-User-Id", "8"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSentMessagesBindsPathVariable() throws Exception {
        when(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(9))
                .thenReturn(List.of(message(1, 9, 1, "USER", "first")));

        mockMvc.perform(get("/api/v1/User/messages/sent/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("first"));
    }

    @Test
    void getSentMessagesRejectsMismatchedHeaderUserId() throws Exception {
        mockMvc.perform(get("/api/v1/User/messages/sent/9")
                        .header("X-User-Id", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserInboxRejectsMismatchedHeaderUserId() throws Exception {
        mockMvc.perform(get("/api/v1/User/messages/inbox/9")
                        .header("X-User-Id", "10"))
                .andExpect(status().isForbidden());
    }

    private static ChatMessage message(Integer id, Integer senderId, Integer receiverId, String senderRole, String content) {
        ChatMessage message = new ChatMessage(senderId, receiverId, senderRole, content, LocalDateTime.now());
        message.setId(id);
        return message;
    }
}
