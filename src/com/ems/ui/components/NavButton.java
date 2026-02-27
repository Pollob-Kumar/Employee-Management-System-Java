package com.ems.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NavButton extends JButton {
    public NavButton(String text) {
        super(text);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.PLAIN, 14));
        setBackground(new Color(45, 52, 64));
        setForeground(Color.WHITE);
    }

    public void setActive(boolean active) {
        if (active) {
            setBackground(new Color(65, 105, 225)); // active blue
        } else {
            setBackground(new Color(45, 52, 64));
        }
    }
}