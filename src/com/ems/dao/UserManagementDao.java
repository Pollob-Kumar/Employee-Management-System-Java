package com.ems.dao;

import com.ems.model.UserListRow;
import com.ems.model.UserRole;
import com.ems.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagementDao {

    public List<UserListRow> listUsers(String roleFilter, String searchText) {
        // roleFilter: "ALL" or "EMPLOYEE" or "MANAGER"
        // searchText: matches username/full_name

        String search = (searchText == null) ? "" : searchText.trim();

        String sql = """
            SELECT u.id AS user_id, u.username, u.role,
                   COALESCE(ep.full_name, mp.full_name) AS full_name,
                   d.name AS department_name
            FROM users u
            LEFT JOIN employee_profiles ep ON ep.user_id = u.id
            LEFT JOIN manager_profiles mp ON mp.user_id = u.id
            LEFT JOIN departments d ON d.id = COALESCE(ep.department_id, mp.department_id)
            WHERE u.role IN ('EMPLOYEE','MANAGER')
              AND (? = 'ALL' OR u.role = ?)
              AND ( ? = '' OR u.username LIKE ? OR COALESCE(ep.full_name, mp.full_name) LIKE ? )
            ORDER BY u.role, full_name, u.username
            """;

        List<UserListRow> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, roleFilter);
            ps.setString(2, roleFilter);

            ps.setString(3, search);
            ps.setString(4, "%" + search + "%");
            ps.setString(5, "%" + search + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("user_id");
                    String username = rs.getString("username");
                    UserRole role = UserRole.valueOf(rs.getString("role"));
                    String fullName = rs.getString("full_name");
                    String deptName = rs.getString("department_name");

                    list.add(new UserListRow(id, username, role, fullName, deptName));
                }
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("DB error in listUsers", e);
        }
    }

    public void deleteUser(long userId) {
        String sql = "DELETE FROM users WHERE id=? AND role IN ('EMPLOYEE','MANAGER')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            int n = ps.executeUpdate();
            if (n == 0) throw new IllegalStateException("User not found or not deletable");
        } catch (Exception e) {
            throw new RuntimeException("DB error in deleteUser", e);
        }
    }

    // --- Load profile for editing (common fields + role-specific) ---

    public ResultSet loadEmployeeProfile(Connection con, long userId) throws SQLException {
        String sql = """
            SELECT ep.department_id, ep.full_name, ep.email, ep.phone, ep.address, ep.join_date
            FROM employee_profiles ep
            WHERE ep.user_id = ?
            """;
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setLong(1, userId);
        return ps.executeQuery();
    }

    public ResultSet loadManagerProfile(Connection con, long userId) throws SQLException {
        String sql = """
            SELECT mp.department_id, mp.full_name, mp.email, mp.phone, mp.address, mp.join_date,
                   mp.designation, mp.allowance
            FROM manager_profiles mp
            WHERE mp.user_id = ?
            """;
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setLong(1, userId);
        return ps.executeQuery();
    }

    public void updateEmployeeProfile(long userId, long departmentId, String fullName, String email, String phone, String address, Date joinDate) {
        String sql = """
            UPDATE employee_profiles
            SET department_id=?, full_name=?, email=?, phone=?, address=?, join_date=?
            WHERE user_id=?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, departmentId);
            ps.setString(2, fullName);
            ps.setString(3, emptyToNull(email));
            ps.setString(4, emptyToNull(phone));
            ps.setString(5, emptyToNull(address));
            ps.setDate(6, joinDate);
            ps.setLong(7, userId);

            int n = ps.executeUpdate();
            if (n == 0) throw new IllegalStateException("Employee profile not found");

        } catch (Exception e) {
            throw new RuntimeException("DB error in updateEmployeeProfile", e);
        }
    }

    public void updateManagerProfile(long userId, long departmentId, String fullName, String email, String phone, String address, Date joinDate,
                                     String designation, BigDecimal allowance) {
        String sql = """
            UPDATE manager_profiles
            SET department_id=?, full_name=?, email=?, phone=?, address=?, join_date=?, designation=?, allowance=?
            WHERE user_id=?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, departmentId);
            ps.setString(2, fullName);
            ps.setString(3, emptyToNull(email));
            ps.setString(4, emptyToNull(phone));
            ps.setString(5, emptyToNull(address));
            ps.setDate(6, joinDate);
            ps.setString(7, designation);
            ps.setBigDecimal(8, allowance == null ? BigDecimal.ZERO : allowance);
            ps.setLong(9, userId);

            int n = ps.executeUpdate();
            if (n == 0) throw new IllegalStateException("Manager profile not found");

        } catch (Exception e) {
            throw new RuntimeException("DB error in updateManagerProfile", e);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    // ... আগের code থাকবে ...
    public UserProfileDetails loadProfile(long userId, UserRole role) {
        if (role == null) throw new IllegalArgumentException("Role required");

        String sqlEmployee = """
            SELECT department_id, full_name, email, phone, address, join_date
            FROM employee_profiles
            WHERE user_id=?
            """;

        String sqlManager = """
            SELECT department_id, full_name, email, phone, address, join_date, designation, allowance
            FROM manager_profiles
            WHERE user_id=?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(role == UserRole.MANAGER ? sqlManager : sqlEmployee)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Profile not found for userId=" + userId);

                UserProfileDetails d = new UserProfileDetails();
                d.userId = userId;
                d.role = role;

                d.departmentId = rs.getLong("department_id");
                d.fullName = rs.getString("full_name");
                d.email = rs.getString("email");
                d.phone = rs.getString("phone");
                d.address = rs.getString("address");
                d.joinDate = rs.getDate("join_date").toString();

                if (role == UserRole.MANAGER) {
                    d.designation = rs.getString("designation");
                    d.allowance = rs.getBigDecimal("allowance");
                }
                return d;
            }

        } catch (Exception e) {
            throw new RuntimeException("DB error in loadProfile", e);
        }
    }
}