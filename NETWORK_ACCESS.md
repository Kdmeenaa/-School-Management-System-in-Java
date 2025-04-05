# Making the School Management System Accessible Over a Network

This document explains how to make your School Management System accessible to clients on other machines across a network.

## Option 1: Remote Access Solutions (Recommended for Quick Setup)

### Using TeamViewer

1. **Install TeamViewer** on the server machine where the School Management System is running:
   - Download from [TeamViewer website](https://www.teamviewer.com/)
   - Install and set up with a permanent password

2. **Share your TeamViewer ID and Password** with clients who need to access the system
   - TeamViewer ID format: xxx xxx xxx
   - Set a secure password

3. **Clients connect using their own TeamViewer client**:
   - They'll see and control your desktop, including the School Management System
   - Multiple users can view simultaneously (with appropriate TeamViewer license)

### Using AnyDesk

1. **Install AnyDesk** on the server machine:
   - Download from [AnyDesk website](https://anydesk.com/)
   - Set up with a permanent password

2. **Share your AnyDesk address** with clients
   - AnyDesk address format: xxx xxx xxx
   - Set a secure password

3. **Clients connect using their own AnyDesk client**

### Using VNC

1. **Install a VNC Server** on the host machine:
   - TightVNC, RealVNC, or UltraVNC
   - Configure port forwarding on your router if needed (typically port 5900)

2. **Share your IP address and VNC password** with clients
   - External IP: Find your public IP at [whatismyip.com](https://www.whatismyip.com/)
   - Set a secure VNC password

3. **Clients connect using a VNC Viewer**:
   - They'll enter your IP address and password

## Option 2: Setting Up a Client-Server Architecture (For Advanced Users)

This approach requires modifying the application to work in a client-server model, separating the database and logic from the UI.

### Step 1: Set Up a Database Server

1. **Install MySQL or PostgreSQL** on your server:
   ```
   sudo apt update
   sudo apt install mysql-server
   ```
   or
   ```
   sudo apt update
   sudo apt install postgresql
   ```

2. **Configure the database** to accept remote connections:
   - Edit MySQL config: `/etc/mysql/mysql.conf.d/mysqld.cnf`
   - Change `bind-address = 127.0.0.1` to `bind-address = 0.0.0.0`

3. **Create a database user** with appropriate permissions:
   ```sql
   CREATE USER 'schooladmin'@'%' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON schooldb.* TO 'schooladmin'@'%';
   FLUSH PRIVILEGES;
   ```

4. **Configure firewall** to allow database port (MySQL: 3306, PostgreSQL: 5432)
   ```
   sudo ufw allow 3306/tcp
   ```

### Step 2: Modify the Application to Use Remote Database

1. **Update the DatabaseManager.java file** to connect to remote database:
   ```java
   private static final String DB_URL = "jdbc:mysql://SERVER_IP:3306/schooldb";
   private static final String DB_USER = "schooladmin";
   private static final String DB_PASSWORD = "password";
   ```

2. **Update the connection method**:
   ```java
   public static Connection getConnection() {
       try {
           if (connection == null || connection.isClosed()) {
               connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return connection;
   }
   ```

3. **Rebuild the application** with these changes

### Step 3: Distribute the Modified Application

1. **Package the modified application** using the `package_app.sh` script
2. **Distribute to clients** with instructions to connect to your server's database

## Option 3: Web Application Conversion (Most Professional Solution)

For a complete network-accessible solution, converting the application to a web-based system is ideal but requires significant development effort.

1. **Create a Spring Boot backend**:
   - REST API for all operations
   - Database access through JPA/Hibernate
   - Authentication with Spring Security

2. **Create a web frontend**:
   - Use Angular, React, or Vue.js
   - Implement all the current screens as web pages
   - Add responsive design for mobile access

3. **Host on a web server**:
   - Apache Tomcat or embedded server
   - Configure security and HTTPS
   - Set up proper authentication and authorization

This option requires professional Java web development skills but provides the most flexible and robust solution for network access.

## IP Address and Connection Information

For any of the above solutions, you'll need to share your IP address with clients:

- **Local network access**: Use your internal IP address (e.g., 192.168.1.x)
  - Find it with `ipconfig` (Windows) or `ifconfig`/`ip addr` (Linux/Mac)

- **Internet access**: Use your public IP address
  - Find it at [whatismyip.com](https://www.whatismyip.com/)
  - Configure port forwarding on your router
  - Consider using a dynamic DNS service if your IP changes frequently

## Security Considerations

When making any application accessible over a network:

1. **Use strong passwords** for all access methods
2. **Keep your operating system and software updated**
3. **Configure firewalls** to only allow necessary connections
4. **Use VPN when possible** for secure remote access
5. **Regularly back up your database** to prevent data loss
