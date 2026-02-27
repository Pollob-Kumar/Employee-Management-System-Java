package com.ems.ui;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {
    public AdminDashboardFrame() {
        super("EMS - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Admin Dashboard (next: sidebar + modules)", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);
    }
}