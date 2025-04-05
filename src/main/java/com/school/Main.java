package com.school;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.school.auth.LoginFrame;
import com.school.database.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Set the look and feel to FlatLaf for a modern UI
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Initialize Database
            DatabaseManager.initialize();

            // Ensure default admin account exists
            DatabaseManager.createDefaultAdminIfNotExists();

            // Set up the main frame and show login screen
            SwingUtilities.invokeLater(() -> {
                // Set app icon and display login screen
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        } catch (Exception e) {
            
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to start application: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Centers a JFrame on the screen
     * @param frame The JFrame to center
     */
    public static void centerFrameOnScreen(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }
}
