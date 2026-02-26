package com.example.ap.models;

public class TaskComplete {
    private Integer taskId;
    private String username;

    public TaskComplete() {
    }

    public TaskComplete(Integer taskId, String username) {
        this.taskId = taskId;
        this.username = username;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
