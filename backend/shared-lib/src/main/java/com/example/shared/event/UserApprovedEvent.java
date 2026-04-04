package com.example.shared.event;

import java.io.Serializable;

public class UserApprovedEvent implements Serializable {
    private Integer userId;
    private String email;

    public UserApprovedEvent() {
    }

    public UserApprovedEvent(Integer userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
