package com.ems.service;

import com.ems.dao.UserManagementDao;
import com.ems.dao.UserProfileDetails;
import com.ems.model.UserListRow;
import com.ems.model.UserRole;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class UserManagementService {

    private final UserManagementDao dao = new UserManagementDao();

    public List<UserListRow> list(String roleFilter, String search) {
        String rf = (roleFilter == null || roleFilter.isBlank()) ? "ALL" : roleFilter.trim().toUpperCase();
        if (!rf.equals("ALL") && !rf.equals("EMPLOYEE") && !rf.equals("MANAGER"))
            throw new IllegalArgumentException("Invalid role filter");
        return dao.listUsers(rf, search);
    }

    public void delete(long userId) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid user id");
        dao.deleteUser(userId);
    }

    public void updateEmployee(long userId, long departmentId, String fullName, String email, String phone, String address, String joinDate) {
        validateCommon(userId, departmentId, fullName, email, joinDate);
        Date jd = Date.valueOf(LocalDate.parse(joinDate.trim()));
        dao.updateEmployeeProfile(userId, departmentId, normalize(fullName), email, phone, address, jd);
    }

    public void updateManager(long userId, long departmentId, String fullName, String email, String phone, String address,
                              String joinDate, String designation, BigDecimal allowance) {
        validateCommon(userId, departmentId, fullName, email, joinDate);
        designation = normalize(designation);
        if (designation.isEmpty()) throw new IllegalArgumentException("Designation required");
        Date jd = Date.valueOf(LocalDate.parse(joinDate.trim()));
        if (allowance != null && allowance.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Allowance cannot be negative");
        dao.updateManagerProfile(userId, departmentId, normalize(fullName), email, phone, address, jd, designation, allowance);
    }

    private void validateCommon(long userId, long departmentId, String fullName, String email, String joinDate) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid user id");
        if (departmentId <= 0) throw new IllegalArgumentException("Department required");

        fullName = normalize(fullName);
        if (fullName.isEmpty()) throw new IllegalArgumentException("Full name required");

        if (email != null && !email.trim().isEmpty() && !email.contains("@"))
            throw new IllegalArgumentException("Invalid email");

        try {
            LocalDate.parse(joinDate.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Join date must be YYYY-MM-DD");
        }
    }

    // ... আগের methods থাকবে ...
    public UserProfileDetails loadProfile(long userId, UserRole role) {
        if (userId <= 0) throw new IllegalArgumentException("Invalid user id");
        if (role != UserRole.EMPLOYEE && role != UserRole.MANAGER)
            throw new IllegalArgumentException("Role must be EMPLOYEE or MANAGER");
        return dao.loadProfile(userId, role);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
}