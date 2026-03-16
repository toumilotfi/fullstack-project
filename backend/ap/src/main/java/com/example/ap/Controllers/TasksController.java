package com.example.ap.Controllers;

import com.example.ap.AppConstants;
import com.example.ap.Repositories.TaskRepository;
import com.example.ap.Repositories.UserRepository;
import com.example.ap.Service.EmailService;
import com.example.ap.Service.NotificationService;
import com.example.ap.models.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_URL + "/Task")
@CrossOrigin
public class TasksController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskRepository taskRepository;

// --- TASK CRUD ---

@PostMapping("/tasks")
public Task createTask(@RequestBody Task task) {
    task.setCreatedAt(LocalDateTime.now());
    Task savedTask = taskRepository.save(task);

    // Send notification to assigned user

    notificationService.createNotification(
            task.getAssignedToUserId(),
            "New Task Assigned",
            "You have been assigned a new task: " + task.getTitle()
    );

    return savedTask;
}

@GetMapping("/tasks")
public List<Task> getAllTasks() {
    return taskRepository.findAll();
}

@GetMapping("/tasks/{id}")
public Task getTaskById(@PathVariable Integer id) {
    return taskRepository.findById(id).orElse(null);
}

@PutMapping("/tasks/{id}")
public Task updateTask(@PathVariable Integer id, @RequestBody Task taskDetails) {
    Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));

    task.setTitle(taskDetails.getTitle());
    task.setDescription(taskDetails.getDescription());
    task.setAssignedToUserId(taskDetails.getAssignedToUserId());

    return taskRepository.save(task);
}

@DeleteMapping("/tasks/{id}")
public void deleteTask(@PathVariable Integer id) {
    taskRepository.deleteById(id);
}
    @PutMapping("/tasks/{id}/respond")
    public Task respondToTask(@PathVariable Integer id, @RequestBody String response) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setUserResponse(response);
        task.setCompleted(true);
        task.setResponseAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        // Send notification to admin
        notificationService.createNotification(
                1, // admin user id (replace with real admin id)
                "Task Response Received",
                "User responded to task: " + task.getTitle()
        );

        return updatedTask;
    }
}
