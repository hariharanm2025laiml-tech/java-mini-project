package controller;

import dao.CustomerDAO;
import model.Customer;
import model.DeliveryStaff;
import model.Payment;
import model.Shipment;
import model.User;
import service.DeliveryService;
import service.PaymentService;
import service.ShipmentService;
import service.TrackingService;
import util.ConsoleUtil;
import util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminController {

    private final ShipmentService shipmentService = new ShipmentService();
    private final DeliveryService deliveryService = new DeliveryService();
    private final PaymentService paymentService = new PaymentService();
    private final TrackingService trackingService = new TrackingService();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public void handleAdminDashboard(User adminUser) {
        boolean running = true;
        while (running) {
            ConsoleUtil.printHeader("ADMIN DASHBOARD");
            System.out.println("Logged in as: " + adminUser.getUsername() + " (" + adminUser.getEmail() + ")\n");
            System.out.println("1.  View All Customers");
            System.out.println("2.  Add Delivery Staff");
            System.out.println("3.  View Delivery Staff");
            System.out.println("4.  Update Delivery Staff");
            System.out.println("5.  Delete Delivery Staff");
            System.out.println("6.  View All Shipments");
            System.out.println("7.  Create Shipment");
            System.out.println("8.  Assign Delivery Staff");
            System.out.println("9.  Update Shipment Status");
            System.out.println("10. Delete Shipment");
            System.out.println("11. Search Shipment (Tracking ID)");
            System.out.println("12. View Payment Details");
            System.out.println("13. Generate Reports");
            System.out.println("14. Dashboard Statistics");
            System.out.println("15. Logout");

            int choice = ConsoleUtil.readInt("\nEnter your choice", 1, 15);
            switch (choice) {
                case 1 -> viewAllCustomers();
                case 2 -> addDeliveryStaff();
                case 3 -> viewDeliveryStaff();
                case 4 -> updateDeliveryStaff();
                case 5 -> deleteDeliveryStaff();
                case 6 -> viewAllShipments();
                case 7 -> createShipmentByAdmin();
                case 8 -> assignDeliveryStaff();
                case 9 -> updateShipmentStatus();
                case 10 -> deleteShipment();
                case 11 -> searchShipment();
                case 12 -> viewPaymentDetails();
                case 13 -> generateReports();
                case 14 -> viewDashboardStatistics();
                case 15 -> {
                    ConsoleUtil.printInfo("Logging out of Admin Dashboard...");
                    running = false;
                }
            }
        }
    }

    private void viewAllCustomers() {
        ConsoleUtil.printHeader("REGISTERED CUSTOMERS");
        try {
            List<Customer> list = customerDAO.findAll();
            if (list.isEmpty()) {
                ConsoleUtil.printInfo("No customers found in database.");
            } else {
                System.out.printf("%-5s | %-12s | %-20s | %-14s | %-25s | %-20s\n", 
                    "ID", "Username", "Customer Name", "Phone", "Email", "Address");
                System.out.println("---------------------------------------------------------------------------------------------------------------");
                for (Customer c : list) {
                    System.out.printf("%-5d | %-12s | %-20s | %-14s | %-25s | %-20s\n",
                        c.getCustomerId(),
                        c.getUsername() != null ? c.getUsername() : "N/A",
                        c.getCustomerName(),
                        c.getPhone(),
                        c.getEmail() != null ? c.getEmail() : "N/A",
                        c.getAddress().length() > 20 ? c.getAddress().substring(0, 17) + "..." : c.getAddress());
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to fetch customers: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void addDeliveryStaff() {
        ConsoleUtil.printHeader("ADD NEW DELIVERY STAFF");
        String username = ConsoleUtil.readRequiredString("Enter Username");
        if (!InputValidator.isValidUsername(username)) {
            ConsoleUtil.printError("Invalid username format. Must be 3-30 alphanumeric characters.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String password = ConsoleUtil.readRequiredString("Enter Temporary Password (min 6 chars)");
        if (!InputValidator.isValidPassword(password)) {
            ConsoleUtil.printError("Password must be at least 6 characters.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String email = ConsoleUtil.readRequiredString("Enter Staff Email");
        if (!InputValidator.isValidEmail(email)) {
            ConsoleUtil.printError("Invalid email format.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String name = ConsoleUtil.readRequiredString("Enter Staff Full Name");
        String phone = ConsoleUtil.readRequiredString("Enter Phone Number");
        String vehicle = ConsoleUtil.readRequiredString("Enter Vehicle Number (e.g. KA-01-AB-1234)");

        try {
            boolean success = deliveryService.addDeliveryStaff(username, password, email, name, phone, vehicle);
            if (success) {
                ConsoleUtil.printSuccess("Delivery Staff registered successfully with BCrypt encrypted password!");
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Registration failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewDeliveryStaff() {
        ConsoleUtil.printHeader("DELIVERY STAFF PERSONNEL");
        try {
            List<DeliveryStaff> list = deliveryService.getAllStaff();
            if (list.isEmpty()) {
                ConsoleUtil.printInfo("No delivery staff members registered.");
            } else {
                System.out.printf("%-5s | %-12s | %-20s | %-14s | %-16s | %-12s\n",
                    "ID", "Username", "Staff Name", "Phone", "Vehicle No", "Status");
                System.out.println("-----------------------------------------------------------------------------------------");
                for (DeliveryStaff s : list) {
                    System.out.printf("%-5d | %-12s | %-20s | %-14s | %-16s | %-12s\n",
                        s.getStaffId(),
                        s.getUsername() != null ? s.getUsername() : "N/A",
                        s.getStaffName(),
                        s.getPhone(),
                        s.getVehicleNumber(),
                        s.getAvailabilityStatus());
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to fetch staff: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void updateDeliveryStaff() {
        ConsoleUtil.printHeader("UPDATE DELIVERY STAFF");
        int staffId = ConsoleUtil.readInt("Enter Delivery Staff ID to update", 1, 999999);
        try {
            Optional<DeliveryStaff> opt = deliveryService.getStaffById(staffId);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("Delivery staff with ID " + staffId + " not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            DeliveryStaff staff = opt.get();
            System.out.println("Current Name    : " + staff.getStaffName());
            System.out.println("Current Phone   : " + staff.getPhone());
            System.out.println("Current Vehicle : " + staff.getVehicleNumber());
            System.out.println("Current Status  : " + staff.getAvailabilityStatus());

            String name = ConsoleUtil.readString("Enter new Name (leave blank to keep current)");
            if (InputValidator.isNotEmpty(name)) staff.setStaffName(name);

            String phone = ConsoleUtil.readString("Enter new Phone (leave blank to keep current)");
            if (InputValidator.isNotEmpty(phone)) staff.setPhone(phone);

            String vehicle = ConsoleUtil.readString("Enter new Vehicle No (leave blank to keep current)");
            if (InputValidator.isNotEmpty(vehicle)) staff.setVehicleNumber(vehicle);

            System.out.println("Select Status: 1. AVAILABLE  2. BUSY  3. OFF_DUTY  4. Keep Current");
            int statChoice = ConsoleUtil.readInt("Choice", 1, 4);
            if (statChoice == 1) staff.setAvailabilityStatus(DeliveryStaff.AvailabilityStatus.AVAILABLE);
            else if (statChoice == 2) staff.setAvailabilityStatus(DeliveryStaff.AvailabilityStatus.BUSY);
            else if (statChoice == 3) staff.setAvailabilityStatus(DeliveryStaff.AvailabilityStatus.OFF_DUTY);

            boolean updated = deliveryService.updateStaff(staff);
            if (updated) {
                ConsoleUtil.printSuccess("Delivery Staff details updated successfully.");
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Update error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void deleteDeliveryStaff() {
        ConsoleUtil.printHeader("DELETE DELIVERY STAFF");
        int staffId = ConsoleUtil.readInt("Enter Delivery Staff ID to delete", 1, 999999);
        try {
            Optional<DeliveryStaff> opt = deliveryService.getStaffById(staffId);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("Delivery Staff ID not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            boolean confirm = ConsoleUtil.readConfirmation("Confirm permanent deletion of " + opt.get().getStaffName());
            if (confirm) {
                boolean deleted = deliveryService.deleteStaff(staffId);
                if (deleted) {
                    ConsoleUtil.printSuccess("Delivery Staff member and user account deleted successfully.");
                } else {
                    ConsoleUtil.printError("Could not delete delivery staff member.");
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Delete error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewAllShipments() {
        ConsoleUtil.printHeader("ALL LOGISTICS SHIPMENTS");
        try {
            List<Shipment> list = shipmentService.getAllShipments();
            if (list.isEmpty()) {
                ConsoleUtil.printInfo("No shipments recorded in system.");
            } else {
                System.out.printf("%-4s | %-14s | %-15s | %-15s | %-16s | %-7s | %-8s\n",
                    "ID", "Tracking ID", "Customer", "Receiver", "Status", "Weight", "Charge");
                System.out.println("-----------------------------------------------------------------------------------------");
                for (Shipment s : list) {
                    System.out.printf("%-4d | %-14s | %-15s | %-15s | %-16s | %-4.1f KG | Rs. %-6.2f\n",
                        s.getShipmentId(),
                        s.getTrackingId(),
                        s.getCustomerName() != null ? s.getCustomerName() : ("Cust#" + s.getCustomerId()),
                        s.getReceiverName(),
                        s.getShipmentStatus(),
                        s.getPackageWeight().doubleValue(),
                        s.getDeliveryCharge().doubleValue());
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Error fetching shipments: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void createShipmentByAdmin() {
        ConsoleUtil.printHeader("CREATE SHIPMENT ON BEHALF OF CUSTOMER");
        int customerId = ConsoleUtil.readInt("Enter Customer ID", 1, 999999);
        try {
            Optional<Customer> custOpt = customerDAO.findById(customerId);
            if (custOpt.isEmpty()) {
                ConsoleUtil.printError("Customer ID " + customerId + " does not exist.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            Customer customer = custOpt.get();
            System.out.println("Booking for Customer: " + customer.getCustomerName() + " (" + customer.getPhone() + ")");

            String senderName = ConsoleUtil.readRequiredString("Sender Name (or press enter for customer name)");
            if (senderName.isEmpty()) senderName = customer.getCustomerName();

            String senderAddress = ConsoleUtil.readRequiredString("Sender Address");
            String receiverName = ConsoleUtil.readRequiredString("Receiver Name");
            String receiverPhone = ConsoleUtil.readRequiredString("Receiver Phone");
            String pickupAddress = ConsoleUtil.readRequiredString("Pickup Address");
            String deliveryAddress = ConsoleUtil.readRequiredString("Delivery Destination Address");
            String description = ConsoleUtil.readRequiredString("Package Description");
            double weightVal = ConsoleUtil.readPositiveDouble("Package Weight in KG (e.g. 2.5)");
            BigDecimal weight = BigDecimal.valueOf(weightVal);

            System.out.println("Select Package Type: 1. Documents  2. Electronics  3. Clothing  4. Fragile  5. General Parcel");
            int pTypeChoice = ConsoleUtil.readInt("Choice", 1, 5);
            String packageType = switch (pTypeChoice) {
                case 1 -> "Documents";
                case 2 -> "Electronics";
                case 3 -> "Clothing";
                case 4 -> "Fragile";
                default -> "General Parcel";
            };

            int days = ConsoleUtil.readInt("Expected Delivery in Days (e.g. 3)", 1, 30);

            System.out.println("Payment Method: 1. Cash On Delivery (COD)  2. Online Payment");
            int payChoice = ConsoleUtil.readInt("Choice", 1, 2);
            Payment.PaymentMethod method = (payChoice == 1) ? Payment.PaymentMethod.CASH_ON_DELIVERY : Payment.PaymentMethod.ONLINE_PAYMENT;

            Shipment booked = shipmentService.bookShipment(customerId, senderName, senderAddress, receiverName,
                receiverPhone, pickupAddress, deliveryAddress, description, weight, packageType, days, method);

            ConsoleUtil.printSuccess("Shipment Created Successfully!");
            System.out.println("Tracking ID      : " + booked.getTrackingId());
            System.out.println("Estimated Charge : Rs. " + booked.getDeliveryCharge());
            System.out.println("Expected Date    : " + booked.getExpectedDeliveryDate());
        } catch (Exception e) {
            ConsoleUtil.printError("Booking failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void assignDeliveryStaff() {
        ConsoleUtil.printHeader("ASSIGN DELIVERY STAFF TO SHIPMENT");
        int shipmentId = ConsoleUtil.readInt("Enter Shipment ID", 1, 999999);
        try {
            Optional<Shipment> shipOpt = shipmentService.getShipmentById(shipmentId);
            if (shipOpt.isEmpty()) {
                ConsoleUtil.printError("Shipment ID not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            Shipment s = shipOpt.get();
            System.out.println("Shipment Tracking ID: " + s.getTrackingId());
            System.out.println("Current Status      : " + s.getShipmentStatus());
            System.out.println("Current Staff       : " + (s.getStaffName() != null ? s.getStaffName() : "Unassigned"));

            int staffId = ConsoleUtil.readInt("Enter Delivery Staff ID to assign", 1, 999999);
            Optional<DeliveryStaff> staffOpt = deliveryService.getStaffById(staffId);
            if (staffOpt.isEmpty()) {
                ConsoleUtil.printError("Staff ID not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            String location = ConsoleUtil.readString("Enter Current Hub / Center Location (e.g. Central Sorting Facility)");
            boolean success = shipmentService.assignStaff(shipmentId, staffId, location);
            if (success) {
                ConsoleUtil.printSuccess("Delivery Staff '" + staffOpt.get().getStaffName() + "' assigned. Shipment marked as CONFIRMED.");
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Assignment failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void updateShipmentStatus() {
        ConsoleUtil.printHeader("UPDATE SHIPMENT DELIVERY STATUS");
        int shipmentId = ConsoleUtil.readInt("Enter Shipment ID", 1, 999999);
        try {
            Optional<Shipment> opt = shipmentService.getShipmentById(shipmentId);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("Shipment ID not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            Shipment s = opt.get();
            System.out.println("Tracking ID    : " + s.getTrackingId());
            System.out.println("Current Status : " + s.getShipmentStatus());
            System.out.println("\nSelect New Status:");
            System.out.println("1. CONFIRMED");
            System.out.println("2. PICKED_UP");
            System.out.println("3. IN_TRANSIT");
            System.out.println("4. OUT_FOR_DELIVERY");
            System.out.println("5. DELIVERED");
            System.out.println("6. CANCELLED");

            int choice = ConsoleUtil.readInt("Choice", 1, 6);
            Shipment.Status newStatus = switch (choice) {
                case 1 -> Shipment.Status.CONFIRMED;
                case 2 -> Shipment.Status.PICKED_UP;
                case 3 -> Shipment.Status.IN_TRANSIT;
                case 4 -> Shipment.Status.OUT_FOR_DELIVERY;
                case 5 -> Shipment.Status.DELIVERED;
                default -> Shipment.Status.CANCELLED;
            };

            String location = ConsoleUtil.readRequiredString("Enter Current City / Hub Location");
            String remarks = ConsoleUtil.readString("Enter Status Remarks");

            boolean updated = deliveryService.updateShipmentStatus(shipmentId, newStatus, location, remarks, null);
            if (updated) {
                ConsoleUtil.printSuccess("Shipment status updated to " + newStatus + " and audit history logged.");
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Status update failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void deleteShipment() {
        ConsoleUtil.printHeader("DELETE SHIPMENT");
        int shipmentId = ConsoleUtil.readInt("Enter Shipment ID to delete", 1, 999999);
        try {
            Optional<Shipment> opt = shipmentService.getShipmentById(shipmentId);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("Shipment ID not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            boolean confirm = ConsoleUtil.readConfirmation("Confirm permanent deletion of Shipment " + opt.get().getTrackingId());
            if (confirm) {
                boolean deleted = shipmentService.deleteShipment(shipmentId);
                if (deleted) {
                    ConsoleUtil.printSuccess("Shipment and associated records deleted successfully.");
                } else {
                    ConsoleUtil.printError("Failed to delete shipment.");
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Delete error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void searchShipment() {
        ConsoleUtil.printHeader("SEARCH SHIPMENT BY TRACKING ID");
        String trackingId = ConsoleUtil.readRequiredString("Enter Tracking ID (e.g. DLM2026100001)");
        try {
            Optional<Shipment> opt = shipmentService.getShipmentByTrackingId(trackingId);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("No shipment found matching tracking ID: " + trackingId);
            } else {
                Shipment s = opt.get();
                System.out.println();
                System.out.println("================================================");
                System.out.println("               SHIPMENT DETAILS                 ");
                System.out.println("================================================");
                System.out.printf("Shipment ID       : %d\n", s.getShipmentId());
                System.out.printf("Tracking ID       : %s\n", s.getTrackingId());
                System.out.printf("Customer Name     : %s\n", s.getCustomerName());
                System.out.printf("Sender Name       : %s\n", s.getSenderName());
                System.out.printf("Receiver Name     : %s\n", s.getReceiverName());
                System.out.printf("Receiver Phone    : %s\n", s.getReceiverPhone());
                System.out.printf("Delivery Address  : %s\n", s.getDeliveryAddress());
                System.out.printf("Package Type      : %s\n", s.getPackageType());
                System.out.printf("Weight            : %.2f KG\n", s.getPackageWeight().doubleValue());
                System.out.printf("Delivery Status   : %s\n", s.getShipmentStatus());
                System.out.printf("Delivery Staff    : %s\n", (s.getStaffName() != null ? s.getStaffName() : "Not Assigned"));
                System.out.printf("Delivery Charge   : Rs. %.2f\n", s.getDeliveryCharge().doubleValue());
                System.out.printf("Expected Delivery : %s\n", s.getExpectedDeliveryDate());
                System.out.println("================================================");

                boolean viewHistory = ConsoleUtil.readConfirmation("View full tracking timeline audit history?");
                if (viewHistory) {
                    trackingService.displayTrackingFlow(trackingId);
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Search failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewPaymentDetails() {
        ConsoleUtil.printHeader("PAYMENT TRANSACTIONS");
        try {
            List<Payment> list = paymentService.getAllPayments();
            if (list.isEmpty()) {
                ConsoleUtil.printInfo("No payment records found.");
            } else {
                System.out.printf("%-6s | %-14s | %-10s | %-18s | %-10s | %-18s\n",
                    "Pay ID", "Tracking ID", "Amount", "Method", "Status", "Payment Date");
                System.out.println("-----------------------------------------------------------------------------------------");
                for (Payment p : list) {
                    System.out.printf("%-6d | %-14s | Rs. %-6.2f | %-18s | %-10s | %-18s\n",
                        p.getPaymentId(),
                        p.getTrackingId() != null ? p.getTrackingId() : ("Ship#" + p.getShipmentId()),
                        p.getAmount().doubleValue(),
                        p.getPaymentMethod(),
                        p.getPaymentStatus(),
                        p.getPaymentDate().toLocalDate().toString());
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Payment fetch error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void generateReports() {
        ConsoleUtil.printHeader("DELIVERY & LOGISTICS ANALYTICS REPORT");
        try {
            int total = shipmentService.countTotalShipments();
            int booked = shipmentService.countByStatus(Shipment.Status.BOOKED);
            int confirmed = shipmentService.countByStatus(Shipment.Status.CONFIRMED);
            int pickedUp = shipmentService.countByStatus(Shipment.Status.PICKED_UP);
            int inTransit = shipmentService.countByStatus(Shipment.Status.IN_TRANSIT);
            int outForDelivery = shipmentService.countByStatus(Shipment.Status.OUT_FOR_DELIVERY);
            int delivered = shipmentService.countByStatus(Shipment.Status.DELIVERED);
            int cancelled = shipmentService.countByStatus(Shipment.Status.CANCELLED);
            BigDecimal totalRev = paymentService.getTotalRevenue();

            System.out.println("--------------------------------------------------");
            System.out.printf("  Total Shipments Created    : %d\n", total);
            System.out.printf("  Delivered Shipments        : %d\n", delivered);
            System.out.printf("  In-Transit & Out Shipments : %d\n", (inTransit + outForDelivery + pickedUp));
            System.out.printf("  Pending / Booked Shipments : %d\n", (booked + confirmed));
            System.out.printf("  Cancelled Shipments        : %d\n", cancelled);
            System.out.printf("  Total Collected Revenue    : Rs. %.2f\n", totalRev.doubleValue());
            System.out.println("--------------------------------------------------");
            System.out.println("DELIVERY EFFICIENCY & COMPLETION RATIO:");
            if (total > 0) {
                double rate = ((double) delivered / total) * 100.0;
                System.out.printf("  Delivery Success Rate      : %.2f%%\n", rate);
            }
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            ConsoleUtil.printError("Report generation failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewDashboardStatistics() {
        ConsoleUtil.printHeader("LIVE DASHBOARD SYSTEM METRICS");
        try {
            int customers = customerDAO.countCustomers();
            int staff = deliveryService.countDeliveryStaff();
            int shipments = shipmentService.countTotalShipments();
            Map<String, Integer> summary = shipmentService.getStatusSummary();
            BigDecimal revenue = paymentService.getTotalRevenue();

            System.out.println("DATABASE AGGREGATE SUMMARY (SQL COUNT & SUM):");
            System.out.printf("• Registered Customers       : %d\n", customers);
            System.out.printf("• Active Delivery Personnel   : %d\n", staff);
            System.out.printf("• Lifetime Shipments Logged   : %d\n", shipments);
            System.out.printf("• Total Paid Revenue Settled  : Rs. %.2f\n", revenue.doubleValue());
            System.out.println("\nSHIPMENT BREAKDOWN BY STATUS (GROUP BY):");
            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                System.out.printf("  - %-18s : %d\n", entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to load statistics: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }
}
