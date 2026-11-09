package controller;

import dao.DeliveryStaffDAO;
import model.DeliveryStaff;
import model.Shipment;
import model.User;
import service.DeliveryService;
import service.ShipmentService;
import service.TrackingService;
import util.ConsoleUtil;

import java.util.List;
import java.util.Optional;

public class DeliveryStaffController {

    private final ShipmentService shipmentService = new ShipmentService();
    private final DeliveryService deliveryService = new DeliveryService();
    private final TrackingService trackingService = new TrackingService();
    private final DeliveryStaffDAO staffDAO = new DeliveryStaffDAO();

    public void handleStaffDashboard(User user) {
        DeliveryStaff staff = null;
        try {
            Optional<DeliveryStaff> staffOpt = staffDAO.findByUserId(user.getUserId());
            if (staffOpt.isEmpty()) {
                ConsoleUtil.printError("Delivery staff profile not found. Please contact Administrator.");
                return;
            }
            staff = staffOpt.get();
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to load staff profile: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            ConsoleUtil.printHeader("DELIVERY STAFF DASHBOARD");
            System.out.println("Courier Agent: " + staff.getStaffName() + " | Vehicle: " + staff.getVehicleNumber());
            System.out.println("Availability : " + staff.getAvailabilityStatus() + "\n");
            System.out.println("1. View Assigned Shipments");
            System.out.println("2. View Shipment Details");
            System.out.println("3. Update Delivery Status");
            System.out.println("4. View Delivery History");
            System.out.println("5. View Profile");
            System.out.println("6. Logout");

            int choice = ConsoleUtil.readInt("\nEnter your choice", 1, 6);
            switch (choice) {
                case 1 -> viewAssignedShipments(staff);
                case 2 -> viewShipmentDetails(staff);
                case 3 -> updateDeliveryStatus(staff);
                case 4 -> viewDeliveryHistory(staff);
                case 5 -> viewProfile(staff, user);
                case 6 -> {
                    ConsoleUtil.printInfo("Logging out of Delivery Staff Portal...");
                    running = false;
                }
            }
        }
    }

    private void viewAssignedShipments(DeliveryStaff staff) {
        ConsoleUtil.printHeader("MY ACTIVE ASSIGNED DELIVERIES");
        try {
            List<Shipment> list = shipmentService.getStaffShipments(staff.getStaffId());
            long activeCount = list.stream().filter(s -> s.getShipmentStatus() != Shipment.Status.DELIVERED && s.getShipmentStatus() != Shipment.Status.CANCELLED).count();

            if (activeCount == 0) {
                ConsoleUtil.printInfo("You have no pending deliveries assigned currently.");
            } else {
                System.out.printf("%-4s | %-14s | %-15s | %-12s | %-16s | %-25s\n",
                    "ID", "Tracking ID", "Receiver", "Phone", "Status", "Delivery Address");
                System.out.println("---------------------------------------------------------------------------------------------------------");
                for (Shipment s : list) {
                    if (s.getShipmentStatus() != Shipment.Status.DELIVERED && s.getShipmentStatus() != Shipment.Status.CANCELLED) {
                        System.out.printf("%-4d | %-14s | %-15s | %-12s | %-16s | %-25s\n",
                            s.getShipmentId(),
                            s.getTrackingId(),
                            s.getReceiverName(),
                            s.getReceiverPhone(),
                            s.getShipmentStatus(),
                            s.getDeliveryAddress().length() > 25 ? s.getDeliveryAddress().substring(0, 22) + "..." : s.getDeliveryAddress());
                    }
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to fetch assigned shipments: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewShipmentDetails(DeliveryStaff staff) {
        ConsoleUtil.printHeader("VIEW SHIPMENT DETAILS");
        String trackingId = ConsoleUtil.readRequiredString("Enter Tracking ID to inspect");
        try {
            Optional<Shipment> shipOpt = shipmentService.getShipmentByTrackingId(trackingId);
            if (shipOpt.isEmpty()) {
                ConsoleUtil.printError("Shipment ID not found.");
            } else {
                Shipment s = shipOpt.get();
                if (s.getStaffId() == null || s.getStaffId().intValue() != staff.getStaffId()) {
                    ConsoleUtil.printWarning("Note: This shipment is currently not assigned to you.");
                }
                System.out.println();
                System.out.println("================================================");
                System.out.println("               SHIPMENT DETAILS                 ");
                System.out.println("================================================");
                System.out.printf("Shipment ID       : %d\n", s.getShipmentId());
                System.out.printf("Tracking ID       : %s\n", s.getTrackingId());
                System.out.printf("Customer Name     : %s\n", s.getCustomerName());
                System.out.printf("Sender Name       : %s\n", s.getSenderName());
                System.out.printf("Sender Address    : %s\n", s.getSenderAddress());
                System.out.printf("Receiver Name     : %s\n", s.getReceiverName());
                System.out.printf("Receiver Phone    : %s\n", s.getReceiverPhone());
                System.out.printf("Pickup Address    : %s\n", s.getPickupAddress());
                System.out.printf("Delivery Address  : %s\n", s.getDeliveryAddress());
                System.out.printf("Package Weight    : %.2f KG\n", s.getPackageWeight().doubleValue());
                System.out.printf("Delivery Status   : %s\n", s.getShipmentStatus());
                System.out.printf("Delivery Charge   : Rs. %.2f\n", s.getDeliveryCharge().doubleValue());
                System.out.printf("Expected Delivery : %s\n", s.getExpectedDeliveryDate());
                System.out.println("================================================");

                boolean viewHistory = ConsoleUtil.readConfirmation("View full tracking history?");
                if (viewHistory) {
                    trackingService.displayTrackingFlow(trackingId);
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void updateDeliveryStatus(DeliveryStaff staff) {
        ConsoleUtil.printHeader("UPDATE SHIPMENT DELIVERY STATUS");
        int shipmentId = ConsoleUtil.readInt("Enter Shipment ID to update", 1, 999999);
        try {
            Optional<Shipment> opt = shipmentService.getShipmentById(shipmentId);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("Shipment ID not found.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            Shipment s = opt.get();
            if (s.getStaffId() == null || s.getStaffId().intValue() != staff.getStaffId()) {
                ConsoleUtil.printError("Unauthorized: You can only update shipments assigned to your staff account.");
                ConsoleUtil.pressEnterToContinue();
                return;
            }

            System.out.println("Tracking ID     : " + s.getTrackingId());
            System.out.println("Current Status  : " + s.getShipmentStatus());
            System.out.println("Receiver Name   : " + s.getReceiverName() + " (" + s.getReceiverPhone() + ")");
            System.out.println("Delivery Address: " + s.getDeliveryAddress());

            System.out.println("\nSelect Next Progression Status:");
            System.out.println("1. PICKED_UP       (Package collected from sender)");
            System.out.println("2. IN_TRANSIT      (Moving through sorting facilities)");
            System.out.println("3. OUT_FOR_DELIVERY(Courier out for final handover)");
            System.out.println("4. DELIVERED       (Handed over to receiver)");
            int choice = ConsoleUtil.readInt("Choice", 1, 4);

            Shipment.Status next = switch (choice) {
                case 1 -> Shipment.Status.PICKED_UP;
                case 2 -> Shipment.Status.IN_TRANSIT;
                case 3 -> Shipment.Status.OUT_FOR_DELIVERY;
                default -> Shipment.Status.DELIVERED;
            };

            String location = ConsoleUtil.readRequiredString("Current Location / Checkpoint");
            String remarks = ConsoleUtil.readString("Operational Remarks (e.g. Package inspected and secured in van)");

            boolean updated = deliveryService.updateShipmentStatus(shipmentId, next, location, remarks, staff.getStaffId());
            if (updated) {
                ConsoleUtil.printSuccess("Status updated to " + next + " and tracking audit trail recorded!");
                if (next == Shipment.Status.DELIVERED) {
                    ConsoleUtil.printSuccess("Delivery completed! If this was Cash On Delivery, payment has been marked as PAID.");
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Update failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewDeliveryHistory(DeliveryStaff staff) {
        ConsoleUtil.printHeader("MY COMPLETED DELIVERIES");
        try {
            List<Shipment> list = shipmentService.getStaffShipments(staff.getStaffId());
            long deliveredCount = list.stream().filter(s -> s.getShipmentStatus() == Shipment.Status.DELIVERED).count();

            if (deliveredCount == 0) {
                ConsoleUtil.printInfo("No completed deliveries recorded yet.");
            } else {
                System.out.printf("%-4s | %-14s | %-15s | %-12s | %-16s | %-12s\n",
                    "ID", "Tracking ID", "Receiver", "Weight", "Charge", "Status");
                System.out.println("----------------------------------------------------------------------------");
                for (Shipment s : list) {
                    if (s.getShipmentStatus() == Shipment.Status.DELIVERED) {
                        System.out.printf("%-4d | %-14s | %-15s | %-4.1f KG | Rs. %-6.2f | %-12s\n",
                            s.getShipmentId(),
                            s.getTrackingId(),
                            s.getReceiverName(),
                            s.getPackageWeight().doubleValue(),
                            s.getDeliveryCharge().doubleValue(),
                            s.getShipmentStatus());
                    }
                }
                System.out.println("\nTotal Completed Deliveries: " + deliveredCount);
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to fetch history: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewProfile(DeliveryStaff staff, User user) {
        ConsoleUtil.printHeader("DELIVERY STAFF PROFILE");
        System.out.println("Staff ID       : " + staff.getStaffId());
        System.out.println("Username       : " + user.getUsername());
        System.out.println("Full Name      : " + staff.getStaffName());
        System.out.println("Phone Number   : " + staff.getPhone());
        System.out.println("Vehicle Number : " + staff.getVehicleNumber());
        System.out.println("Duty Status    : " + staff.getAvailabilityStatus());
        System.out.println("Email Address  : " + user.getEmail());
        ConsoleUtil.pressEnterToContinue();
    }
}
