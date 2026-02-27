package com.ems.service;

import com.ems.model.UserRole;

import java.math.BigDecimal;

public class UserCreateRequest {
    public UserRole role;

    // auth
    public String username;
    public char[] password;

    // common profile
    public long departmentId;
    public String fullName;
    public String email;
    public String phone;
    public String address;
    public String joinDate; // YYYY-MM-DD

    // manager-only
    public String designation;
    public BigDecimal allowance;
}