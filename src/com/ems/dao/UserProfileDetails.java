package com.ems.dao;

import com.ems.model.UserRole;

import java.math.BigDecimal;

public class UserProfileDetails {
    public long userId;
    public UserRole role;

    public long departmentId;
    public String fullName;
    public String email;
    public String phone;
    public String address;
    public String joinDate; // YYYY-MM-DD

    // manager only
    public String designation;
    public BigDecimal allowance;
}