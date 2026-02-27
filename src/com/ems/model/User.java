package com.ems.model;

public class User {
    private long id;
    private String username;
    private UserRole role;
    private String status;

    public User(long id, String username, UserRole role, String status) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public String getStatus() { return status; }
}