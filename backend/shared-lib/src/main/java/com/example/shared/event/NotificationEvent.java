package com.example.shared.event;

import java.io.Serializable;

public class NotificationEvent implements Serializable {
    private Integer userId;
    private String title;
    private String message;
    private String eventType;

    public NotificationEvent() {
    }

    public NotificationEvent(Integer userId, String title, String message, String eventType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.eventType = eventType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
