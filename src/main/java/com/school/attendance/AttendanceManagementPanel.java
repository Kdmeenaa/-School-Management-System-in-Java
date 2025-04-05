package com.school.attendance;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.school.dashboard.AdminDashboard;
import com.school.database.DatabaseManager;

/**
 * Panel for managing student attendance in the School Management System
 */
public class AttendanceManagementPanel extends JPanel {
    private AdminDashboard dashboard;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> classFilterComboBox;
    private JTextField dateField;
    private JButton markAttendanceButton, viewReportButton, saveButton, refreshButton;
    private JPanel attendanceMarkingPanel;

    private final Map<Integer, JComboBox<String>> attendanceStatusMap = new HashMap<>();

    /**
     * Constructor for AttendanceManagementPanel
     * @param dashboard Reference to the AdminDashboard for status updates
     */
    public AttendanceManagementPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        initComponents();

        // Load student data
        loadStudentsForAttendance();
    }

    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Top panel for filters and controls
        JPanel topPanel = createTopPanel();

        // Table panel for displaying students
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Student Attendance"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create the table model with column names
        String[] columnNames = {
                "ID", "Roll Number", "Name", "Class", "Section", "Attendance Status", "Remarks"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable directly
            }
        };

        // Create the table and set properties
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        studentTable.getTableHeader().setReorderingAllowed(false);

        // Hide the ID column
        studentTable.getColumnModel().getColumn(0).setMinWidth(0);
        studentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        studentTable.getColumnModel().getColumn(0).setWidth(0);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Attendance marking panel
        attendanceMarkingPanel = createAttendanceMarkingPanel();
        attendanceMarkingPanel.setVisible(false);

        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(attendanceMarkingPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the top panel with filters and date selection
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Date panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date (YYYY-MM-DD):"));

        dateField = new JTextField(10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(dateFormat.format(new Date()));
        datePanel.add(dateField);

        // Class filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Class:"));

        classFilterComboBox = new JComboBox<>();
        classFilterComboBox.addItem("All Classes");
        loadClassesIntoComboBox();
        classFilterComboBox.addActionListener(e -> loadStudentsForAttendance());
        filterPanel.add(classFilterComboBox);

        // Action buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStudentsForAttendance());

        markAttendanceButton = new JButton("Mark Attendance");
        markAttendanceButton.addActionListener(e -> showAttendanceMarkingPanel());

        viewReportButton = new JButton("View Report");
        viewReportButton.addActionListener(e -> viewAttendanceReport());

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(markAttendanceButton);
        buttonsPanel.add(viewReportButton);

        // Add sub-panels to main panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(datePanel, BorderLayout.NORTH);
        leftPanel.add(filterPanel, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create panel for marking attendance
     */
    private JPanel createAttendanceMarkingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Mark Attendance"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Table panel for attendance marking
        JPanel attendanceTablePanel = new JPanel(new BorderLayout());

        // Scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(createAttendanceTable());
        attendanceTablePanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Attendance");
        saveButton.addActionListener(e -> saveAttendance());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> attendanceMarkingPanel.setVisible(false));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to panel
        panel.add(attendanceTablePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create the attendance marking table
     */
    private JPanel createAttendanceTable() {
        JPanel panel = new JPanel(new GridLayout(0, 4, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add header row
        panel.add(new JLabel("Roll Number", SwingConstants.CENTER));
        panel.add(new JLabel("Name", SwingConstants.CENTER));
        panel.add(new JLabel("Status", SwingConstants.CENTER));
        panel.add(new JLabel("Remarks", SwingConstants.CENTER));

        return panel;
    }

    /**
     * Load available classes into the filter combo box
     */
    private void loadClassesIntoComboBox() {
        try {
            String query = "SELECT DISTINCT class FROM students ORDER BY class";
            ResultSet rs = DatabaseManager.executeQuery(query);

            while (rs.next()) {
                String className = rs.getString("class");
                if (className != null && !className.isEmpty()) {
                    classFilterComboBox.addItem(className);
                }
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading classes: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load students based on selected class filter
     */
    private void loadStudentsForAttendance() {
        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Get student data from database based on selected class
            String selectedClass = (String) classFilterComboBox.getSelectedItem();
            String query;

            if ("All Classes".equals(selectedClass)) {
                query = "SELECT id, roll_number, name, class, section FROM students ORDER BY class, section, roll_number";
            } else {
                query = "SELECT id, roll_number, name, class, section FROM students WHERE class = ? ORDER BY section, roll_number";
            }

            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            if (!"All Classes".equals(selectedClass)) {
                pstmt.setString(1, selectedClass);
            }

            ResultSet rs = pstmt.executeQuery();

            // Get selected date
            String selectedDate = dateField.getText().trim();

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                int studentId = rs.getInt("id");
                row.add(studentId);
                row.add(rs.getString("roll_number"));
                row.add(rs.getString("name"));
                row.add(rs.getString("class"));
                row.add(rs.getString("section"));

                // Check if attendance already exists for this student on this date
                String status = getAttendanceStatus(studentId, selectedDate);
                String remarks = getAttendanceRemarks(studentId, selectedDate);

                row.add(status);
                row.add(remarks);

                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();
            pstmt.close();

            // Update status
            dashboard.setStatusMessage("Loaded " + tableModel.getRowCount() + " students for attendance");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get the attendance status for a student on a specific date
     */
    private String getAttendanceStatus(int studentId, String date) {
        try {
            String query = "SELECT status FROM attendance WHERE student_id = ? AND date = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                rs.close();
                pstmt.close();
                return status;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Not Marked";
    }

    /**
     * Get the attendance remarks for a student on a specific date
     */
    private String getAttendanceRemarks(int studentId, String date) {
        try {
            String query = "SELECT remarks FROM attendance WHERE student_id = ? AND date = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String remarks = rs.getString("remarks");
                rs.close();
                pstmt.close();
                return remarks != null ? remarks : "";
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Show the attendance marking panel
     */
    private void showAttendanceMarkingPanel() {
        // Validate date
        String selectedDate = dateField.getText().trim();
        if (selectedDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date (YYYY-MM-DD)",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if there are students to mark attendance for
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No students found to mark attendance for",
                    "No Students", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear the attendance map
        attendanceStatusMap.clear();

        // Create the attendance marking table content
        JPanel tableContent = (JPanel) ((JScrollPane) attendanceMarkingPanel.getComponent(0)).getViewport().getView();
        tableContent.removeAll();

        // Add header row
        tableContent.add(new JLabel("Roll Number", SwingConstants.CENTER));
        tableContent.add(new JLabel("Name", SwingConstants.CENTER));
        tableContent.add(new JLabel("Status", SwingConstants.CENTER));
        tableContent.add(new JLabel("Remarks", SwingConstants.CENTER));

        // Add student rows
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int studentId = (int) tableModel.getValueAt(row, 0);
            String rollNumber = (String) tableModel.getValueAt(row, 1);
            String name = (String) tableModel.getValueAt(row, 2);
            String currentStatus = (String) tableModel.getValueAt(row, 5);
            String currentRemarks = (String) tableModel.getValueAt(row, 6);

            JLabel rollLabel = new JLabel(rollNumber, SwingConstants.CENTER);
            JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);

            // Status combo box
            String[] statusOptions = {"PRESENT", "ABSENT", "LATE", "EXCUSED"};
            JComboBox<String> statusCombo = new JComboBox<>(statusOptions);

            // Set the current status if already marked
            if (!"Not Marked".equals(currentStatus)) {
                statusCombo.setSelectedItem(currentStatus);
            }

            // Add to map for later retrieval
            attendanceStatusMap.put(studentId, statusCombo);

            // Remarks field
            JTextField remarksField = new JTextField(currentRemarks);
            remarksField.setName("remarks_" + studentId);

            tableContent.add(rollLabel);
            tableContent.add(nameLabel);
            tableContent.add(statusCombo);
            tableContent.add(remarksField);
        }

        // Update the panel and make it visible
        tableContent.revalidate();
        tableContent.repaint();
        attendanceMarkingPanel.setVisible(true);
    }

    /**
     * Save the attendance data
     */
    private void saveAttendance() {
        String selectedDate = dateField.getText().trim();

        try {
            // Begin transaction
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Remove existing attendance records for this date
            String deleteQuery = "DELETE FROM attendance WHERE date = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setString(1, selectedDate);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            // Insert new attendance records
            String insertQuery = "INSERT INTO attendance (student_id, date, status, remarks) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            JPanel tableContent = (JPanel) ((JScrollPane) attendanceMarkingPanel.getComponent(0)).getViewport().getView();

            // Process each student
            for (Map.Entry<Integer, JComboBox<String>> entry : attendanceStatusMap.entrySet()) {
                int studentId = entry.getKey();
                String status = (String) entry.getValue().getSelectedItem();

                // Find the remarks field for this student
                String remarks = "";
                for (Component comp : tableContent.getComponents()) {
                    if (comp instanceof JTextField && comp.getName() != null && comp.getName().equals("remarks_" + studentId)) {
                        remarks = ((JTextField) comp).getText().trim();
                        break;
                    }
                }

                // Insert the attendance record
                insertStmt.setInt(1, studentId);
                insertStmt.setString(2, selectedDate);
                insertStmt.setString(3, status);
                insertStmt.setString(4, remarks);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            insertStmt.close();

            // Commit transaction
            conn.commit();
            conn.setAutoCommit(true);

            // Hide the panel and refresh the data
            attendanceMarkingPanel.setVisible(false);
            loadStudentsForAttendance();

            JOptionPane.showMessageDialog(this, "Attendance saved successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dashboard.setStatusMessage("Attendance saved for " + selectedDate);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);

            try {
                // Rollback transaction on error
                DatabaseManager.getConnection().rollback();
                DatabaseManager.getConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * View attendance report
     */
    private void viewAttendanceReport() {
        // Create a dialog to select the date range
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);

        // Set default date range (current month)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();

        // End date is today
        String endDate = dateFormat.format(cal.getTime());
        endDateField.setText(endDate);

        // Start date is first of the month
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(cal.getTime());
        startDateField.setText(startDate);

        panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        panel.add(startDateField);
        panel.add(new JLabel("End Date (YYYY-MM-DD):"));
        panel.add(endDateField);

        // Add class filter
        JComboBox<String> classFilter = new JComboBox<>();
        classFilter.addItem("All Classes");
        for (int i = 1; i < classFilterComboBox.getItemCount(); i++) {
            classFilter.addItem(classFilterComboBox.getItemAt(i));
        }

        panel.add(new JLabel("Class:"));
        panel.add(classFilter);

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Date Range for Report",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String start = startDateField.getText().trim();
            String end = endDateField.getText().trim();
            String selectedClass = (String) classFilter.getSelectedItem();

            if (start.isEmpty() || end.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both start and end dates",
                        "Missing Dates", JOptionPane.WARNING_MESSAGE);
                return;
            }

            generateAttendanceReport(start, end, selectedClass);
        }
    }

    /**
     * Generate the attendance report for the given date range
     */
    private void generateAttendanceReport(String startDate, String endDate, String classFilter) {
        try {
            // Setup the query based on class filter
            String query;
            PreparedStatement pstmt;

            if ("All Classes".equals(classFilter)) {
                query = """
                        SELECT s.roll_number, s.name, s.class, s.section,
                        COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present_days,
                        COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) as absent_days,
                        COUNT(CASE WHEN a.status = 'LATE' THEN 1 END) as late_days,
                        COUNT(CASE WHEN a.status = 'EXCUSED' THEN 1 END) as excused_days,
                        COUNT(DISTINCT a.date) as total_days
                        FROM students s
                        LEFT JOIN attendance a ON s.id = a.student_id AND a.date BETWEEN ? AND ?
                        GROUP BY s.id, s.roll_number, s.name, s.class, s.section
                        ORDER BY s.class, s.section, s.roll_number
                        """;
                pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, startDate);
                pstmt.setString(2, endDate);
            } else {
                query = """
                        SELECT s.roll_number, s.name, s.class, s.section,
                        COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present_days,
                        COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) as absent_days,
                        COUNT(CASE WHEN a.status = 'LATE' THEN 1 END) as late_days,
                        COUNT(CASE WHEN a.status = 'EXCUSED' THEN 1 END) as excused_days,
                        COUNT(DISTINCT a.date) as total_days
                        FROM students s
                        LEFT JOIN attendance a ON s.id = a.student_id AND a.date BETWEEN ? AND ?
                        WHERE s.class = ?
                        GROUP BY s.id, s.roll_number, s.name, s.class, s.section
                        ORDER BY s.section, s.roll_number
                        """;
                pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, startDate);
                pstmt.setString(2, endDate);
                pstmt.setString(3, classFilter);
            }

            ResultSet rs = pstmt.executeQuery();

            // Create the report content
            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder.append("ATTENDANCE REPORT\n");
            reportBuilder.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
            reportBuilder.append("Class: ").append(classFilter).append("\n\n");
            reportBuilder.append(String.format("%-15s %-25s %-10s %-10s %-15s %-15s %-15s %-15s %-15s\n",
                    "Roll Number", "Name", "Class", "Section", "Present Days", "Absent Days",
                    "Late Days", "Excused Days", "Attendance %"));
            reportBuilder.append("-".repeat(135)).append("\n");

            while (rs.next()) {
                String rollNumber = rs.getString("roll_number");
                String name = rs.getString("name");
                String className = rs.getString("class");
                String section = rs.getString("section");
                int presentDays = rs.getInt("present_days");
                int absentDays = rs.getInt("absent_days");
                int lateDays = rs.getInt("late_days");
                int excusedDays = rs.getInt("excused_days");
                int totalDays = rs.getInt("total_days");

                // Calculate attendance percentage
                double attendancePercentage = totalDays > 0 ?
                        (double) (presentDays + lateDays) / totalDays * 100 : 0;

                reportBuilder.append(String.format("%-15s %-25s %-10s %-10s %-15d %-15d %-15d %-15d %-15.2f%%\n",
                        rollNumber, name, className, section, presentDays, absentDays,
                        lateDays, excusedDays, attendancePercentage));
            }

            rs.close();
            pstmt.close();

            // Display the report
            JTextArea reportArea = new JTextArea(reportBuilder.toString());
            reportArea.setEditable(false);
            reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(reportArea);
            scrollPane.setPreferredSize(new Dimension(800, 500));

            JOptionPane.showMessageDialog(this, scrollPane, "Attendance Report", JOptionPane.INFORMATION_MESSAGE);
            dashboard.setStatusMessage("Generated attendance report");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating attendance report: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
