# DELIVERY & LOGISTICS MANAGEMENT SYSTEM

A complete, fully functional, console-based Java Mini Project developed using **Core Java (Java 17+)**, **JDBC**, and **MySQL 8.0**, secured with **BCrypt password hashing** and managed with **Maven**.

Designed specifically for college mini project presentations, academic demonstrations, and enterprise logistics simulation.

---

## 📋 Table of Contents

- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [Database Schema (MySQL)](#-database-schema-mysql)
- [Default Login Credentials](#-default-login-credentials)
- [Installation and Setup](#-installation-and-setup)
- [How to Run](#-how-to-run)
- [Application Walkthrough & Sample Outputs](#-application-walkthrough--sample-outputs)
  - [Main Login Menu](#1-main-login-menu)
  - [Admin Dashboard](#2-admin-dashboard)
  - [Customer Dashboard](#3-customer-dashboard)
  - [Delivery Staff Dashboard](#4-delivery-staff-dashboard)
  - [Shipment Tracking Audit Trail](#5-shipment-tracking-audit-trail)
- [Troubleshooting](#-troubleshooting)

---

## 🌟 Key Features

1. **Role-Based Authentication (RBAC):**
   - Separate interactive portals for **Administrator**, **Customer**, and **Delivery Staff**.
   - Passwords hashed using **BCrypt** with salt rounds. No hardcoded or plaintext credentials.
   - Self-service **Customer Registration** with input validation and duplicate prevention.

2. **Shipment Lifecycle Management:**
   - Automatic unique **Tracking ID** generation (e.g., `DLM2026100001`).
   - Automated delivery charge calculation based on package weight:
     $$\text{Total Charge} = \text{Base Fee (Rs. 50)} + (\text{Weight in KG} \times \text{Rs. 40})$$
   - Realistic package types: *Documents*, *Electronics*, *Apparel*, *Fragile Goods*, and *General Box*.

3. **Real-Time Delivery Tracking Audit Trail:**
   - Strict status state machine progression:
     $$\text{BOOKED} \longrightarrow \text{CONFIRMED} \longrightarrow \text{PICKED\_UP} \longrightarrow \text{IN\_TRANSIT} \longrightarrow \text{OUT\_FOR\_DELIVERY} \longrightarrow \text{DELIVERED}$$
   - Prevents illegal status transitions or tampering.
   - Comprehensive audit trail recording checkpoint timestamp, location, and operational remarks for each milestone.

4. **Payment Record System:**
   - Supports **Cash on Delivery (COD)** and simulated **Online Payment (Debit/Credit/UPI)**.
   - Automatically settles COD payments to `PAID` upon delivery confirmation.

5. **Reports and Statistical Aggregations:**
   - Utilizes SQL aggregate functions (`COUNT()`, `SUM()`, `GROUP BY`).
   - Summarizes total revenue, delivered ratio, in-transit parcels, and courier metrics.

---

## 💻 Technology Stack

- **Programming Language:** Java 17+ (Core Java)
- **Database:** MySQL 8.0+
- **Driver / Connectivity:** MySQL Connector/J (`mysql-connector-j` 8.3.0) via JDBC
- **Security:** `org.mindrot:jbcrypt` (0.4) for one-way salted password hashing
- **Build Tool:** Apache Maven
- **Design Pattern:** Layered MVC Architecture (Controller → Service → DAO → Database)

---

## 🏛️ Project Architecture

```
delivery-logistics-system/
│
├── pom.xml                                 # Maven build configuration
├── database.sql                            # Complete MySQL DDL & DML database script
├── README.md                               # Comprehensive documentation
├── .env.example                            # Database environment variables template
├── .env                                    # Local database credentials
│
├── src/main/resources/
│   └── application.properties             # Database connection & business settings
│
└── src/main/java/
    │
    ├── Main.java                           # Application entry point & main menu
    │
    ├── database/
    │   ├── DatabaseConnection.java         # Thread-safe JDBC connection singleton
    │   └── DatabaseInitializer.java        # Auto-creates DB, schema & default accounts
    │
    ├── model/
    │   ├── User.java                       # User entity (ADMIN, CUSTOMER, DELIVERY_STAFF)
    │   ├── Customer.java                   # Customer profile entity
    │   ├── DeliveryStaff.java              # Delivery personnel & vehicle entity
    │   ├── Shipment.java                   # Master shipment entity
    │   ├── ShipmentTracking.java           # Checkpoint audit trail entity
    │   └── Payment.java                    # Payment transaction entity
    │
    ├── dao/
    │   ├── UserDAO.java                    # User table CRUD
    │   ├── CustomerDAO.java                # Customer table CRUD
    │   ├── DeliveryStaffDAO.java           # Staff table CRUD
    │   ├── ShipmentDAO.java                # Shipment table CRUD & SQL aggregations
    │   ├── ShipmentTrackingDAO.java        # Tracking log operations
    │   └── PaymentDAO.java                 # Payment records & revenue summation
    │
    ├── service/
    │   ├── AuthenticationService.java      # Login authentication & customer sign-up
    │   ├── ShipmentService.java            # Booking, calculations & assignment
    │   ├── DeliveryService.java            # Staff management & status transitions
    │   ├── TrackingService.java            # Timeline rendering & history flow
    │   └── PaymentService.java             # Transaction inspection & revenue reports
    │
    ├── controller/
    │   ├── AdminController.java            # 15 Admin dashboard console operations
    │   ├── CustomerController.java         # 8 Customer portal console operations
    │   └── DeliveryStaffController.java    # 6 Delivery personnel console operations
    │
    └── util/
        ├── ConsoleUtil.java                # Formatted headers, tables & input prompts
        ├── InputValidator.java             # Regex validation (email, phone, username)
        ├── PasswordUtil.java               # BCrypt password hashing & verification
        └── TrackingIdGenerator.java        # Formatted unique tracking ID generator
```

---

## 🗄️ Database Schema (MySQL)

Database Name: `delivery_logistics_db`

```
+--------------------+        +-----------------------+        +-------------------+
|      users         |        |       customers       |        |   delivery_staff  |
+--------------------+        +-----------------------+        +-------------------+
| user_id (PK)       |<-------| user_id (FK)          |   +--->| user_id (FK)      |
| username (UNIQUE)  |        | customer_id (PK)      |   |    | staff_id (PK)     |
| password (BCrypt)  |        | customer_name         |   |    | staff_name        |
| role (ENUM)        |        | phone                 |   |    | phone             |
| email (UNIQUE)     |        | address               |   |    | vehicle_number    |
| status             |        +-----------------------+   |    | status            |
+--------------------+                    |               |    +-------------------+
                                          | 1             |              | 1
                                          |               |              |
                                          | N             |              | N
                                      +-----------------------+          |
                                      |       shipments       |----------+
                                      +-----------------------+
                                      | shipment_id (PK)      |
                                      | tracking_id (UNIQUE)  |
                                      | customer_id (FK)      |
                                      | staff_id (FK, NULL)   |
                                      | sender_name           |
                                      | receiver_name         |
                                      | pickup_address        |
                                      | delivery_address      |
                                      | package_weight        |
                                      | package_type          |
                                      | booking_date          |
                                      | expected_delivery     |
                                      | shipment_status       |
                                      | delivery_charge       |
                                      +-----------------------+
                                           | 1            | 1
                                           |              |
                                           | N            | 1
                      +-------------------------+    +-----------------------+
                      |    shipment_tracking    |    |       payments        |
                      +-------------------------+    +-----------------------+
                      | tracking_entry_id (PK)  |    | payment_id (PK)       |
                      | tracking_id             |    | shipment_id (FK, UNQ) |
                      | shipment_id (FK)        |    | amount                |
                      | shipment_status         |    | payment_method (ENUM) |
                      | location                |    | payment_status (ENUM) |
                      | remarks                 |    | payment_date          |
                      | updated_at              |    +-----------------------+
                      +-------------------------+
```

---

## 🔑 Default Login Credentials

| Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin` | `Admin@123` | Full system control: customer records, staff assignments, status overrides, reports |
| **Registered Customer** | `customer` | `Customer@123` | Book deliveries, generate tracking IDs, view personal shipments, cancel bookings |
| **Delivery Personnel** | `staff` | `Staff@123` | Inspect assigned packages, update checkpoint statuses (Picked up, Transit, Delivered) |

> **Note:** Passwords in the database are hashed with BCrypt. The application automatically synchronizes and validates these accounts on launch.

---

## ⚙️ Installation and Setup

### 1. Prerequisites
- **Java Development Kit (JDK 17 or later)**: Verify with `java -version`.
- **MySQL Server 8.0+**: Ensure MySQL service is running on `localhost:3306`.
- **Apache Maven 3.8+** (Optional if using IDE or bundled commands).

### 2. Configure Database Credentials
Open `src/main/resources/application.properties` (or copy `.env.example` to `.env`):
```properties
db.host=localhost
db.port=3306
db.name=delivery_logistics_db
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```
*(Leave `db.password=` empty if your root user has no password).*

### 3. Initialize Database
You can initialize the database using either method:
- **Method A (Automatic):** Simply run the application. `DatabaseInitializer` will auto-create `delivery_logistics_db` and execute the schema.
- **Method B (Manual):** Import `database.sql` into MySQL Workbench or via command line:
  ```bash
  mysql -u root -p < database.sql
  ```

---

## 🚀 How to Run

### Option 1: Using Maven Command Line
In the root directory of the project:
```bash
# 1. Compile the code
mvn clean compile

# 2. Run the application
mvn exec:java
```

### Option 2: Running in IntelliJ IDEA / Eclipse / VS Code
1. Open the project folder (`c:\java mini project`) in IntelliJ IDEA or VS Code.
2. Allow Maven to import dependencies (`mysql-connector-j` and `jbcrypt`).
3. Open `src/main/java/Main.java`.
4. Right-click and choose **Run 'Main.main()'**.

### Option 3: Build Standalone Runnable JAR
```bash
mvn package
java -jar target/delivery-logistics-system-1.0.0-jar-with-dependencies.jar
```

---

## 📸 Application Walkthrough & Sample Outputs

### 1. Main Login Menu
```
========================================
    DELIVERY & LOGISTICS MANAGEMENT     
                 SYSTEM                 
========================================
1. Admin Login
2. Customer Login
3. Delivery Staff Login
4. Customer Registration
5. Exit
========================================
Enter your choice: 1
```

---

### 2. Admin Dashboard
```
==================================================
                 ADMIN DASHBOARD                  
==================================================
Logged in as: admin (admin@logistics.com)

1.  View All Customers
2.  Add Delivery Staff
3.  View Delivery Staff
4.  Update Delivery Staff
5.  Delete Delivery Staff
6.  View All Shipments
7.  Create Shipment
8.  Assign Delivery Staff
9.  Update Shipment Status
10. Delete Shipment
11. Search Shipment (Tracking ID)
12. View Payment Details
13. Generate Reports
14. Dashboard Statistics
15. Logout

Enter your choice: 14

==================================================
          LIVE DASHBOARD SYSTEM METRICS           
==================================================
DATABASE AGGREGATE SUMMARY (SQL COUNT & SUM):
• Registered Customers       : 1
• Active Delivery Personnel   : 1
• Lifetime Shipments Logged   : 2
• Total Paid Revenue Settled  : Rs. 270.00

SHIPMENT BREAKDOWN BY STATUS (GROUP BY):
  - IN_TRANSIT         : 1
  - DELIVERED          : 1
```

---

### 3. Customer Dashboard
```
==================================================
                CUSTOMER DASHBOARD                
==================================================
Welcome, Arun Kumar | Phone: 9876543210

1. Book New Delivery
2. View My Shipments
3. Track My Shipment
4. View Shipment Details
5. Cancel Shipment
6. View Payment Details
7. View Profile
8. Logout

Enter your choice: 1

==================================================
           BOOK NEW DELIVERY SHIPMENT             
==================================================
Rate Plan: Base Charge Rs. 50 + Rs. 40/KG

Sender Name: Arun Kumar
Sender Full Address: Outer Ring Road, Bangalore
Receiver Name: Neha Sharma
Receiver Phone Number: 9876501234
Pickup Address: Outer Ring Road, Bangalore
Delivery Destination Address: Whitefield ITPL Main Rd, Bangalore - 560066
Package Description: Confidential Research Folder & Flash Drive
Package Weight in KG (e.g. 1.5): 1.5

Select Package Classification:
1. Documents / Envelopes
2. Electronics / Gadgets
3. Apparel / Fashion
4. Fragile Items / Glassware
5. General Box / Parcel
Choice: 1

--> Calculated Delivery Fee: Rs. 110.00
Select Payment Option:
1. Cash On Delivery (Pay upon arrival)
2. Online Payment (Debit/Credit/UPI Simulated)
Choice: 1

Confirm shipment booking with charge Rs. 110.00 (y/n): y

[SUCCESS] Shipment Booked Successfully!
Tracking ID      : DLM20261058291
Status           : BOOKED
Delivery Charge  : Rs. 110.00
Expected By      : 2026-11-03
Keep your Tracking ID safe to check delivery progress.
```

---

### 4. Delivery Staff Dashboard
```
==================================================
             DELIVERY STAFF DASHBOARD             
==================================================
Courier Agent: Rajesh Sharma | Vehicle: KA-01-MJ-4589
Availability : AVAILABLE

1. View Assigned Shipments
2. View Shipment Details
3. Update Delivery Status
4. View Delivery History
5. View Profile
6. Logout

Enter your choice: 3

==================================================
          UPDATE SHIPMENT DELIVERY STATUS         
==================================================
Enter Shipment ID to update: 1
Tracking ID     : DLM2026100001
Current Status  : IN_TRANSIT
Receiver Name   : Vikram Singh (9845012345)
Delivery Address: Indiranagar 100ft Road, Bangalore - 560038

Select Next Progression Status:
1. PICKED_UP       (Package collected from sender)
2. IN_TRANSIT      (Moving through sorting facilities)
3. OUT_FOR_DELIVERY(Courier out for final handover)
4. DELIVERED       (Handed over to receiver)
Choice: 3

Current Location / Checkpoint: Indiranagar Local Hub
Operational Remarks: Package sorted into local van for final delivery

[SUCCESS] Status updated to OUT_FOR_DELIVERY and tracking audit trail recorded!
```

---

### 5. Shipment Tracking Audit Trail
```
==================================================
               SHIPMENT TRACKING                  
==================================================
Tracking ID     : DLM2026100001
Current Status  : OUT_FOR_DELIVERY
Sender          : Arun Kumar
Receiver        : Vikram Singh (9845012345)
Delivery Address: Indiranagar 100ft Road, Bangalore - 560038
Expected Date   : 2026-11-01
--------------------------------------------------
STATUS HISTORY & AUDIT TRAIL:
--------------------------------------------------
[BOOKED]
  Time    : 29-Oct-2026 01:13 PM
  Location: Bangalore Central Hub
  Remarks : Shipment booked online by customer.
  |
  v
[CONFIRMED]
  Time    : 29-Oct-2026 09:13 PM
  Location: Bangalore Central Hub
  Remarks : Shipment verified and confirmed by Admin.
  |
  v
[PICKED_UP]
  Time    : 30-Oct-2026 01:13 PM
  Location: Outer Ring Road, Bangalore
  Remarks : Picked up by Rajesh Sharma (Vehicle: KA-01-MJ-4589).
  |
  v
[IN_TRANSIT]
  Time    : 31-Oct-2026 07:13 AM
  Location: Electronic City Sorting Facility
  Remarks : Package processed through main sorting hub.
  |
  v
[OUT_FOR_DELIVERY]
  Time    : 31-Oct-2026 01:25 PM
  Location: Indiranagar Local Hub
  Remarks : Package sorted into local van for final delivery
==================================================
```

---

## 🛠️ Troubleshooting

1. **`java.sql.SQLException: Access denied for user 'root'@'localhost'`:**
   - Open `src/main/resources/application.properties` or `.env`.
   - Update `db.password` with your actual MySQL password.

2. **`java.sql.SQLNonTransientConnectionException: Could not create connection to database server`:**
   - Make sure your MySQL service is started (e.g., via Windows Services or `net start mysql`).

3. **Status transition rejected:**
   - The system strictly enforces the sequential supply-chain timeline:
     `BOOKED` → `CONFIRMED` → `PICKED_UP` → `IN_TRANSIT` → `OUT_FOR_DELIVERY` → `DELIVERED`.
   - You cannot mark a newly booked package directly as `DELIVERED`.
