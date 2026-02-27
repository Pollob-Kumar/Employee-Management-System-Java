package com.ems;

import com.ems.ui.LoginFrame;
import com.ems.util.LookAndFeelUtil;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        LookAndFeelUtil.useNimbusIfAvailable();

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}