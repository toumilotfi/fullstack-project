package com.example.ap.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;


public class Email {
    @Id
    @GeneratedValue

    private Long id;

    private String toEmail;
    private String subject;
    private String status;

    private LocalDateTime sentAt;
}