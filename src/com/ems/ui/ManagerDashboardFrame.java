package com.ems.ui;

import javax.swing.*;
import java.awt.*;

public class ManagerDashboardFrame extends JFrame {
    public ManagerDashboardFrame() {
        super("EMS - Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Manager Dashboard (next: dept employees, leave approvals, reports)", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);
    }
}