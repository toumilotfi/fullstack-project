package com.example.task.controller;

import com.example.task.model.Task;
import com.example.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/Task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tasks/{id}/respond")
    public ResponseEntity<Task> respondToTask(@PathVariable Integer id, @RequestBody String response) {
        return ResponseEntity.ok(taskService.respondToTask(id, response));
    }

    @PutMapping("/tasks/approve/{id}")
    public ResponseEntity<Task> approveTask(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.approveTask(id));
    }

    @PutMapping("/tasks/{id}/decline")
    public ResponseEntity<Task> declineTask(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.declineTask(id));
    }

    @PutMapping("/tasks/{id}/request-revision")
    public ResponseEntity<Task> requestRevision(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.requestRevision(id));
    }
}
