package com.school.student;

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
 * Panel for managing students in the School Management System
 */
public class StudentManagementPanel extends JPanel {
    private AdminDashboard dashboard;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, searchButton, clearButton;
    private JPanel detailsPanel;

    // Student form fields
    private JTextField nameField, rollNumberField, classField, sectionField, parentNameField;
    private JTextField parentPhoneField, addressField;
    private JTextField joinDateField;
    private JButton saveButton, cancelButton;

    private boolean isEditing = false;
    private int editingStudentId = -1;

    /**
     * Constructor for StudentManagementPanel
     * @param dashboard Reference to the AdminDashboard for status updates
     */
    public StudentManagementPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        initComponents();

        // Load student data
        loadStudentData();
    }

    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Table panel for displaying students
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Students"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create the table model with column names
        String[] columnNames = {
                "ID", "Name", "Roll Number", "Class", "Section",
                "Parent Name", "Parent Phone", "Join Date"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
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

        // Double click on row to edit
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = studentTable.getSelectedRow();
                    if (selectedRow != -1) {
                        editStudent();
                    }
                }
            }
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Action button panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");

        searchButton.addActionListener(e -> searchStudents());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadStudentData();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Student");
        editButton = new JButton("Edit Student");
        deleteButton = new JButton("Delete Student");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addStudent());
        editButton.addActionListener(e -> editStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        refreshButton.addActionListener(e -> loadStudentData());

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(refreshButton);

        actionPanel.add(searchPanel);
        actionPanel.add(buttonsPanel);
        tablePanel.add(actionPanel, BorderLayout.NORTH);

        // Student details panel (initially hidden)
        detailsPanel = createDetailsPanel();
        detailsPanel.setVisible(false);

        // Add panels to main panel
        add(tablePanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the details panel for adding/editing students
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Student Details"),
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

        // Roll Number field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Roll Number:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        rollNumberField = new JTextField(20);
        formPanel.add(rollNumberField, gbc);

        // Class field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Class:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        classField = new JTextField(20);
        formPanel.add(classField, gbc);

        // Section field
        gbc.gridx = 2;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Section:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        sectionField = new JTextField(10);
        formPanel.add(sectionField, gbc);

        // Parent Name field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Parent Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        parentNameField = new JTextField(20);
        formPanel.add(parentNameField, gbc);

        // Parent Phone field
        gbc.gridx = 2;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Parent Phone:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 3;
        parentPhoneField = new JTextField(15);
        formPanel.add(parentPhoneField, gbc);

        // Address field
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        addressField = new JTextField(50);
        formPanel.add(addressField, gbc);

        // Join Date field
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Join Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        joinDateField = new JTextField(10);
        formPanel.add(joinDateField, gbc);

        // Add form panel to details panel
        panel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveStudent());
        cancelButton.addActionListener(e -> detailsPanel.setVisible(false));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Load student data from database into the table
     */
    private void loadStudentData() {
        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Get student data from database
            String query = "SELECT id, name, roll_number, class, section, parent_name, parent_phone, join_date FROM students ORDER BY name";
            ResultSet rs = DatabaseManager.executeQuery(query);

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("roll_number"));
                row.add(rs.getString("class"));
                row.add(rs.getString("section"));
                row.add(rs.getString("parent_name"));
                row.add(rs.getString("parent_phone"));
                row.add(rs.getString("join_date"));
                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();

            // Update status
            dashboard.setStatusMessage("Loaded " + tableModel.getRowCount() + " students");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Search for students based on search field text
     */
    private void searchStudents() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            loadStudentData();
            return;
        }

        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Search for students
            String query = "SELECT id, name, roll_number, class, section, parent_name, parent_phone, join_date " +
                    "FROM students " +
                    "WHERE name LIKE ? OR roll_number LIKE ? OR class LIKE ? OR section LIKE ? " +
                    "OR parent_name LIKE ? OR parent_phone LIKE ?";

            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("roll_number"));
                row.add(rs.getString("class"));
                row.add(rs.getString("section"));
                row.add(rs.getString("parent_name"));
                row.add(rs.getString("parent_phone"));
                row.add(rs.getString("join_date"));
                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();
            pstmt.close();

            // Update status
            dashboard.setStatusMessage("Found " + tableModel.getRowCount() + " students matching '" + searchText + "'");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching for students: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show form for adding a new student
     */
    private void addStudent() {
        isEditing = false;
        editingStudentId = -1;

        // Clear form fields
        nameField.setText("");
        rollNumberField.setText("");
        classField.setText("");
        sectionField.setText("");
        parentNameField.setText("");
        parentPhoneField.setText("");
        addressField.setText("");

        // Set current date as default join date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        joinDateField.setText(dateFormat.format(new java.util.Date()));

        // Show details panel
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add Student"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        detailsPanel.setVisible(true);
    }

    /**
     * Show form for editing an existing student
     */
    private void editStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        isEditing = true;
        editingStudentId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            // Get student data from database
            String query = "SELECT * FROM students WHERE id = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            pstmt.setInt(1, editingStudentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Fill form fields with student data
                nameField.setText(rs.getString("name"));
                rollNumberField.setText(rs.getString("roll_number"));
                classField.setText(rs.getString("class"));
                sectionField.setText(rs.getString("section"));
                parentNameField.setText(rs.getString("parent_name"));
                parentPhoneField.setText(rs.getString("parent_phone"));
                addressField.setText(rs.getString("address"));
                joinDateField.setText(rs.getString("join_date"));

                // Show details panel
                detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Edit Student"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                detailsPanel.setVisible(true);
            }

            // Close result set and statement
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete the selected student
     */
    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student: " + studentName + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                // Delete student from database
                String query = "DELETE FROM students WHERE id = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setInt(1, studentId);
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    // Remove from table
                    tableModel.removeRow(selectedRow);
                    dashboard.setStatusMessage("Student deleted successfully");
                    dashboard.refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete student",
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting student: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Save student data (for both add and edit operations)
     */
    private void saveStudent() {
        // Validate form fields
        if (nameField.getText().trim().isEmpty() || rollNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Roll Number are required fields",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (isEditing) {
                // Update existing student
                String query = "UPDATE students SET name = ?, roll_number = ?, class = ?, section = ?, " +
                        "parent_name = ?, parent_phone = ?, address = ?, join_date = ? WHERE id = ?";

                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, rollNumberField.getText().trim());
                pstmt.setString(3, classField.getText().trim());
                pstmt.setString(4, sectionField.getText().trim());
                pstmt.setString(5, parentNameField.getText().trim());
                pstmt.setString(6, parentPhoneField.getText().trim());
                pstmt.setString(7, addressField.getText().trim());
                pstmt.setString(8, joinDateField.getText().trim());
                pstmt.setInt(9, editingStudentId);

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Student updated successfully",
                            "Update Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.setStatusMessage("Student updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update student",
                            "Update Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Check if roll number already exists
                String checkQuery = "SELECT COUNT(*) FROM students WHERE roll_number = ?";
                PreparedStatement checkStmt = DatabaseManager.getConnection().prepareStatement(checkQuery);
                checkStmt.setString(1, rollNumberField.getText().trim());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Roll Number already exists. Please use a different roll number.",
                            "Duplicate Roll Number", JOptionPane.WARNING_MESSAGE);
                    rs.close();
                    checkStmt.close();
                    return;
                }

                rs.close();
                checkStmt.close();

                // Add new student
                String query = "INSERT INTO students (name, roll_number, class, section, parent_name, parent_phone, " +
                        "address, join_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, rollNumberField.getText().trim());
                pstmt.setString(3, classField.getText().trim());
                pstmt.setString(4, sectionField.getText().trim());
                pstmt.setString(5, parentNameField.getText().trim());
                pstmt.setString(6, parentPhoneField.getText().trim());
                pstmt.setString(7, addressField.getText().trim());
                pstmt.setString(8, joinDateField.getText().trim());

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Student added successfully",
                            "Add Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.setStatusMessage("Student added successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add student",
                            "Add Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Refresh the data and hide the details panel
            loadStudentData();
            detailsPanel.setVisible(false);
            dashboard.refreshDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving student: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
