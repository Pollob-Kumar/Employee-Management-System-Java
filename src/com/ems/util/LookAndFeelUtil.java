package com.ems.util;

import javax.swing.*;

public final class LookAndFeelUtil {
    private LookAndFeelUtil() {}

    public static void useNimbusIfAvailable() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fallback to default
        }
    }
}