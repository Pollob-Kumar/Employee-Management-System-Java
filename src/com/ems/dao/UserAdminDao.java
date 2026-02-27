package com.ems.dao;

import com.ems.model.UserRole;
import com.ems.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class UserAdminDao {

    public long createUserAndProfile(
            String username,
            UserRole role,
            byte[] passwordHash,
            byte[] salt,
            int iterations,
            long departmentId,
            String fullName,
            String email,
            String phone,
            String address,
            Date joinDate,
            String designation,          // manager-only
            BigDecimal allowance         // manager-only
    ) {
        String insertUserSql = """
            INSERT INTO users (username, role, password_hash, password_salt, iterations, status)
            VALUES (?, ?, ?, ?, ?, 'ACTIVE')
            """;

        String insertEmployeeSql = """
            INSERT INTO employee_profiles (user_id, department_id, full_name, email, phone, address, join_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        String insertManagerSql = """
            INSERT INTO manager_profiles (user_id, department_id, full_name, email, phone, address, join_date, designation, allowance)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                long userId;

                try (PreparedStatement ps = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, role.name());
                    ps.setBytes(3, passwordHash);
                    ps.setBytes(4, salt);
                    ps.setInt(5, iterations);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new IllegalStateException("No user id returned");
                        userId = keys.getLong(1);
                    }
                }

                if (role == UserRole.EMPLOYEE) {
                    try (PreparedStatement ps = con.prepareStatement(insertEmployeeSql)) {
                        ps.setLong(1, userId);
                        ps.setLong(2, departmentId);
                        ps.setString(3, fullName);
                        ps.setString(4, emptyToNull(email));
                        ps.setString(5, emptyToNull(phone));
                        ps.setString(6, emptyToNull(address));
                        ps.setDate(7, joinDate);
                        ps.executeUpdate();
                    }
                } else if (role == UserRole.MANAGER) {
                    try (PreparedStatement ps = con.prepareStatement(insertManagerSql)) {
                        ps.setLong(1, userId);
                        ps.setLong(2, departmentId);
                        ps.setString(3, fullName);
                        ps.setString(4, emptyToNull(email));
                        ps.setString(5, emptyToNull(phone));
                        ps.setString(6, emptyToNull(address));
                        ps.setDate(7, joinDate);
                        ps.setString(8, designation);
                        ps.setBigDecimal(9, allowance == null ? BigDecimal.ZERO : allowance);
                        ps.executeUpdate();
                    }
                } else {
                    throw new IllegalArgumentException("Only MANAGER/EMPLOYEE supported here");
                }

                con.commit();
                return userId;

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error creating user/profile", e);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}