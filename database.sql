-- ====================================================================
-- College Java Mini Project: DELIVERY & LOGISTICS MANAGEMENT SYSTEM
-- Target DBMS: MySQL 8.0+
-- Database: delivery_logistics_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS delivery_logistics_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE delivery_logistics_db;

-- Clean re-creation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS shipment_tracking;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS delivery_staff;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================================
-- Table 1: users
-- Stores credentials and roles for ADMIN, CUSTOMER, and DELIVERY_STAFF
-- ====================================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER', 'DELIVERY_STAFF') NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- ====================================================================
-- Table 2: customers
-- Customer profile details linked to users table
-- ====================================================================
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    INDEX idx_customer_phone (phone)
) ENGINE=InnoDB;

-- ====================================================================
-- Table 3: delivery_staff
-- Delivery personnel details, assigned vehicle, and availability
-- ====================================================================
CREATE TABLE delivery_staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    staff_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    vehicle_number VARCHAR(50) NOT NULL,
    availability_status ENUM('AVAILABLE', 'BUSY', 'OFF_DUTY') NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    INDEX idx_staff_status (availability_status)
) ENGINE=InnoDB;

-- ====================================================================
-- Table 4: shipments
-- Master shipment records with customer, staff, and address details
-- ====================================================================
CREATE TABLE shipments (
    shipment_id INT AUTO_INCREMENT PRIMARY KEY,
    tracking_id VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    staff_id INT DEFAULT NULL,
    sender_name VARCHAR(100) NOT NULL,
    sender_address TEXT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    pickup_address TEXT NOT NULL,
    delivery_address TEXT NOT NULL,
    package_description VARCHAR(255) NOT NULL,
    package_weight DECIMAL(6, 2) NOT NULL,
    package_type VARCHAR(50) NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date DATE NOT NULL,
    shipment_status ENUM('BOOKED', 'CONFIRMED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'BOOKED',
    delivery_charge DECIMAL(8, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES delivery_staff (staff_id) ON DELETE SET NULL,
    INDEX idx_shipment_tracking (tracking_id),
    INDEX idx_shipment_status (shipment_status),
    INDEX idx_shipment_customer (customer_id)
) ENGINE=InnoDB;

-- ====================================================================
-- Table 5: shipment_tracking
-- Complete status transition audit trail with location and timestamps
-- ====================================================================
CREATE TABLE shipment_tracking (
    tracking_entry_id INT AUTO_INCREMENT PRIMARY KEY,
    tracking_id VARCHAR(50) NOT NULL,
    shipment_id INT NOT NULL,
    shipment_status ENUM('BOOKED', 'CONFIRMED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') NOT NULL,
    location VARCHAR(150) NOT NULL,
    remarks VARCHAR(255) DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments (shipment_id) ON DELETE CASCADE,
    INDEX idx_tracking_history (tracking_id),
    INDEX idx_tracking_shipment (shipment_id)
) ENGINE=InnoDB;

-- ====================================================================
-- Table 6: payments
-- Payment transaction records linked to shipments
-- ====================================================================
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    shipment_id INT NOT NULL UNIQUE,
    amount DECIMAL(8, 2) NOT NULL,
    payment_method ENUM('CASH_ON_DELIVERY', 'ONLINE_PAYMENT') NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments (shipment_id) ON DELETE CASCADE,
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB;

-- ====================================================================
-- INITIAL SEED DATA
-- Default BCrypt Passwords:
-- Admin:          admin     / Admin@123
-- Customer:       customer  / Customer@123
-- Delivery Staff: staff     / Staff@123
-- (DatabaseInitializer automatically ensures exact BCrypt hashes on start)
-- ====================================================================

-- 1. Insert Default Users
INSERT INTO users (user_id, username, password, role, email, status) VALUES
(1, 'admin', '$2a$12$7N8kKk7sV8Gf3I3uLgT8/e0L3r7zJqK3o7Y8Z9a0B1c2D3e4F5g6h', 'ADMIN', 'admin@logistics.com', 'ACTIVE'),
(2, 'customer', '$2a$12$8O9lLl8tW9Hg4J4vMhU9/f1M4s8aKrL4p8Z9a0b1C2d3E4f5G6h7i', 'CUSTOMER', 'customer@gmail.com', 'ACTIVE'),
(3, 'staff', '$2a$12$9P0mMm9uX0Ih5K5wNiV0/g2N5t9bLsM5q9a0b1c2D3e4F5g6H7i8j', 'DELIVERY_STAFF', 'staff@logistics.com', 'ACTIVE');

-- 2. Insert Customer Profile
INSERT INTO customers (customer_id, user_id, customer_name, phone, address) VALUES
(1, 2, 'Arun Kumar', '9876543210', 'Flat 402, Green Glen Residency, Outer Ring Road, Bangalore - 560103');

-- 3. Insert Delivery Staff Profile
INSERT INTO delivery_staff (staff_id, user_id, staff_name, phone, vehicle_number, availability_status) VALUES
(1, 3, 'Rajesh Sharma', '9123456780', 'KA-01-MJ-4589', 'AVAILABLE');

-- 4. Insert Sample Shipments
INSERT INTO shipments (shipment_id, tracking_id, customer_id, staff_id, sender_name, sender_address, 
                       receiver_name, receiver_phone, pickup_address, delivery_address, 
                       package_description, package_weight, package_type, booking_date, 
                       expected_delivery_date, shipment_status, delivery_charge) VALUES
(1, 'DLM2026100001', 1, 1, 'Arun Kumar', 'Outer Ring Road, Bangalore', 
    'Vikram Singh', '9845012345', 'Outer Ring Road, Bangalore', 'Indiranagar 100ft Road, Bangalore - 560038', 
    'MacBook Pro Charger & Electronics', 2.50, 'Electronics', NOW() - INTERVAL 2 DAY, 
    CURDATE() + INTERVAL 1 DAY, 'IN_TRANSIT', 180.00),
(2, 'DLM2026100002', 1, 1, 'Arun Kumar', 'Outer Ring Road, Bangalore', 
    'Pooja Nair', '9731298765', 'Outer Ring Road, Bangalore', 'Koramangala 5th Block, Bangalore - 560034', 
    'Legal Property Documents Folder', 0.80, 'Documents', NOW() - INTERVAL 4 DAY, 
    CURDATE() - INTERVAL 1 DAY, 'DELIVERED', 90.00);

-- 5. Insert Tracking History for Sample Shipments
INSERT INTO shipment_tracking (tracking_id, shipment_id, shipment_status, location, remarks, updated_at) VALUES
('DLM2026100001', 1, 'BOOKED', 'Bangalore Central Hub', 'Shipment booked online by customer.', NOW() - INTERVAL 2 DAY),
('DLM2026100001', 1, 'CONFIRMED', 'Bangalore Central Hub', 'Shipment verified and confirmed by Admin.', NOW() - INTERVAL 40 HOUR),
('DLM2026100001', 1, 'PICKED_UP', 'Outer Ring Road, Bangalore', 'Picked up by Rajesh Sharma (Vehicle: KA-01-MJ-4589).', NOW() - INTERVAL 24 HOUR),
('DLM2026100001', 1, 'IN_TRANSIT', 'Electronic City Sorting Facility', 'Package processed through main sorting hub.', NOW() - INTERVAL 6 HOUR),

('DLM2026100002', 2, 'BOOKED', 'Bangalore Central Hub', 'Booking accepted.', NOW() - INTERVAL 4 DAY),
('DLM2026100002', 2, 'CONFIRMED', 'Bangalore Central Hub', 'Confirmed by Admin.', NOW() - INTERVAL 3 DAY),
('DLM2026100002', 2, 'PICKED_UP', 'Outer Ring Road, Bangalore', 'Picked up by courier staff.', NOW() - INTERVAL 2 DAY),
('DLM2026100002', 2, 'IN_TRANSIT', 'Koramangala Hub', 'In transit to local delivery center.', NOW() - INTERVAL 30 HOUR),
('DLM2026100002', 2, 'OUT_FOR_DELIVERY', 'Koramangala Hub', 'Out for delivery with Rajesh Sharma.', NOW() - INTERVAL 26 HOUR),
('DLM2026100002', 2, 'DELIVERED', 'Koramangala 5th Block', 'Package handed over to receiver Pooja Nair.', NOW() - INTERVAL 24 HOUR);

-- 6. Insert Payment Records
INSERT INTO payments (payment_id, shipment_id, amount, payment_method, payment_status, payment_date) VALUES
(1, 1, 180.00, 'ONLINE_PAYMENT', 'PAID', NOW() - INTERVAL 2 DAY),
(2, 2, 90.00, 'CASH_ON_DELIVERY', 'PAID', NOW() - INTERVAL 24 HOUR);
