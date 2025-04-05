package com.school.database;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager handles all database operations for the School Management System.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:school_management.db";
    private static Connection connection;

    /**
     * Initialize the database and create tables if they don't exist
     */
    public static void initialize() {
        try {
            // Create a connection to the database
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Database connection established.");

            // Create tables if they don't exist
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    /**
     * Get the database connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Create all required tables in the database
     */
    private static void createTables() throws SQLException {
        // Create users table for authentication
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        // Create teachers table
        String createTeachersTable = """
                CREATE TABLE IF NOT EXISTS teachers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE,
                    phone TEXT,
                    address TEXT,
                    subject TEXT,
                    salary REAL,
                    join_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        // Create students table
        String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    roll_number TEXT UNIQUE,
                    class TEXT,
                    section TEXT,
                    parent_name TEXT,
                    parent_phone TEXT,
                    address TEXT,
                    join_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        // Create fees table
        String createFeesTable = """
                CREATE TABLE IF NOT EXISTS fees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER,
                    amount REAL,
                    payment_date DATE,
                    due_date DATE,
                    status TEXT,
                    payment_method TEXT,
                    receipt_number TEXT,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                )
                """;

        // Create attendance table
        String createAttendanceTable = """
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER,
                    date DATE,
                    status TEXT,
                    remarks TEXT,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                )
                """;

        // Create teacher_subjects table (many-to-many relationship)
        String createTeacherSubjectsTable = """
                CREATE TABLE IF NOT EXISTS teacher_subjects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    teacher_id INTEGER,
                    subject_name TEXT,
                    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
                )
                """;

        // Execute all create table statements
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(createUsersTable);
            statement.execute(createTeachersTable);
            statement.execute(createStudentsTable);
            statement.execute(createFeesTable);
            statement.execute(createAttendanceTable);
            statement.execute(createTeacherSubjectsTable);
        }
    }

    /**
     * Create a default admin user if no users exist
     */
    public static void createDefaultAdminIfNotExists() {
        try {
            String query = "SELECT COUNT(*) FROM users";
            try (Statement statement = getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    // No users exist, create default admin
                    String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                    String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = getConnection().prepareStatement(insertQuery)) {
                        pstmt.setString(1, "admin");
                        pstmt.setString(2, hashedPassword);
                        pstmt.setString(3, "ADMIN");
                        pstmt.executeUpdate();
                        System.out.println("Default admin user created.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to check/create default admin: " + e.getMessage());
        }
    }

    /**
     * Verifies login credentials
     * @param username Username to check
     * @param password Password to check
     * @return The user role if authentication is successful, null otherwise
     */
    public static String authenticateUser(String username, String password) {
        try {
            String query = "SELECT password, role FROM users WHERE username = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHashedPassword = rs.getString("password");
                        if (BCrypt.checkpw(password, storedHashedPassword)) {
                            return rs.getString("role");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Authentication failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Close the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }

    /**
     * Run a query and return the results
     */
    public static ResultSet executeQuery(String query) throws SQLException {
        Statement statement = getConnection().createStatement();
        return statement.executeQuery(query);
    }

    /**
     * Execute a statement that modifies the database (INSERT, UPDATE, DELETE)
     */
    public static int executeUpdate(String query) throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            return statement.executeUpdate(query);
        }
    }
}
