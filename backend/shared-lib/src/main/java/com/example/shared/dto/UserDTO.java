package com.example.shared.dto;

public class UserDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean userActive;

    public UserDTO() {
    }

    public UserDTO(Integer id, String email, String firstName, String lastName, String role, Boolean userActive) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.userActive = userActive;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getUserActive() {
        return userActive;
    }

    public void setUserActive(Boolean userActive) {
        this.userActive = userActive;
    }
}
