package com.school.auth;

import com.school.Main;
import com.school.dashboard.AdminDashboard;
import com.school.database.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Login screen for the School Management System
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        // Set up the frame
        setTitle("School Management System - Login");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Main.centerFrameOnScreen(this);
        setResizable(false);

        // Add window listener to close database connection on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseManager.closeConnection();
            }
        });

        // Create the panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("School Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        // Login button
        loginButton = new JButton("Login");
        loginButton.addActionListener((ActionEvent e) -> performLogin());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(loginButton);

        // Status label for displaying error messages
        statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add status label at the bottom
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.PAGE_END);

        // Add main panel to frame
        add(mainPanel);
    }

    /**
     * Perform login authentication
     */
    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }

        // Authenticate user
        String userRole = DatabaseManager.authenticateUser(username, password);

        if (userRole != null) {
            // Authentication successful
            switch (userRole) {
                case "ADMIN":
                    openAdminDashboard(username);
                    break;
                // Other roles can be handled here
                default:
                    statusLabel.setText("Unknown role: " + userRole);
                    break;
            }
        } else {
            // Authentication failed
            statusLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
    }

    /**
     * Open the admin dashboard
     * @param username The username of the logged-in admin
     */
    private void openAdminDashboard(String username) {
        this.dispose(); // Close login window
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard(username);
            dashboard.setVisible(true);
        });
    }
}
