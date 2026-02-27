package com.ems.service;

import com.ems.dao.UserAdminDao;
import com.ems.model.UserRole;
import com.ems.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

public class UserAdminService {
    private static final int ITERATIONS = 120_000;

    private final UserAdminDao dao = new UserAdminDao();

    public long createManagerOrEmployee(UserCreateRequest req) {
        if (req == null) throw new IllegalArgumentException("Request required");
        if (req.role != UserRole.MANAGER && req.role != UserRole.EMPLOYEE)
            throw new IllegalArgumentException("Role must be MANAGER or EMPLOYEE");

        String username = normalize(req.username);
        if (username.isEmpty() || username.length() < 3) throw new IllegalArgumentException("Username min 3 chars");
        if (username.length() > 50) throw new IllegalArgumentException("Username too long");

        if (req.password == null || req.password.length < 8) throw new IllegalArgumentException("Password min 8 chars");

        if (req.departmentId <= 0) throw new IllegalArgumentException("Department required");

        String fullName = normalize(req.fullName);
        if (fullName.isEmpty() || fullName.length() < 3) throw new IllegalArgumentException("Full name required");

        LocalDate jd;
        try {
            jd = LocalDate.parse(normalize(req.joinDate)); // expects YYYY-MM-DD
        } catch (Exception e) {
            throw new IllegalArgumentException("Join date must be YYYY-MM-DD");
        }

        String email = normalize(req.email);
        if (!email.isEmpty() && !email.contains("@")) throw new IllegalArgumentException("Invalid email");

        String phone = normalize(req.phone);
        String address = normalize(req.address);

        String designation = null;
        BigDecimal allowance = null;

        if (req.role == UserRole.MANAGER) {
            designation = normalize(req.designation);
            if (designation.isEmpty()) throw new IllegalArgumentException("Designation required for manager");
            if (designation.length() > 80) throw new IllegalArgumentException("Designation too long");

            allowance = (req.allowance == null) ? BigDecimal.ZERO : req.allowance;
            if (allowance.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Allowance cannot be negative");
        }

        byte[] salt = PasswordUtil.generateSalt();
        byte[] hash = PasswordUtil.pbkdf2(req.password, salt, ITERATIONS);

        // clear password in memory
        for (int i = 0; i < req.password.length; i++) req.password[i] = '\0';

        return dao.createUserAndProfile(
                username,
                req.role,
                hash,
                salt,
                ITERATIONS,
                req.departmentId,
                fullName,
                emptyToNull(email),
                emptyToNull(phone),
                emptyToNull(address),
                Date.valueOf(jd),
                designation,
                allowance
        );
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}