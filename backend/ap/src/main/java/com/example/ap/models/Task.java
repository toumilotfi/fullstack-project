package com.example.ap.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "status", nullable = false)
    private String status = "ASSIGNED";

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "assigned_to_user_id", nullable = false)
    private Integer assignedToUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_response", length = 1000)
    private String userResponse;

    @Column(name = "response_at")
    private LocalDateTime responseAt;

    public Task() {
    }

    public Task(Integer id) {
        this.id = id;
    }

    public Task(String title, Integer id, String description, Integer assignedToUserId,
                LocalDateTime createdAt, String status,
                String userResponse, LocalDateTime responseAt) {
        this.title = title;
        this.id = id;
        this.description = description;
        this.assignedToUserId = assignedToUserId;
        this.createdAt = createdAt;
        this.status = status; 
        this.userResponse = userResponse;
        this.responseAt = responseAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Integer assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public LocalDateTime getResponseAt() {
        return responseAt;
    }

    public void setResponseAt(LocalDateTime responseAt) {
        this.responseAt = responseAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return Objects.equals(id, task.id) &&
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                Objects.equals(assignedToUserId, task.assignedToUserId) &&
                Objects.equals(createdAt, task.createdAt) &&
                Objects.equals(status, task.status) && 
                Objects.equals(userResponse, task.userResponse) &&
                Objects.equals(responseAt, task.responseAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, assignedToUserId, createdAt, status, userResponse, responseAt); 
    }
}
