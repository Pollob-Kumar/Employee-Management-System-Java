package com.ems.ui;

import com.ems.ui.components.NavButton;
import com.ems.ui.panels.DepartmentsPanel;
import com.ems.ui.panels.UsersListPanel;
import com.ems.ui.panels.UsersPanel;
import com.ems.util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private NavButton btnDepartments;
    private NavButton btnUsersCreate;
    private NavButton btnUsersManage;

    public AdminDashboardFrame() {
        super("EMS - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        setContentPane(buildUI());
        showDepartments();
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 246, 250));

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(35, 40, 49));
        sidebar.setBorder(new EmptyBorder(14, 12, 14, 12));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel brand = new JLabel("EMS");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 20));
        brand.setBorder(new EmptyBorder(0, 6, 14, 6));

        btnDepartments = new NavButton("Departments");
        btnUsersCreate = new NavButton("Create User");
        btnUsersManage = new NavButton("Manage Users");

        sidebar.add(brand);
        sidebar.add(btnDepartments);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnUsersCreate);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnUsersManage);
        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("Logout");
        logout.setFocusPainted(false);
        sidebar.add(logout);

        // Topbar
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Color.WHITE);
        topbar.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel userLbl = new JLabel("Logged in: " + (Session.getCurrentUser() != null ? Session.getCurrentUser().getUsername() : ""));
        userLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        topbar.add(userLbl, BorderLayout.EAST);

        // Content cards
        content.add(new DepartmentsPanel(), "DEPARTMENTS");
        content.add(new UsersPanel(), "USERS_CREATE");
        content.add(new UsersListPanel(), "USERS_MANAGE");

        // Layout
        root.add(sidebar, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(topbar, BorderLayout.NORTH);
        center.add(content, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // Actions
        btnDepartments.addActionListener(e -> showDepartments());
        btnUsersCreate.addActionListener(e -> showUsersCreate());
        btnUsersManage.addActionListener(e -> showUsersManage());
        logout.addActionListener(e -> doLogout());

        return root;
    }

    private void setActive(NavButton active) {
        btnDepartments.setActive(active == btnDepartments);
        btnUsersCreate.setActive(active == btnUsersCreate);
        btnUsersManage.setActive(active == btnUsersManage);
    }

    private void showDepartments() {
        setActive(btnDepartments);
        cardLayout.show(content, "DEPARTMENTS");
    }

    private void showUsersCreate() {
        setActive(btnUsersCreate);
        cardLayout.show(content, "USERS_CREATE");
    }

    private void showUsersManage() {
        setActive(btnUsersManage);
        cardLayout.show(content, "USERS_MANAGE");
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Logout now?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        Session.clear();
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }
}