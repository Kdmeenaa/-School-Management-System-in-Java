package com.school.fees;

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
import java.util.Date;

/**
 * Panel for managing student fees in the School Management System
 */
public class FeesManagementPanel extends JPanel {
    private AdminDashboard dashboard;
    private JTable feesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, searchButton, clearButton;
    private JButton generateReceiptButton;
    private JPanel detailsPanel;

    // Fees form fields
    private JComboBox<StudentComboItem> studentComboBox;
    private JTextField amountField, dueDateField, paymentDateField;
    private JComboBox<String> statusComboBox, paymentMethodComboBox;
    private JTextField receiptNumberField;
    private JButton saveButton, cancelButton;

    private boolean isEditing = false;
    private int editingFeeId = -1;

    /**
     * Constructor for FeesManagementPanel
     * @param dashboard Reference to the AdminDashboard for status updates
     */
    public FeesManagementPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        initComponents();

        // Load fees data
        loadFeesData();
    }

    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Table panel for displaying fees
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Student Fees"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create the table model with column names
        String[] columnNames = {
                "ID", "Student Name", "Roll Number", "Amount", "Due Date",
                "Payment Date", "Status", "Payment Method", "Receipt Number"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create the table and set properties
        feesTable = new JTable(tableModel);
        feesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        feesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        feesTable.getTableHeader().setReorderingAllowed(false);

        // Hide the ID column
        feesTable.getColumnModel().getColumn(0).setMinWidth(0);
        feesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        feesTable.getColumnModel().getColumn(0).setWidth(0);

        // Double click on row to edit
        feesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = feesTable.getSelectedRow();
                    if (selectedRow != -1) {
                        editFee();
                    }
                }
            }
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(feesTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Action button panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");

        searchButton.addActionListener(e -> searchFees());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadFeesData();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Fee");
        editButton = new JButton("Edit Fee");
        deleteButton = new JButton("Delete Fee");
        refreshButton = new JButton("Refresh");
        generateReceiptButton = new JButton("Generate Receipt");

        addButton.addActionListener(e -> addFee());
        editButton.addActionListener(e -> editFee());
        deleteButton.addActionListener(e -> deleteFee());
        refreshButton.addActionListener(e -> loadFeesData());
        generateReceiptButton.addActionListener(e -> generateReceipt());

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(generateReceiptButton);

        actionPanel.add(searchPanel);
        actionPanel.add(buttonsPanel);
        tablePanel.add(actionPanel, BorderLayout.NORTH);

        // Fee details panel (initially hidden)
        detailsPanel = createDetailsPanel();
        detailsPanel.setVisible(false);

        // Add panels to main panel
        add(tablePanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the details panel for adding/editing fees
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Fee Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Form panel with GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Student selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Student:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        studentComboBox = new JComboBox<>();
        loadStudentsIntoComboBox();
        formPanel.add(studentComboBox, gbc);

        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        amountField = new JTextField(10);
        formPanel.add(amountField, gbc);

        // Due Date field
        gbc.gridx = 2;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        dueDateField = new JTextField(10);
        // Set default due date to a month from now
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dueDateField.setText(dateFormat.format(cal.getTime()));
        formPanel.add(dueDateField, gbc);

        // Status field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        String[] statusOptions = {"PENDING", "PAID", "OVERDUE"};
        statusComboBox = new JComboBox<>(statusOptions);
        formPanel.add(statusComboBox, gbc);

        // Payment Date field
        gbc.gridx = 2;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Payment Date:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        paymentDateField = new JTextField(10);
        formPanel.add(paymentDateField, gbc);

        // Payment Method field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Payment Method:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        String[] paymentOptions = {"", "CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "CHEQUE"};
        paymentMethodComboBox = new JComboBox<>(paymentOptions);
        formPanel.add(paymentMethodComboBox, gbc);

        // Receipt Number field
        gbc.gridx = 2;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Receipt Number:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 3;
        receiptNumberField = new JTextField(10);
        formPanel.add(receiptNumberField, gbc);

        // Add form panel to details panel
        panel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveFee());
        cancelButton.addActionListener(e -> detailsPanel.setVisible(false));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Load students into the combo box
     */
    private void loadStudentsIntoComboBox() {
        studentComboBox.removeAllItems();

        try {
            String query = "SELECT id, name, roll_number FROM students ORDER BY name";
            ResultSet rs = DatabaseManager.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String rollNumber = rs.getString("roll_number");
                StudentComboItem item = new StudentComboItem(id, name, rollNumber);
                studentComboBox.addItem(item);
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load fees data from database into the table
     */
    private void loadFeesData() {
        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Get fees data from database with student information
            String query = """
                    SELECT f.id, f.student_id, s.name as student_name, s.roll_number,
                    f.amount, f.due_date, f.payment_date, f.status, f.payment_method, f.receipt_number
                    FROM fees f
                    JOIN students s ON f.student_id = s.id
                    ORDER BY f.due_date DESC
                    """;

            ResultSet rs = DatabaseManager.executeQuery(query);

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("student_name"));
                row.add(rs.getString("roll_number"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getString("due_date"));
                row.add(rs.getString("payment_date"));
                row.add(rs.getString("status"));
                row.add(rs.getString("payment_method"));
                row.add(rs.getString("receipt_number"));
                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();

            // Update status
            dashboard.setStatusMessage("Loaded " + tableModel.getRowCount() + " fee records");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fee data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Search for fees based on search field text
     */
    private void searchFees() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            loadFeesData();
            return;
        }

        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Search for fees
            String query = """
                    SELECT f.id, f.student_id, s.name as student_name, s.roll_number,
                    f.amount, f.due_date, f.payment_date, f.status, f.payment_method, f.receipt_number
                    FROM fees f
                    JOIN students s ON f.student_id = s.id
                    WHERE s.name LIKE ? OR s.roll_number LIKE ? OR f.status LIKE ?
                    OR f.payment_method LIKE ? OR f.receipt_number LIKE ?
                    ORDER BY f.due_date DESC
                    """;

            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("student_name"));
                row.add(rs.getString("roll_number"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getString("due_date"));
                row.add(rs.getString("payment_date"));
                row.add(rs.getString("status"));
                row.add(rs.getString("payment_method"));
                row.add(rs.getString("receipt_number"));
                tableModel.addRow(row);
            }

            // Close the result set
            rs.close();
            pstmt.close();

            // Update status
            dashboard.setStatusMessage("Found " + tableModel.getRowCount() + " fee records matching '" + searchText + "'");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching for fees: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show form for adding a new fee
     */
    private void addFee() {
        isEditing = false;
        editingFeeId = -1;

        // Reset form fields
        if (studentComboBox.getItemCount() > 0) {
            studentComboBox.setSelectedIndex(0);
        }
        amountField.setText("");

        // Set default due date to a month from now
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dueDateField.setText(dateFormat.format(cal.getTime()));

        paymentDateField.setText("");
        statusComboBox.setSelectedItem("PENDING");
        paymentMethodComboBox.setSelectedIndex(0);
        receiptNumberField.setText("");

        // Show details panel
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add Fee"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        detailsPanel.setVisible(true);
    }

    /**
     * Show form for editing an existing fee
     */
    private void editFee() {
        int selectedRow = feesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fee record to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        isEditing = true;
        editingFeeId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            // Get fee data from database
            String query = "SELECT * FROM fees WHERE id = ?";
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
            pstmt.setInt(1, editingFeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Select student in combo box
                int studentId = rs.getInt("student_id");
                for (int i = 0; i < studentComboBox.getItemCount(); i++) {
                    StudentComboItem item = studentComboBox.getItemAt(i);
                    if (item.getId() == studentId) {
                        studentComboBox.setSelectedIndex(i);
                        break;
                    }
                }

                // Fill form fields with fee data
                amountField.setText(String.valueOf(rs.getDouble("amount")));
                dueDateField.setText(rs.getString("due_date"));
                paymentDateField.setText(rs.getString("payment_date"));
                statusComboBox.setSelectedItem(rs.getString("status"));

                String paymentMethod = rs.getString("payment_method");
                if (paymentMethod != null && !paymentMethod.isEmpty()) {
                    paymentMethodComboBox.setSelectedItem(paymentMethod);
                } else {
                    paymentMethodComboBox.setSelectedIndex(0);
                }

                receiptNumberField.setText(rs.getString("receipt_number"));

                // Show details panel
                detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Edit Fee"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                detailsPanel.setVisible(true);
            }

            // Close result set and statement
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fee data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete the selected fee
     */
    private void deleteFee() {
        int selectedRow = feesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fee record to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int feeId = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the fee record for student: " + studentName + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                // Delete fee from database
                String query = "DELETE FROM fees WHERE id = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setInt(1, feeId);
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    // Remove from table
                    tableModel.removeRow(selectedRow);
                    dashboard.setStatusMessage("Fee record deleted successfully");
                    dashboard.refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete fee record",
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting fee record: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Save fee data (for both add and edit operations)
     */
    private void saveFee() {
        // Validate form fields
        if (studentComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a student",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (amountField.getText().trim().isEmpty() || dueDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount and Due Date are required fields",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate amount is a number
        double amount = 0.0;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get selected student
        StudentComboItem selectedStudent = (StudentComboItem) studentComboBox.getSelectedItem();
        int studentId = selectedStudent.getId();

        try {
            if (isEditing) {
                // Update existing fee
                String query = "UPDATE fees SET student_id = ?, amount = ?, due_date = ?, payment_date = ?, " +
                        "status = ?, payment_method = ?, receipt_number = ? WHERE id = ?";

                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setInt(1, studentId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, dueDateField.getText().trim());
                pstmt.setString(4, paymentDateField.getText().trim());
                pstmt.setString(5, (String) statusComboBox.getSelectedItem());
                pstmt.setString(6, (String) paymentMethodComboBox.getSelectedItem());
                pstmt.setString(7, receiptNumberField.getText().trim());
                pstmt.setInt(8, editingFeeId);

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Fee record updated successfully",
                            "Update Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.setStatusMessage("Fee record updated successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update fee record",
                            "Update Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Add new fee
                String query = "INSERT INTO fees (student_id, amount, due_date, payment_date, status, " +
                        "payment_method, receipt_number) VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setInt(1, studentId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, dueDateField.getText().trim());
                pstmt.setString(4, paymentDateField.getText().trim());
                pstmt.setString(5, (String) statusComboBox.getSelectedItem());
                pstmt.setString(6, (String) paymentMethodComboBox.getSelectedItem());
                pstmt.setString(7, receiptNumberField.getText().trim());

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Fee record added successfully",
                            "Add Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.setStatusMessage("Fee record added successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add fee record",
                            "Add Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Refresh the data and hide the details panel
            loadFeesData();
            detailsPanel.setVisible(false);
            dashboard.refreshDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving fee record: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate a receipt for the selected fee
     */
    private void generateReceipt() {
        int selectedRow = feesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fee record to generate receipt",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) tableModel.getValueAt(selectedRow, 6);
        if (!"PAID".equals(status)) {
            JOptionPane.showMessageDialog(this, "Can only generate receipts for PAID fees",
                    "Fee Not Paid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get fee and student details
        int feeId = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 1);
        String rollNumber = (String) tableModel.getValueAt(selectedRow, 2);
        double amount = (double) tableModel.getValueAt(selectedRow, 3);
        String paymentDate = (String) tableModel.getValueAt(selectedRow, 5);
        String paymentMethod = (String) tableModel.getValueAt(selectedRow, 7);
        String receiptNumber = (String) tableModel.getValueAt(selectedRow, 8);

        // If receipt number is empty, generate one
        if (receiptNumber == null || receiptNumber.isEmpty()) {
            receiptNumber = "RCPT-" + feeId + "-" + System.currentTimeMillis();
            try {
                // Update the receipt number in the database
                String query = "UPDATE fees SET receipt_number = ? WHERE id = ?";
                PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(query);
                pstmt.setString(1, receiptNumber);
                pstmt.setInt(2, feeId);
                pstmt.executeUpdate();
                pstmt.close();

                // Refresh the data
                loadFeesData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating receipt number: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Generate and display the receipt
        StringBuilder receiptBuilder = new StringBuilder();
        receiptBuilder.append("SCHOOL MANAGEMENT SYSTEM\n\n");
        receiptBuilder.append("PAYMENT RECEIPT\n\n");
        receiptBuilder.append("Receipt Number: ").append(receiptNumber).append("\n");
        receiptBuilder.append("Date: ").append(paymentDate).append("\n\n");
        receiptBuilder.append("Student Name: ").append(studentName).append("\n");
        receiptBuilder.append("Roll Number: ").append(rollNumber).append("\n\n");
        receiptBuilder.append("Payment Amount: $").append(String.format("%.2f", amount)).append("\n");
        receiptBuilder.append("Payment Method: ").append(paymentMethod).append("\n\n");
        receiptBuilder.append("Thank you for your payment!\n");

        // Show the receipt
        JTextArea receiptArea = new JTextArea(receiptBuilder.toString());
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Payment Receipt", JOptionPane.INFORMATION_MESSAGE);
        dashboard.setStatusMessage("Receipt generated for " + studentName);
    }

    /**
     * Class to represent a student in the combo box
     */
    private class StudentComboItem {
        private int id;
        private String name;
        private String rollNumber;

        public StudentComboItem(int id, String name, String rollNumber) {
            this.id = id;
            this.name = name;
            this.rollNumber = rollNumber;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRollNumber() {
            return rollNumber;
        }

        @Override
        public String toString() {
            return name + " (" + rollNumber + ")";
        }
    }
}
