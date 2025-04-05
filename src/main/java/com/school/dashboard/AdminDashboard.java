package com.school.dashboard;

import com.school.Main;
import com.school.auth.LoginFrame;
import com.school.database.DatabaseManager;
import com.school.student.StudentManagementPanel;
import com.school.teacher.TeacherManagementPanel;
import com.school.fees.FeesManagementPanel;
import com.school.attendance.AttendanceManagementPanel;
import com.school.reports.ReportsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Admin Dashboard panel for the School Management System
 */
public class AdminDashboard extends JFrame {
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JPanel contentPanel;
    private JPanel statusPanel;
    private JLabel welcomeLabel;
    private JLabel dateTimeLabel;
    private JLabel statusLabel;

    private String username;

    // Dashboard Statistics
    private int totalStudents = 0;
    private int totalTeachers = 0;
    private int pendingFees = 0;

    /**
     * Constructor for AdminDashboard
     * @param username The logged-in admin's username
     */
    public AdminDashboard(String username) {
        this.username = username;

        // Set up the frame
        setTitle("School Management System - Admin Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Main.centerFrameOnScreen(this);
        setResizable(true);

        // Add window listener to close database connection on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseManager.closeConnection();
            }
        });

        // Initialize components
        initComponents();

        // Load dashboard statistics
        loadDashboardStats();

        // Set up timer to update date/time label every second
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
    }

    /**
     * Initialize dashboard components
     */
    private void initComponents() {
        // Main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Top panel for header and user info
        JPanel topPanel = createTopPanel();

        // Left panel for menu
        menuPanel = createMenuPanel();

        // Center panel for content
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());

        // Status panel for the bottom
        statusPanel = createStatusPanel();

        // Add the default dashboard panel to the content panel
        contentPanel.add(createDashboardPanel(), "DASHBOARD");

        // Add all panels to the main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
    }

    /**
     * Create the top panel with header and user info
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(51, 102, 153));
        topPanel.setPreferredSize(new Dimension(1200, 80));

        // School name label
        JLabel schoolLabel = new JLabel("School Management System");
        schoolLabel.setFont(new Font("Arial", Font.BOLD, 24));
        schoolLabel.setForeground(Color.WHITE);
        schoolLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // User info panel on the right
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        userPanel.add(welcomeLabel);
        userPanel.add(logoutButton);

        topPanel.add(schoolLabel, BorderLayout.WEST);
        topPanel.add(userPanel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * Create the menu panel with navigation buttons
     */
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(51, 51, 51));
        menuPanel.setPreferredSize(new Dimension(200, 600));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Menu buttons with action listeners
        JButton dashboardBtn = createMenuButton("Dashboard", "DASHBOARD");
        JButton studentsBtn = createMenuButton("Students", "STUDENTS");
        JButton teachersBtn = createMenuButton("Teachers", "TEACHERS");
        JButton feesBtn = createMenuButton("Fees Management", "FEES");
        JButton attendanceBtn = createMenuButton("Attendance", "ATTENDANCE");
        JButton reportsBtn = createMenuButton("Reports", "REPORTS");

        // Add buttons to menu panel
        menuPanel.add(dashboardBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(studentsBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(teachersBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(feesBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(attendanceBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(reportsBtn);

        return menuPanel;
    }

    /**
     * Create a menu button with specified text and action command
     */
    private JButton createMenuButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setActionCommand(actionCommand);
        button.addActionListener(e -> showPanel(e.getActionCommand()));
        return button;
    }

    /**
     * Create the dashboard panel with statistics and overview
     */
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());

        // Welcome header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Admin Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel);

        // Main dashboard content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(2, 2, 20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Statistics cards
        JPanel studentsCard = createStatsCard("Students", String.valueOf(totalStudents), "View Students", "STUDENTS");
        JPanel teachersCard = createStatsCard("Teachers", String.valueOf(totalTeachers), "View Teachers", "TEACHERS");
        JPanel feesCard = createStatsCard("Pending Fees", String.valueOf(pendingFees), "View Fees", "FEES");
        JPanel attendanceCard = createStatsCard("Today's Attendance", "Mark Now", "Take Attendance", "ATTENDANCE");

        contentPanel.add(studentsCard);
        contentPanel.add(teachersCard);
        contentPanel.add(feesCard);
        contentPanel.add(attendanceCard);

        // Date and time panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dateTimeLabel = new JLabel();
        updateDateTime();
        datePanel.add(dateTimeLabel);

        // Add components to dashboard panel
        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        dashboardPanel.add(contentPanel, BorderLayout.CENTER);
        dashboardPanel.add(datePanel, BorderLayout.SOUTH);

        return dashboardPanel;
    }

    /**
     * Create a statistics card with title, value and action button
     */
    private JPanel createStatsCard(String title, String value, String buttonText, String actionCommand) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JButton actionButton = new JButton(buttonText);
        actionButton.addActionListener(e -> showPanel(actionCommand));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(actionButton, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Create the status panel for footer
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Ready");

        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(versionLabel, BorderLayout.EAST);

        return statusPanel;
    }

    /**
     * Show the selected panel in the content area
     */
    private void showPanel(String panelName) {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        // Create panel if it doesn't exist yet
        if (panelName.equals("STUDENTS") && contentPanel.getComponentCount() < 2) {
            contentPanel.add(new StudentManagementPanel(this), "STUDENTS");
        } else if (panelName.equals("TEACHERS") && contentPanel.getComponentCount() < 3) {
            contentPanel.add(new TeacherManagementPanel(this), "TEACHERS");
        } else if (panelName.equals("FEES") && contentPanel.getComponentCount() < 4) {
            contentPanel.add(new FeesManagementPanel(this), "FEES");
        } else if (panelName.equals("ATTENDANCE") && contentPanel.getComponentCount() < 5) {
            contentPanel.add(new AttendanceManagementPanel(this), "ATTENDANCE");
        } else if (panelName.equals("REPORTS") && contentPanel.getComponentCount() < 6) {
            contentPanel.add(new ReportsPanel(this), "REPORTS");
        }

        cardLayout.show(contentPanel, panelName);
        statusLabel.setText("Current module: " + panelName.toLowerCase());
    }

    /**
     * Update the date and time label
     */
    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm:ss a");
        String formattedDate = dateFormat.format(new Date());
        dateTimeLabel.setText(formattedDate);
    }

    /**
     * Load statistics for the dashboard
     */
    private void loadDashboardStats() {
        try {
            // Get total students
            ResultSet studentRS = DatabaseManager.executeQuery("SELECT COUNT(*) FROM students");
            if (studentRS.next()) {
                totalStudents = studentRS.getInt(1);
            }

            // Get total teachers
            ResultSet teacherRS = DatabaseManager.executeQuery("SELECT COUNT(*) FROM teachers");
            if (teacherRS.next()) {
                totalTeachers = teacherRS.getInt(1);
            }

            // Get pending fees
            ResultSet feesRS = DatabaseManager.executeQuery("SELECT COUNT(*) FROM fees WHERE status = 'PENDING'");
            if (feesRS.next()) {
                pendingFees = feesRS.getInt(1);
            }

            // Close result sets
            studentRS.close();
            teacherRS.close();
            feesRS.close();

            // Refresh dashboard
            contentPanel.remove(0);
            contentPanel.add(createDashboardPanel(), "DASHBOARD", 0);
            showPanel("DASHBOARD");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dashboard statistics: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Log out and return to login screen
     */
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }

    /**
     * Refresh the statistics on the dashboard
     */
    public void refreshDashboard() {
        loadDashboardStats();
    }

    /**
     * Update the status message
     */
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
}
