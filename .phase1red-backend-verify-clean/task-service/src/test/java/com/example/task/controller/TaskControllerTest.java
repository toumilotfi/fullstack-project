package com.example.task.controller;

import com.example.task.model.Task;
import com.example.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    private TaskController controller;

    @BeforeEach
    void setUp() {
        controller = new TaskController(taskService);
    }

    @Test
    void createTaskReturnsSavedTask() {
        Task task = task(1, "Create");
        when(taskService.createTask(task)).thenReturn(task);

        ResponseEntity<Task> response = controller.createTask(task);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void getAllTasksReturnsTasks() {
        when(taskService.getAllTasks()).thenReturn(List.of(task(1, "One"), task(2, "Two")));

        ResponseEntity<List<Task>> response = controller.getAllTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getTaskReturnsRequestedTask() {
        when(taskService.getTaskById(3)).thenReturn(task(3, "Lookup"));

        ResponseEntity<Task> response = controller.getTask(3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Lookup", response.getBody().getTitle());
    }

    @Test
    void updateTaskReturnsUpdatedTask() {
        Task task = task(4, "Updated");
        when(taskService.updateTask(4, task)).thenReturn(task);

        ResponseEntity<Task> response = controller.updateTask(4, task);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, response.getBody().getId());
    }

    @Test
    void deleteTaskReturnsOk() {
        ResponseEntity<Void> response = controller.deleteTask(5);

        verify(taskService).deleteTask(5);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void respondToTaskReturnsUpdatedTask() {
        when(taskService.respondToTask(6, "response")).thenReturn(task(6, "Responded"));

        ResponseEntity<Task> response = controller.respondToTask(6, "response");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, response.getBody().getId());
    }

    @Test
    void approveTaskReturnsUpdatedTask() {
        when(taskService.approveTask(7)).thenReturn(task(7, "Approved"));

        ResponseEntity<Task> response = controller.approveTask(7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Approved", response.getBody().getTitle());
    }

    @Test
    void declineTaskReturnsUpdatedTask() {
        when(taskService.declineTask(8)).thenReturn(task(8, "Declined"));

        ResponseEntity<Task> response = controller.declineTask(8);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(8, response.getBody().getId());
    }

    @Test
    void requestRevisionReturnsUpdatedTask() {
        when(taskService.requestRevision(9)).thenReturn(task(9, "Revision"));

        ResponseEntity<Task> response = controller.requestRevision(9);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Revision", response.getBody().getTitle());
    }

    private static Task task(Integer id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        return task;
    }
}
