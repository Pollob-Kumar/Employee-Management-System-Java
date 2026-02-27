package com.ems.dao;

import com.ems.model.UserRole;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    public UserAuthRecord findAuthByUsername(String username) {
        String sql = """
            SELECT id, username, role, status, password_hash, password_salt, iterations
            FROM users
            WHERE username = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UserAuthRecord r = new UserAuthRecord();
                r.id = rs.getLong("id");
                r.username = rs.getString("username");
                r.role = UserRole.valueOf(rs.getString("role"));
                r.status = rs.getString("status");
                r.passwordHash = rs.getBytes("password_hash");
                r.salt = rs.getBytes("password_salt");
                r.iterations = rs.getInt("iterations");
                return r;
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error in findAuthByUsername", e);
        }
    }
}