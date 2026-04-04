package com.example.shared.event;

import java.io.Serializable;

public class EmailEvent implements Serializable {
    private String toEmail;
    private String subject;
    private String body;
    private boolean html;

    public EmailEvent() {
    }

    public EmailEvent(String toEmail, String subject, String body, boolean html) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        this.html = html;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }
}
