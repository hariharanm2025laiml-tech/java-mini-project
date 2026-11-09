package model;

import java.time.LocalDateTime;

public class User {
    public enum Role {
        ADMIN, CUSTOMER, DELIVERY_STAFF
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    private int userId;
    private String username;
    private String password;
    private Role role;
    private String email;
    private Status status;
    private LocalDateTime createdAt;

    public User() {
        this.status = Status.ACTIVE;
    }

    public User(int userId, String username, String password, Role role, String email, Status status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.status = status;
    }

    public User(String username, String password, Role role, String email) {
        this(0, username, password, role, email, Status.ACTIVE);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    @Override
    public String toString() {
        return String.format("User[ID=%d, Username='%s', Role=%s, Email='%s', Status=%s]",
                userId, username, role, email, status);
    }
}
