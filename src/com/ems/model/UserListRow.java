package com.ems.model;

public class UserListRow {
    private long userId;
    private String username;
    private UserRole role;
    private String fullName;
    private String departmentName;

    public UserListRow(long userId, String username, UserRole role, String fullName, String departmentName) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.departmentName = departmentName;
    }

    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getDepartmentName() { return departmentName; }
}