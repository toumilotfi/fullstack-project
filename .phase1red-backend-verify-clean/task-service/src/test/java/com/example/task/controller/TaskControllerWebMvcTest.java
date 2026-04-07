package com.example.task.controller;

import com.example.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerWebMvcTest {

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService)).build();
    }

    @Test
    void approveTaskRejectsRequestsWithoutAdminHeaders() throws Exception {
        mockMvc.perform(put("/api/v1/Task/tasks/approve/7"))
                .andExpect(status().isForbidden());
    }

    @Test
    void declineTaskRejectsRequestsWithoutAdminHeaders() throws Exception {
        mockMvc.perform(put("/api/v1/Task/tasks/7/decline"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestRevisionRejectsRequestsWithoutAdminHeaders() throws Exception {
        mockMvc.perform(put("/api/v1/Task/tasks/7/request-revision"))
                .andExpect(status().isForbidden());
    }
}
