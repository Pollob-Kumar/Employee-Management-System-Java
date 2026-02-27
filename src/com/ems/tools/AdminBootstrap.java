package com.ems.tools;

import com.ems.util.DBConnection;
import com.ems.util.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Scanner;

public class AdminBootstrap {

    private static final int ITERATIONS = 120_000;

    public static void main(String[] args) {
        System.out.println("=== EMS Admin Bootstrap ===");
        System.out.println("This will create the first ADMIN user if it does not already exist.\n");

        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter admin username: ");
            String username = sc.nextLine().trim();

            if (username.isEmpty() || username.length() < 3) {
                System.out.println("Invalid username (min 3 chars).");
                return;
            }

            char[] password = readPassword(sc, "Enter admin password (min 8 chars): ");
            if (password.length < 8) {
                System.out.println("Password too short.");
                Arrays.fill(password, '\0');
                return;
            }

            char[] confirm = readPassword(sc, "Confirm password: ");
            if (!Arrays.equals(password, confirm)) {
                System.out.println("Password mismatch.");
                Arrays.fill(password, '\0');
                Arrays.fill(confirm, '\0');
                return;
            }
            Arrays.fill(confirm, '\0');

            if (adminExists()) {
                System.out.println("An ADMIN already exists. Bootstrap cancelled.");
                Arrays.fill(password, '\0');
                return;
            }

            if (usernameExists(username)) {
                System.out.println("Username already exists. Choose a different username.");
                Arrays.fill(password, '\0');
                return;
            }

            byte[] salt = PasswordUtil.generateSalt();
            byte[] hash = PasswordUtil.pbkdf2(password, salt, ITERATIONS);

            Arrays.fill(password, '\0'); // clear password from memory

            long newId = insertAdmin(username, hash, salt, ITERATIONS);
            System.out.println("SUCCESS: ADMIN created with user id = " + newId);

        } catch (Exception e) {
            System.out.println("Bootstrap failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // If console password hiding is needed later, you can use System.console().readPassword().
    private static char[] readPassword(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine();
        return s.toCharArray();
    }

    private static boolean adminExists() throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE role='ADMIN' LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private static boolean usernameExists(String username) throws Exception {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static long insertAdmin(String username, byte[] hash, byte[] salt, int iterations) throws Exception {
        String sql = """
            INSERT INTO users (username, role, password_hash, password_salt, iterations, status)
            VALUES (?, 'ADMIN', ?, ?, ?, 'ACTIVE')
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setBytes(2, hash);
            ps.setBytes(3, salt);
            ps.setInt(4, iterations);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new IllegalStateException("No generated key returned.");
            }
        }
    }
}