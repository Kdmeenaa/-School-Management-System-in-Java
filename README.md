# School Management System

A comprehensive Java-based School Management System with features for student and teacher management, fee tracking, attendance, and reporting.

## Features

- User Authentication
- Admin Dashboard
- Student Management
- Teacher Management
- Fees & Salary Management
- Attendance System
- Reports & Data Export
- Database Storage

## System Requirements

- Java Development Kit (JDK) 17+
- Maven for building
- SQLite (embedded database, no installation required)

## Building the Application

### Using Maven

```bash
cd SchoolManagementSystem
mvn clean package
```

This will produce a JAR file in the `target` directory: `SchoolManagementSystem-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Without Maven

If Maven is not available, you can compile and package the application manually:

1. Compile Java files:
```bash
javac -d bin -cp "libs/*" src/main/java/com/school/**/*.java
```

2. Create JAR file:
```bash
jar cvfm SchoolManagementSystem.jar manifest.txt -C bin .
```

## Making the Application Network-Accessible

Since this is a desktop application built with Java Swing, it's primarily designed to run locally on a user's machine. However, there are several ways to make it accessible to clients on other machines:

### Option 1: Converting to a Client-Server Architecture

1. Separate the application into:
   - A server component that handles database operations and business logic
   - A client component that handles the user interface

2. The server would expose an API (using technologies like Java RMI, gRPC, or a RESTful API)

3. The client would connect to the server over the network

This approach requires significant architectural changes to the current application.

### Option 2: Using a Remote Desktop Solution

1. Install the application on a server or dedicated machine
2. Use remote desktop software to allow clients to access the application:
   - Windows Remote Desktop
   - VNC
   - TeamViewer
   - AnyDesk

This approach is simpler but may have performance issues over slower networks.

### Option 3: Deploying as a Web Application

The most comprehensive solution would be to rewrite the application as a web application using:
- Spring Boot for the backend
- Thymeleaf, JSP, or a modern JavaScript framework (React, Angular, Vue) for the frontend
- JDBC or JPA for database access

This approach requires the most significant changes but provides the best user experience for remote access.

## Default Login

- Username: `admin`
- Password: `admin123`

## Database

The application uses SQLite by default, which stores the database in a file. The database file is created automatically on first run.

To use a different database system (like MySQL or PostgreSQL), you would need to:
1. Update the JDBC dependency in the `pom.xml` file
2. Modify the `DatabaseManager` class to use the appropriate connection string
3. Ensure the relevant database server is running and accessible

## License

This project is for educational purposes only.
