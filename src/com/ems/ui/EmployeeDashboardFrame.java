package com.ems.ui;

import javax.swing.*;
import java.awt.*;

public class EmployeeDashboardFrame extends JFrame {
    public EmployeeDashboardFrame() {
        super("EMS - Employee Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Employee Dashboard (next: profile, attendance, leave apply)", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);
    }
}