package com.example.notification.controller;

import com.example.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserNotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificationController(notificationService, restTemplate))
                .build();
    }

    @Test
    void legacyUserNotificationStatusRouteIsGone() throws Exception {
        mockMvc.perform(get("/api/v1/User/not/6"))
                .andExpect(status().isNotFound());
    }

    @Test
    void legacyUserNotificationReadRouteIsGone() throws Exception {
        mockMvc.perform(put("/api/v1/User/read/7"))
                .andExpect(status().isNotFound());
    }
}
