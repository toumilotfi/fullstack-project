package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import com.example.shared.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerWebMvcTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController(notificationService, restTemplate);
        ReflectionTestUtils.setField(controller, "gatewaySecret", "gw-secret");
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void notificationStatusBindsPathVariable() throws Exception {
        when(notificationService.getUserNotifications(4)).thenReturn(List.of(notification(11, 4, "Status")));

        mockMvc.perform(get("/api/v1/Not/notifications/status/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].userId").value(4))
                .andExpect(jsonPath("$[0].title").value("Status"));
    }

    @Test
    void createNotificationRejectsNonAdminRequests() throws Exception {
        mockMvc.perform(post("/api/v1/Not/notifications")
                        .param("userId", "4")
                        .param("title", "Status")
                        .param("message", "Body")
                        .header("X-User-Role", "USER")
                        .header("X-Gateway-Secret", "gw-secret"))
                .andExpect(status().isForbidden());
    }

    @Test
    void notifyAllRejectsNonAdminRequests() throws Exception {
        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.eq("http://auth-service/api/v1/admin/users"),
                org.mockito.ArgumentMatchers.eq(org.springframework.http.HttpMethod.GET),
                org.mockito.ArgumentMatchers.any(org.springframework.http.HttpEntity.class),
                org.mockito.ArgumentMatchers.eq(UserDTO[].class)
        )).thenReturn(ResponseEntity.ok(new UserDTO[]{new UserDTO(1, "admin@example.com", "Admin", "User", "ADMIN", true)}));

        mockMvc.perform(post("/api/v1/Not/notify/all")
                        .param("message", "Broadcast")
                        .header("X-User-Role", "USER")
                        .header("X-Gateway-Secret", "gw-secret"))
                .andExpect(status().isForbidden());
    }

    @Test
    void notificationStatusRejectsNonOwners() throws Exception {
        when(notificationService.getUserNotifications(4)).thenReturn(List.of(notification(11, 4, "Status")));

        mockMvc.perform(get("/api/v1/Not/notifications/status/4")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAsReadBindsPathVariable() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/Not/read/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Notification marked as read"));
    }

    private static Notification notification(Integer id, Integer userId, String title) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage("Body");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
