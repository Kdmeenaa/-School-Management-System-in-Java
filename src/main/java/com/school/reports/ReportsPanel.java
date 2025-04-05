package com.school.reports;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.school.dashboard.AdminDashboard;
import com.school.database.DatabaseManager;

/**
 * Panel for generating and managing reports in the School Management System
 */
public class ReportsPanel extends JPanel {
    private AdminDashboard dashboard;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeComboBox;
    private JButton generateButton, exportPdfButton, exportExcelButton;
    private String currentReportType = "Students";

    /**
     * Constructor for ReportsPanel
     * @param dashboard Reference to the AdminDashboard for status updates
     */
    public ReportsPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize components
        initComponents();

        // Load initial report (Students by default)
        generateReport();
    }

    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Top panel for report controls
        JPanel topPanel = createTopPanel();

        // Table panel for displaying report data
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Report Data"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create the table model (columns will be set based on report type)
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create the table and set properties
        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        reportTable.getTableHeader().setReorderingAllowed(false);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /**
     * Create the top panel with report type selection and export buttons
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Report type selection panel
        JPanel reportTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypePanel.add(new JLabel("Report Type:"));

        reportTypeComboBox = new JComboBox<>(new String[] {
                "Students", "Teachers", "Fee Collection", "Attendance Summary", "Classroom Distribution"
        });
        reportTypeComboBox.addActionListener(e -> {
            currentReportType = (String) reportTypeComboBox.getSelectedItem();
            generateReport();
        });
        reportTypePanel.add(reportTypeComboBox);

        // Generate button
        generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        reportTypePanel.add(generateButton);

        // Export buttons panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        exportPdfButton = new JButton("Export as PDF");
        exportPdfButton.addActionListener(e -> exportToPdf());

        exportExcelButton = new JButton("Export as Excel");
        exportExcelButton.addActionListener(e -> exportToExcel());

        exportPanel.add(exportPdfButton);
        exportPanel.add(exportExcelButton);

        // Add sub-panels to main panel
        panel.add(reportTypePanel, BorderLayout.WEST);
        panel.add(exportPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Generate the selected report
     */
    private void generateReport() {
        switch (currentReportType) {
            case "Students":
                generateStudentReport();
                break;
            case "Teachers":
                generateTeacherReport();
                break;
            case "Fee Collection":
                generateFeeCollectionReport();
                break;
            case "Attendance Summary":
                generateAttendanceSummaryReport();
                break;
            case "Classroom Distribution":
                generateClassroomDistributionReport();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown report type: " + currentReportType,
                        "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate the student report
     */
    private void generateStudentReport() {
        try {
            // Set up columns for student report
            String[] columns = {
                    "ID", "Name", "Roll Number", "Class", "Section", "Parent Name", "Parent Phone", "Join Date"
            };
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            // Query to get all students
            String query = "SELECT * FROM students ORDER BY class, section, roll_number";
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

            rs.close();

            // Update status
            dashboard.setStatusMessage("Generated student report with " + tableModel.getRowCount() + " records");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating student report: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate the teacher report
     */
    private void generateTeacherReport() {
        try {
            // Set up columns for teacher report
            String[] columns = {
                    "ID", "Name", "Email", "Phone", "Subject", "Salary", "Join Date"
            };
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            // Query to get all teachers
            String query = "SELECT * FROM teachers ORDER BY name";
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

            rs.close();

            // Update status
            dashboard.setStatusMessage("Generated teacher report with " + tableModel.getRowCount() + " records");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating teacher report: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate the fee collection report
     */
    private void generateFeeCollectionReport() {
        try {
            // Set up columns for fee collection report
            String[] columns = {
                    "Student Name", "Roll Number", "Class", "Amount", "Payment Date", "Status", "Payment Method"
            };
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            // Query to get fee collection data
            String query = """
                    SELECT s.name as student_name, s.roll_number, s.class, f.amount,
                    f.payment_date, f.status, f.payment_method
                    FROM fees f
                    JOIN students s ON f.student_id = s.id
                    ORDER BY f.payment_date DESC
                    """;

            ResultSet rs = DatabaseManager.executeQuery(query);

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("student_name"));
                row.add(rs.getString("roll_number"));
                row.add(rs.getString("class"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getString("payment_date"));
                row.add(rs.getString("status"));
                row.add(rs.getString("payment_method"));
                tableModel.addRow(row);
            }

            rs.close();

            // Update status
            dashboard.setStatusMessage("Generated fee collection report with " + tableModel.getRowCount() + " records");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating fee collection report: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate the attendance summary report
     */
    private void generateAttendanceSummaryReport() {
        try {
            // Set up columns for attendance summary report
            String[] columns = {
                    "Student Name", "Roll Number", "Class", "Section", "Present Days", "Absent Days",
                    "Late Days", "Excused Days", "Attendance %"
            };
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            // Query to get attendance summary data
            String query = """
                    SELECT s.name, s.roll_number, s.class, s.section,
                    COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present_days,
                    COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END) as absent_days,
                    COUNT(CASE WHEN a.status = 'LATE' THEN 1 END) as late_days,
                    COUNT(CASE WHEN a.status = 'EXCUSED' THEN 1 END) as excused_days,
                    COUNT(a.date) as total_days
                    FROM students s
                    LEFT JOIN attendance a ON s.id = a.student_id
                    GROUP BY s.id, s.name, s.roll_number, s.class, s.section
                    ORDER BY s.class, s.section, s.roll_number
                    """;

            ResultSet rs = DatabaseManager.executeQuery(query);

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("name"));
                row.add(rs.getString("roll_number"));
                row.add(rs.getString("class"));
                row.add(rs.getString("section"));

                int presentDays = rs.getInt("present_days");
                int absentDays = rs.getInt("absent_days");
                int lateDays = rs.getInt("late_days");
                int excusedDays = rs.getInt("excused_days");
                int totalDays = rs.getInt("total_days");

                row.add(presentDays);
                row.add(absentDays);
                row.add(lateDays);
                row.add(excusedDays);

                // Calculate attendance percentage
                double attendancePercentage = totalDays > 0 ?
                        (double) (presentDays + lateDays) / totalDays * 100 : 0;
                row.add(String.format("%.2f%%", attendancePercentage));

                tableModel.addRow(row);
            }

            rs.close();

            // Update status
            dashboard.setStatusMessage("Generated attendance summary report with " + tableModel.getRowCount() + " records");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating attendance summary report: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate the classroom distribution report
     */
    private void generateClassroomDistributionReport() {
        try {
            // Set up columns for classroom distribution report
            String[] columns = {
                    "Class", "Section", "Number of Students", "Boys", "Girls"
            };
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            // Query to get classroom distribution data
            String query = """
                    SELECT class, section, COUNT(*) as total_students
                    FROM students
                    GROUP BY class, section
                    ORDER BY class, section
                    """;

            ResultSet rs = DatabaseManager.executeQuery(query);

            // Add rows to the table model
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("class"));
                row.add(rs.getString("section"));
                row.add(rs.getInt("total_students"));

                // For boys and girls count, we'll use dummy data since we don't have gender in student table
                // In a real system, you would query this from the database
                row.add("N/A");
                row.add("N/A");

                tableModel.addRow(row);
            }

            rs.close();

            // Update status
            dashboard.setStatusMessage("Generated classroom distribution report with " + tableModel.getRowCount() + " records");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating classroom distribution report: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Export the current report data to PDF
     */
    private void exportToPdf() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export",
                    "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Choose file to save PDF
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new File(currentReportType.replace(" ", "_") + "_Report.pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();

        try {
            // Create a document with page settings
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            // Add title
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph(currentReportType + " Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add date
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);
            Paragraph date = new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            document.add(new Paragraph(" ")); // Add space

            // Create table
            PdfPTable table = new PdfPTable(tableModel.getColumnCount());
            table.setWidthPercentage(100);

            // Add header row
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(i)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Add data rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    table.addCell(value != null ? value.toString() : "");
                }
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(this, "PDF exported successfully",
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            dashboard.setStatusMessage("Exported " + currentReportType + " report to PDF");

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Export the current report data to Excel
     */
    private void exportToExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export",
                    "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Choose file to save Excel
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel Report");
        fileChooser.setSelectedFile(new File(currentReportType.replace(" ", "_") + "_Report.xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create sheet
            Sheet sheet = workbook.createSheet(currentReportType + " Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(tableModel.getColumnName(i));
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Row dataRow = sheet.createRow(row + 1);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Cell cell = dataRow.createCell(col);
                    Object value = tableModel.getValueAt(row, col);

                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof Double) {
                        cell.setCellValue((Double) value);
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // Auto size columns
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                workbook.write(fileOut);
            }

            JOptionPane.showMessageDialog(this, "Excel exported successfully",
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            dashboard.setStatusMessage("Exported " + currentReportType + " report to Excel");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to Excel: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
