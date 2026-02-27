package com.ems.ui;

import com.ems.model.User;
import com.ems.model.UserRole;
import com.ems.service.AuthService;
import com.ems.util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JButton loginButton;

    public LoginFrame() {
        super("Employee Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("EMS Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Sign in with your username and password");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username");
        usernameField = new JTextField();
        usernameField.setColumns(20);

        JLabel passLabel = new JLabel("Password");
        passwordField = new JPasswordField();
        passwordField.setColumns(20);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(new Color(180, 30, 30));

        loginButton = new JButton("Login");
        loginButton.setFocusPainted(false);

        // layout
        gc.gridx = 0; gc.gridy = 0;
        form.add(userLabel, gc);
        gc.gridx = 1; gc.gridy = 0;
        form.add(usernameField, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(passLabel, gc);
        gc.gridx = 1; gc.gridy = 1;
        form.add(passwordField, gc);

        gc.gridx = 1; gc.gridy = 2;
        form.add(messageLabel, gc);

        gc.gridx = 1; gc.gridy = 3;
        form.add(loginButton, gc);

        root.add(form, BorderLayout.CENTER);

        // actions
        loginButton.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin()); // Enter press to login

        return root;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty()) {
            showError("Username required");
            return;
        }
        if (password.length == 0) {
            showError("Password required");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user == null) {
                showError("Invalid credentials or disabled account");
                return;
            }

            Session.setCurrentUser(user);
            openDashboard(user.getRole());

        } catch (Exception ex) {
            showError("Login error: " + ex.getMessage());
        } finally {
            // clear password field for safety
            passwordField.setText("");
        }
    }

    private void openDashboard(UserRole role) {
        SwingUtilities.invokeLater(() -> {
            JFrame next;
            if (role == UserRole.ADMIN) next = new AdminDashboardFrame();
            else if (role == UserRole.MANAGER) next = new ManagerDashboardFrame();
            else next = new EmployeeDashboardFrame();

            next.setVisible(true);
            dispose();
        });
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
    }
}