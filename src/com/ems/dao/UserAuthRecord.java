package com.ems.dao;

import com.ems.model.UserRole;

public class UserAuthRecord {
    public long id;
    public String username;
    public UserRole role;
    public String status;
    public byte[] passwordHash;
    public byte[] salt;
    public int iterations;
}