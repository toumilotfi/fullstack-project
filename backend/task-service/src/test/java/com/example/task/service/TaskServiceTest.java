package com.example.task.service;

import com.example.shared.event.NotificationEvent;
import com.example.task.config.RabbitConfig;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, rabbitTemplate);
    }

    @Test
    void getAllTasksReturnsRepositoryResults() {
        Task first = task(1, "One", "Desc 1", 9, "ASSIGNED");
        Task second = task(2, "Two", "Desc 2", 7, "APPROVED");
        when(taskRepository.findAll()).thenReturn(List.of(first, second));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
        assertEquals("Two", tasks.get(1).getTitle());
    }

    @Test
    void getTaskByIdReturnsExistingTask() {
        Task task = task(4, "Lookup", "Task", 11, "ASSIGNED");
        when(taskRepository.findById(4)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(4);

        assertEquals(4, result.getId());
    }

    @Test
    void getTaskByIdThrowsWhenMissing() {
        when(taskRepository.findById(4)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.getTaskById(4));

        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void createTaskSetsAssignmentDefaultsAndPublishesNotification() {
        Task task = task(null, "Mission", "Desc", 15, null);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(21);
            return saved;
        });

        Task saved = taskService.createTask(task);

        assertEquals(21, saved.getId());
        assertEquals("ASSIGNED", saved.getStatus());
        assertNotNull(saved.getCreatedAt());

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.notification.task"),
                eventCaptor.capture()
        );
        assertEquals(15, eventCaptor.getValue().getUserId());
        assertEquals("TASK_ASSIGNED", eventCaptor.getValue().getEventType());
        assertEquals("New Task Assigned", eventCaptor.getValue().getTitle());
    }

    @Test
    void updateTaskAppliesOnlyProvidedFields() {
        Task existing = task(9, "Old Title", "Old Desc", 1, "ASSIGNED");
        Task incoming = task(null, "New Title", null, 3, null);
        when(taskRepository.findById(9)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task saved = taskService.updateTask(9, incoming);

        assertEquals("New Title", saved.getTitle());
        assertEquals("Old Desc", saved.getDescription());
        assertEquals(3, saved.getAssignedToUserId());
    }

    @Test
    void deleteTaskDelegatesToRepository() {
        taskService.deleteTask(33);

        verify(taskRepository).deleteById(33);
    }

    @Test
    void respondToTaskMarksTaskSubmittedAndPublishesAdminNotification() {
        Task existing = task(10, "Respond", "Desc", 22, "ASSIGNED");
        when(taskRepository.findById(10)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task saved = taskService.respondToTask(10, "Done it");

        assertEquals("SUBMITTED", saved.getStatus());
        assertEquals("Done it", saved.getUserResponse());
        assertNotNull(saved.getResponseAt());

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.notification.task"),
                eventCaptor.capture()
        );
        assertEquals(null, eventCaptor.getValue().getUserId());
        assertEquals("TASK_RESPONDED", eventCaptor.getValue().getEventType());
    }

    @Test
    void approveTaskMarksApprovedAndPublishesUserNotification() {
        Task existing = task(11, "Approve", "Desc", 42, "SUBMITTED");
        when(taskRepository.findById(11)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task saved = taskService.approveTask(11);

        assertEquals("APPROVED", saved.getStatus());

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.notification.task"),
                eventCaptor.capture()
        );
        assertEquals(42, eventCaptor.getValue().getUserId());
        assertEquals("TASK_APPROVED", eventCaptor.getValue().getEventType());
    }

    @Test
    void declineTaskMarksDeclinedAndPublishesAdminNotification() {
        Task existing = task(12, "Decline", "Desc", 42, "ASSIGNED");
        when(taskRepository.findById(12)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task saved = taskService.declineTask(12);

        assertEquals("DECLINED", saved.getStatus());

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.notification.task"),
                eventCaptor.capture()
        );
        assertEquals(null, eventCaptor.getValue().getUserId());
        assertEquals("TASK_DECLINED", eventCaptor.getValue().getEventType());
    }

    @Test
    void requestRevisionMarksRevisionRequestedAndPublishesUserNotification() {
        Task existing = task(13, "Revise", "Desc", 42, "SUBMITTED");
        when(taskRepository.findById(13)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task saved = taskService.requestRevision(13);

        assertEquals("REVISION_REQUESTED", saved.getStatus());

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.Mockito.eq(RabbitConfig.EVENT_EXCHANGE),
                org.mockito.Mockito.eq("event.notification.task"),
                eventCaptor.capture()
        );
        assertEquals(42, eventCaptor.getValue().getUserId());
        assertEquals("TASK_REVISION", eventCaptor.getValue().getEventType());
    }

    @Test
    void getTasksForUserDelegatesToRepositoryQuery() {
        when(taskRepository.findByAssignedToUserId(5)).thenReturn(List.of(task(1, "Mine", "Desc", 5, "ASSIGNED")));

        List<Task> results = taskService.getTasksForUser(5);

        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getAssignedToUserId());
    }

    private static Task task(Integer id, String title, String description, Integer assignedToUserId, String status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setAssignedToUserId(assignedToUserId);
        task.setStatus(status);
        return task;
    }
}
