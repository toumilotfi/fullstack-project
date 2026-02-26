package com.example.ap.models;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "fName", nullable = false)
    private String firstName;
    @Column(name = "lName")
    private String lastName;
    @Column(name = "password", nullable = false)
    private String secretPassword;
    @Column(name = "userA")
    private Boolean userActive = true;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public User() {
    }

    public User(LocalDateTime createdAt,
                Boolean userActive,
                String secretPassword,
                String lastName,
                String firstName,
                String email
    ) {
        this.createdAt = createdAt;
        this.userActive = userActive;
        this.secretPassword = secretPassword;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSecretPassword() {
        return secretPassword;
    }

    public void setSecretPassword(String secretPassword) {
        this.secretPassword = secretPassword;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Boolean getUserActive() {
        return userActive;
    }

    public void setUserActive(Boolean userActive) {
        this.userActive = userActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(secretPassword, user.secretPassword) && Objects.equals(userActive, user.userActive) && Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, firstName, lastName, secretPassword, userActive, createdAt);
    }
}