package com.school.teacher;

import com.school.dashboard.AdminDashboard;
import com.school.database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel for managing teachers in the School Management System
 */
public class TeacherManagementPanel extends JPanel {
    private AdminDashboard dashboard;
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, searchButton, clearButton;
    private JPanel detailsPanel;

    // Teacher form fields
    private JTextField nameField, emailField, phoneField, addressField, subjectField;
    private JTextField salaryField, joinDateField;
    private JButton saveButton, cancelButton;

    private boolean isEditing = false;
    private int editingTeacherId = -1;

    /**
     * Constructor for TeacherManagementPanel
     * @param dashboard Reference to the AdminDashboard for status updates
     */
    public TeacherManagementPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        initComponents();

        // Load teacher data
        loadTeacherData();
    }

    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Table panel for displaying teachers
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Teachers"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create the table model with column names
        String[] columnNames = {
                "ID", "Name", "Email", "Phone", "Subject", "Salary", "Join Date"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create the table and set properties
        teacherTable = new JTable(tableModel);
        teacherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        teacherTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        teacherTable.getTableHeader().setReorderingAllowed(false);

        // Hide the ID column
        teacherTable.getColumnModel().getColumn(0).setMinWidth(0);
        teacherTable.getColumnModel().getColumn(0).setMaxWidth(0);
        teacherTable.getColumnModel().getColumn(0).setWidth(0);

        // Double click on row to edit
        teacherTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = teacherTable.getSelectedRow();
                    if (selectedRow != -1) {
                        editTeacher();
                    }
                }
            }
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(teacherTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Action button panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");

        searchButton.addActionListener(e -> searchTeachers());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadTeacherData();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Teacher");
        editButton = new JButton("Edit Teacher");
        deleteButton = new JButton("Delete Teacher");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addTeacher());
        editButton.addActionListener(e -> editTeacher());
        deleteButton.addActionListener(e -> deleteTeacher());
        refreshButton.addActionListener(e -> loadTeacherData());

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(refreshButton);

        actionPanel.add(searchPanel);
        actionPanel.add(buttonsPanel);
        tablePanel.add(actionPanel, BorderLayout.NORTH);

        // Teacher details panel (initially hidden)
        detailsPanel = createDetailsPanel();
        detailsPanel.setVisible(false);

        // Add panels to main panel
        add(tablePanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the details panel for adding/editing teachers
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Teacher Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Form panel with GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Email field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        // Phone field
        gbc.gridx = 2;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        phoneField = new JTextField(15);
        formPanel.add(phoneField, gbc);

        // Subject field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        subjectField = new JTextField(20);
        formPanel.add(subjectField, gbc);

        // Salary field
        gbc.gridx = 2;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Salary:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        salaryField = new JTextField(10);
        formPanel.add(salaryField, gbc);

        // Address field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        addressField = new JTextField(50);
        formPanel.add(addressField, gbc);

        // Join Date field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Join Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        joinDateField = new JTextField(10);
        formPanel.add(joinDateField, gbc);

        // Add form panel to details panel
        panel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveTeacher());
        cancelButton.addActionListener(e -> detailsPanel.setVisible(false));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Load teacher data from database into the table
     */
    private void loadTeacherData() {
        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Get teacher data from database
            String query = "SELECT id, name, email, phone, subject, salary, join_date FROM teachers ORDER BY name";
            ResultSet rs = DatabaseManager.executeQuery(query);

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("subject"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getString("join_date"));
                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();

            // Update status
            dashboard.setStatusMessage("Loaded " + tableModel.getRowCount() + " teachers");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading teacher data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Search for teachers based on search field text
     */
    private void searchTeachers() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            loadTeacherData();
            return;
        }

        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Search for teachers
            String query = "SELECT id, name, email, phone, subject, salary, join_date " +
                    "FROM teachers " +
                    "WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR subject LIKE ?";

            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 4; i++) {
                pstmt.setString(i, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("subject"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getString("join_date"));
                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();
            pstmt.close();

            // Update status
            dashboard.setStatusMessage("Found " + tableModel.getRowCount() + " teachers matching '" + searchText + "'");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching for teachers: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show form for adding a new teacher
     */
    private void addTeacher() {
        isEditing = false;
        editingTeacherId = -1;

        // Clear form fields
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        subjectField.setText("");
        salaryField.setText("");

        // Set current date as default join date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        joinDateField.setText(dateFormat.format(new java.util.Date()));

        // Show details panel
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add Teacher"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        detailsPanel.setVisible(true);
    }

    /**
     * Show form for editing an existing teacher
     */
    private void editTeacher() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        isEditing = true;
        editingTeacherId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            // Get teacher data from database
            String query = "SELECT * FROM teachers WHERE id = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            pstmt.setInt(1, editingTeacherId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Fill form fields with teacher data
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                addressField.setText(rs.getString("address"));
                subjectField.setText(rs.getString("subject"));
                salaryField.setText(String.valueOf(rs.getDouble("salary")));
                joinDateField.setText(rs.getString("join_date"));

                // Show details panel
                detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Edit Teacher"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                detailsPanel.setVisible(true);
            }

            // Close result set and statement
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading teacher data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete the selected teacher
     */
    private void deleteTeacher() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int teacherId = (int) tableModel.getValueAt(selectedRow, 0);
        String teacherName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete teacher: " + teacherName + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                // Delete teacher from database
                String query = "DELETE FROM teachers WHERE id = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setInt(1, teacherId);
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    // Remove from table
                    tableModel.removeRow(selectedRow);
                    dashboard.setStatusMessage("Teacher deleted successfully");
                    dashboard.refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete teacher",
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting teacher: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Save teacher data (for both add and edit operations)
     */
    private void saveTeacher() {
        // Validate form fields
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is a required field",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate salary is a number
        double salary = 0.0;
        try {
            if (!salaryField.getText().trim().isEmpty()) {
                salary = Double.parseDouble(salaryField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Salary must be a valid number",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (isEditing) {
                // Update existing teacher
                String query = "UPDATE teachers SET name = ?, email = ?, phone = ?, address = ?, " +
                        "subject = ?, salary = ?, join_date = ? WHERE id = ?";

                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, emailField.getText().trim());
                pstmt.setString(3, phoneField.getText().trim());
                pstmt.setString(4, addressField.getText().trim());
                pstmt.setString(5, subjectField.getText().trim());
                pstmt.setDouble(6, salary);
                pstmt.setString(7, joinDateField.getText().trim());
                pstmt.setInt(8, editingTeacherId);

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Teacher updated successfully",
                            "Update Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.setStatusMessage("Teacher updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update teacher",
                            "Update Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Check if email already exists (if provided)
                if (!emailField.getText().trim().isEmpty()) {
                    String checkQuery = "SELECT COUNT(*) FROM teachers WHERE email = ?";
                    PreparedStatement checkStmt = DatabaseManager.getConnection().prepareStatement(checkQuery);
                    checkStmt.setString(1, emailField.getText().trim());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Email already exists. Please use a different email.",
                                "Duplicate Email", JOptionPane.WARNING_MESSAGE);
                        rs.close();
                        checkStmt.close();
                        return;
                    }

                    rs.close();
                    checkStmt.close();
                }

                // Add new teacher
                String query = "INSERT INTO teachers (name, email, phone, address, subject, salary, join_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, emailField.getText().trim());
                pstmt.setString(3, phoneField.getText().trim());
                pstmt.setString(4, addressField.getText().trim());
                pstmt.setString(5, subjectField.getText().trim());
                pstmt.setDouble(6, salary);
                pstmt.setString(7, joinDateField.getText().trim());

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Teacher added successfully",
                            "Add Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.setStatusMessage("Teacher added successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add teacher",
                            "Add Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Refresh the data and hide the details panel
            loadTeacherData();
            detailsPanel.setVisible(false);
            dashboard.refreshDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving teacher: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
