package com.example.task.service;

import com.example.shared.event.NotificationEvent;
import com.example.task.config.RabbitConfig;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    public TaskService(TaskRepository taskRepository, RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("ASSIGNED");
        Task saved = taskRepository.save(task);

        publishNotification(task.getAssignedToUserId(), "New Task Assigned",
                "You have been assigned: " + task.getTitle(), "TASK_ASSIGNED");
        return saved;
    }

    public Task updateTask(Integer id, Task updatedTask) {
        Task existing = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (updatedTask.getTitle() != null) {
            existing.setTitle(updatedTask.getTitle());
        }
        if (updatedTask.getDescription() != null) {
            existing.setDescription(updatedTask.getDescription());
        }
        if (updatedTask.getAssignedToUserId() != null) {
            existing.setAssignedToUserId(updatedTask.getAssignedToUserId());
        }
        return taskRepository.save(existing);
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public Task respondToTask(Integer id, String response) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setUserResponse(response);
        task.setStatus("SUBMITTED");
        task.setResponseAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        publishNotification(null, "Task Response Received",
                "A response was submitted for task: " + task.getTitle(), "TASK_RESPONDED");
        return saved;
    }

    public Task approveTask(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("APPROVED");
        Task saved = taskRepository.save(task);

        publishNotification(task.getAssignedToUserId(), "Task Approved",
                "Your task has been approved: " + task.getTitle(), "TASK_APPROVED");
        return saved;
    }

    public Task declineTask(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("DECLINED");
        Task saved = taskRepository.save(task);

        publishNotification(null, "Mission Declined",
                "Task declined: " + task.getTitle(), "TASK_DECLINED");
        return saved;
    }

    public Task requestRevision(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("REVISION_REQUESTED");
        Task saved = taskRepository.save(task);

        publishNotification(task.getAssignedToUserId(), "Revision Requested",
                "Please revise your task: " + task.getTitle(), "TASK_REVISION");
        return saved;
    }

    public List<Task> getTasksForUser(Integer userId) {
        return taskRepository.findByAssignedToUserId(userId);
    }

    private void publishNotification(Integer userId, String title, String message, String eventType) {
        NotificationEvent event = new NotificationEvent(userId, title, message, eventType);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.notification.task", event);
    }
}
