package database;

import util.PasswordUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static boolean initializeDatabase() {
        System.out.println("[DatabaseInitializer] Connecting to MySQL server...");

        // 1. Create database if it does not exist
        try (Connection serverConn = DatabaseConnection.getInstance().getServerConnection();
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DatabaseConnection.getDbName() + 
                               " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            System.out.println("[DatabaseInitializer] Verified database: " + DatabaseConnection.getDbName());
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Notice: Server-level DB creation: " + e.getMessage());
        }

        // 2. Connect to database and verify schema
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (!tablesExist(conn)) {
                System.out.println("[DatabaseInitializer] Tables not found. Creating database schema...");
                createSchema(conn);
            }

            // 3. Ensure default accounts (Admin, Customer, Staff) are present and properly hashed
            ensureDefaultAccounts(conn);

            System.out.println("[DatabaseInitializer] Database connectivity and schema ready.");
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Database connection failure: " + e.getMessage());
            return false;
        }
    }

    private static boolean tablesExist(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "users", new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void createSchema(Connection conn) {
        String[] sqlStatements = {
            "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "role ENUM('ADMIN', 'CUSTOMER', 'DELIVERY_STAFF') NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_user_username (username), " +
                "INDEX idx_user_role (role)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL UNIQUE, " +
                "customer_name VARCHAR(100) NOT NULL, " +
                "phone VARCHAR(20) NOT NULL, " +
                "address TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE, " +
                "INDEX idx_customer_phone (phone)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS delivery_staff (" +
                "staff_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL UNIQUE, " +
                "staff_name VARCHAR(100) NOT NULL, " +
                "phone VARCHAR(20) NOT NULL, " +
                "vehicle_number VARCHAR(50) NOT NULL, " +
                "availability_status ENUM('AVAILABLE', 'BUSY', 'OFF_DUTY') NOT NULL DEFAULT 'AVAILABLE', " +
                "FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE, " +
                "INDEX idx_staff_status (availability_status)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS shipments (" +
                "shipment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "tracking_id VARCHAR(50) NOT NULL UNIQUE, " +
                "customer_id INT NOT NULL, " +
                "staff_id INT DEFAULT NULL, " +
                "sender_name VARCHAR(100) NOT NULL, " +
                "sender_address TEXT NOT NULL, " +
                "receiver_name VARCHAR(100) NOT NULL, " +
                "receiver_phone VARCHAR(20) NOT NULL, " +
                "pickup_address TEXT NOT NULL, " +
                "delivery_address TEXT NOT NULL, " +
                "package_description VARCHAR(255) NOT NULL, " +
                "package_weight DECIMAL(6, 2) NOT NULL, " +
                "package_type VARCHAR(50) NOT NULL, " +
                "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "expected_delivery_date DATE NOT NULL, " +
                "shipment_status ENUM('BOOKED', 'CONFIRMED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'BOOKED', " +
                "delivery_charge DECIMAL(8, 2) NOT NULL, " +
                "FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (staff_id) REFERENCES delivery_staff (staff_id) ON DELETE SET NULL, " +
                "INDEX idx_shipment_tracking (tracking_id), " +
                "INDEX idx_shipment_status (shipment_status)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS shipment_tracking (" +
                "tracking_entry_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "tracking_id VARCHAR(50) NOT NULL, " +
                "shipment_id INT NOT NULL, " +
                "shipment_status ENUM('BOOKED', 'CONFIRMED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') NOT NULL, " +
                "location VARCHAR(150) NOT NULL, " +
                "remarks VARCHAR(255) DEFAULT NULL, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (shipment_id) REFERENCES shipments (shipment_id) ON DELETE CASCADE, " +
                "INDEX idx_tracking_history (tracking_id)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS payments (" +
                "payment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "shipment_id INT NOT NULL UNIQUE, " +
                "amount DECIMAL(8, 2) NOT NULL, " +
                "payment_method ENUM('CASH_ON_DELIVERY', 'ONLINE_PAYMENT') NOT NULL, " +
                "payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING', " +
                "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (shipment_id) REFERENCES shipments (shipment_id) ON DELETE CASCADE, " +
                "INDEX idx_payment_status (payment_status)" +
            ") ENGINE=InnoDB;"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
            }
            System.out.println("[DatabaseInitializer] All tables created successfully.");
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Error executing schema creation: " + e.getMessage());
        }
    }

    private static void ensureDefaultAccounts(Connection conn) {
        String adminPassHash = PasswordUtil.hashPassword("Admin@123");
        String customerPassHash = PasswordUtil.hashPassword("Customer@123");
        String staffPassHash = PasswordUtil.hashPassword("Staff@123");

        // 1. Ensure Admin
        ensureUser(conn, "admin", adminPassHash, "ADMIN", "admin@logistics.com", "Admin@123");

        // 2. Ensure Customer
        int customerUserId = ensureUser(conn, "customer", customerPassHash, "CUSTOMER", "customer@gmail.com", "Customer@123");
        if (customerUserId > 0) {
            ensureCustomerProfile(conn, customerUserId, "Arun Kumar", "9876543210", "Outer Ring Road, Bangalore - 560103");
        }

        // 3. Ensure Staff
        int staffUserId = ensureUser(conn, "staff", staffPassHash, "DELIVERY_STAFF", "staff@logistics.com", "Staff@123");
        if (staffUserId > 0) {
            ensureStaffProfile(conn, staffUserId, "Rajesh Sharma", "9123456780", "KA-01-MJ-4589");
        }
    }

    private static int ensureUser(Connection conn, String username, String hashedPass, String role, String email, String plainPass) {
        String checkSql = "SELECT user_id, password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    String insertSql = "INSERT INTO users (username, password, role, email, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
                    try (PreparedStatement ins = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        ins.setString(1, username);
                        ins.setString(2, hashedPass);
                        ins.setString(3, role);
                        ins.setString(4, email);
                        ins.executeUpdate();
                        try (ResultSet keys = ins.getGeneratedKeys()) {
                            if (keys.next()) return keys.getInt(1);
                        }
                    }
                } else {
                    int userId = rs.getInt("user_id");
                    String currentHash = rs.getString("password");
                    if (!PasswordUtil.checkPassword(plainPass, currentHash)) {
                        try (PreparedStatement update = conn.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")) {
                            update.setString(1, hashedPass);
                            update.setInt(2, userId);
                            update.executeUpdate();
                        }
                    }
                    return userId;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] User check/insert failed for " + username + ": " + e.getMessage());
        }
        return -1;
    }

    private static void ensureCustomerProfile(Connection conn, int userId, String name, String phone, String address) {
        String checkSql = "SELECT customer_id FROM customers WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    String insertSql = "INSERT INTO customers (user_id, customer_name, phone, address) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        ins.setInt(1, userId);
                        ins.setString(2, name);
                        ins.setString(3, phone);
                        ins.setString(4, address);
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Customer profile check/insert failed: " + e.getMessage());
        }
    }

    private static void ensureStaffProfile(Connection conn, int userId, String name, String phone, String vehicle) {
        String checkSql = "SELECT staff_id FROM delivery_staff WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    String insertSql = "INSERT INTO delivery_staff (user_id, staff_name, phone, vehicle_number, availability_status) " +
                                       "VALUES (?, ?, ?, ?, 'AVAILABLE')";
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        ins.setInt(1, userId);
                        ins.setString(2, name);
                        ins.setString(3, phone);
                        ins.setString(4, vehicle);
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Staff profile check/insert failed: " + e.getMessage());
        }
    }
}
