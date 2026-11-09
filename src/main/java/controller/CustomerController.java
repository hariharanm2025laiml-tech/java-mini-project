package controller;

import dao.CustomerDAO;
import model.Customer;
import model.Payment;
import model.Shipment;
import model.User;
import service.PaymentService;
import service.ShipmentService;
import service.TrackingService;
import util.ConsoleUtil;
import util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CustomerController {

    private final ShipmentService shipmentService = new ShipmentService();
    private final TrackingService trackingService = new TrackingService();
    private final PaymentService paymentService = new PaymentService();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public void handleCustomerDashboard(User user) {
        Customer customer = null;
        try {
            Optional<Customer> custOpt = customerDAO.findByUserId(user.getUserId());
            if (custOpt.isEmpty()) {
                ConsoleUtil.printError("Customer profile not linked with user account. Please contact support.");
                return;
            }
            customer = custOpt.get();
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to load customer profile: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            ConsoleUtil.printHeader("CUSTOMER DASHBOARD");
            System.out.println("Welcome, " + customer.getCustomerName() + " | Phone: " + customer.getPhone() + "\n");
            System.out.println("1. Book New Delivery");
            System.out.println("2. View My Shipments");
            System.out.println("3. Track My Shipment");
            System.out.println("4. View Shipment Details");
            System.out.println("5. Cancel Shipment");
            System.out.println("6. View Payment Details");
            System.out.println("7. View Profile");
            System.out.println("8. Logout");

            int choice = ConsoleUtil.readInt("\nEnter your choice", 1, 8);
            switch (choice) {
                case 1 -> bookNewDelivery(customer);
                case 2 -> viewMyShipments(customer);
                case 3 -> trackMyShipment(customer);
                case 4 -> viewShipmentDetails(customer);
                case 5 -> cancelShipment(customer);
                case 6 -> viewPaymentDetails(customer);
                case 7 -> viewProfile(customer, user);
                case 8 -> {
                    ConsoleUtil.printInfo("Logging out of Customer Portal...");
                    running = false;
                }
            }
        }
    }

    private void bookNewDelivery(Customer customer) {
        ConsoleUtil.printHeader("BOOK NEW DELIVERY SHIPMENT");
        System.out.println("Rate Plan: Base Charge Rs. 50 + Rs. 40/KG\n");

        String senderName = ConsoleUtil.readRequiredString("Sender Name (default: " + customer.getCustomerName() + ")");
        if (senderName.isEmpty()) senderName = customer.getCustomerName();

        String senderAddress = ConsoleUtil.readRequiredString("Sender Full Address");
        String receiverName = ConsoleUtil.readRequiredString("Receiver Name");
        String receiverPhone = ConsoleUtil.readRequiredString("Receiver Phone Number");
        String pickupAddress = ConsoleUtil.readRequiredString("Pickup Address");
        String deliveryAddress = ConsoleUtil.readRequiredString("Delivery Destination Address");
        String packageDescription = ConsoleUtil.readRequiredString("Package Description (e.g. Laptop in bubble wrap)");
        double weightVal = ConsoleUtil.readPositiveDouble("Package Weight in KG (e.g. 1.5)");
        BigDecimal weight = BigDecimal.valueOf(weightVal);

        System.out.println("\nSelect Package Classification:");
        System.out.println("1. Documents / Envelopes");
        System.out.println("2. Electronics / Gadgets");
        System.out.println("3. Apparel / Fashion");
        System.out.println("4. Fragile Items / Glassware");
        System.out.println("5. General Box / Parcel");
        int typeChoice = ConsoleUtil.readInt("Choice", 1, 5);
        String packageType = switch (typeChoice) {
            case 1 -> "Documents";
            case 2 -> "Electronics";
            case 3 -> "Apparel";
            case 4 -> "Fragile";
            default -> "General Parcel";
        };

        BigDecimal estimatedCharge = shipmentService.calculateDeliveryCharge(weight);
        System.out.printf("\n--> Calculated Delivery Fee: Rs. %.2f\n", estimatedCharge.doubleValue());

        System.out.println("Select Payment Option:");
        System.out.println("1. Cash On Delivery (Pay upon arrival)");
        System.out.println("2. Online Payment (Debit/Credit/UPI Simulated)");
        int payChoice = ConsoleUtil.readInt("Choice", 1, 2);
        Payment.PaymentMethod method = (payChoice == 1) 
            ? Payment.PaymentMethod.CASH_ON_DELIVERY : Payment.PaymentMethod.ONLINE_PAYMENT;

        boolean confirm = ConsoleUtil.readConfirmation("Confirm shipment booking with charge Rs. " + estimatedCharge);
        if (!confirm) {
            ConsoleUtil.printWarning("Booking cancelled by customer.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        try {
            Shipment shipment = shipmentService.bookShipment(customer.getCustomerId(), senderName, senderAddress,
                receiverName, receiverPhone, pickupAddress, deliveryAddress, packageDescription, weight,
                packageType, 3, method);

            ConsoleUtil.printSuccess("Shipment Booked Successfully!");
            System.out.println("Tracking ID      : " + shipment.getTrackingId());
            System.out.println("Status           : " + shipment.getShipmentStatus());
            System.out.println("Delivery Charge  : Rs. " + shipment.getDeliveryCharge());
            System.out.println("Expected By      : " + shipment.getExpectedDeliveryDate());
            System.out.println("Keep your Tracking ID safe to check delivery progress.");
        } catch (Exception e) {
            ConsoleUtil.printError("Booking failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewMyShipments(Customer customer) {
        ConsoleUtil.printHeader("MY REGISTERED SHIPMENTS");
        try {
            List<Shipment> list = shipmentService.getCustomerShipments(customer.getCustomerId());
            if (list.isEmpty()) {
                ConsoleUtil.printInfo("You have not booked any shipments yet.");
            } else {
                System.out.printf("%-4s | %-14s | %-15s | %-16s | %-7s | %-8s | %-12s\n",
                    "ID", "Tracking ID", "Receiver", "Status", "Weight", "Charge", "Expected");
                System.out.println("--------------------------------------------------------------------------------------------------");
                for (Shipment s : list) {
                    System.out.printf("%-4d | %-14s | %-15s | %-16s | %-4.1f KG | Rs. %-6.2f | %-12s\n",
                        s.getShipmentId(),
                        s.getTrackingId(),
                        s.getReceiverName(),
                        s.getShipmentStatus(),
                        s.getPackageWeight().doubleValue(),
                        s.getDeliveryCharge().doubleValue(),
                        s.getExpectedDeliveryDate().toString());
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to fetch shipments: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void trackMyShipment(Customer customer) {
        ConsoleUtil.printHeader("TRACK SHIPMENT");
        String trackingId = ConsoleUtil.readRequiredString("Enter Tracking ID (e.g. DLM2026100001)");
        try {
            Optional<Shipment> shipOpt = shipmentService.getShipmentByTrackingId(trackingId);
            if (shipOpt.isEmpty()) {
                ConsoleUtil.printError("Tracking ID not found.");
            } else if (shipOpt.get().getCustomerId() != customer.getCustomerId()) {
                ConsoleUtil.printError("Access Denied: You can only track your own shipments.");
            } else {
                trackingService.displayTrackingFlow(trackingId);
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Tracking error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewShipmentDetails(Customer customer) {
        ConsoleUtil.printHeader("VIEW SHIPMENT DETAILS");
        String trackingId = ConsoleUtil.readRequiredString("Enter Tracking ID");
        try {
            Optional<Shipment> shipOpt = shipmentService.getShipmentByTrackingId(trackingId);
            if (shipOpt.isEmpty() || shipOpt.get().getCustomerId() != customer.getCustomerId()) {
                ConsoleUtil.printError("Shipment not found under your account.");
            } else {
                Shipment s = shipOpt.get();
                System.out.println();
                System.out.println("================================================");
                System.out.println("               SHIPMENT DETAILS                 ");
                System.out.println("================================================");
                System.out.printf("Shipment ID       : %d\n", s.getShipmentId());
                System.out.printf("Tracking ID       : %s\n", s.getTrackingId());
                System.out.printf("Sender Name       : %s\n", s.getSenderName());
                System.out.printf("Sender Address    : %s\n", s.getSenderAddress());
                System.out.printf("Receiver Name     : %s\n", s.getReceiverName());
                System.out.printf("Receiver Phone    : %s\n", s.getReceiverPhone());
                System.out.printf("Pickup Address    : %s\n", s.getPickupAddress());
                System.out.printf("Delivery Address  : %s\n", s.getDeliveryAddress());
                System.out.printf("Package Type      : %s\n", s.getPackageType());
                System.out.printf("Weight            : %.2f KG\n", s.getPackageWeight().doubleValue());
                System.out.printf("Delivery Status   : %s\n", s.getShipmentStatus());
                System.out.printf("Delivery Staff    : %s\n", (s.getStaffName() != null ? s.getStaffName() : "Awaiting Assignment"));
                System.out.printf("Delivery Charge   : Rs. %.2f\n", s.getDeliveryCharge().doubleValue());
                System.out.printf("Expected Delivery : %s\n", s.getExpectedDeliveryDate());
                System.out.println("================================================");
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Error retrieving details: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void cancelShipment(Customer customer) {
        ConsoleUtil.printHeader("CANCEL SHIPMENT");
        int shipmentId = ConsoleUtil.readInt("Enter Shipment ID to cancel", 1, 999999);
        try {
            boolean confirm = ConsoleUtil.readConfirmation("Are you sure you want to cancel this booking?");
            if (confirm) {
                boolean cancelled = shipmentService.cancelShipment(shipmentId, customer.getCustomerId());
                if (cancelled) {
                    ConsoleUtil.printSuccess("Shipment cancelled successfully.");
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Cancellation failed: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewPaymentDetails(Customer customer) {
        ConsoleUtil.printHeader("MY PAYMENT RECORDS");
        try {
            List<Shipment> shipments = shipmentService.getCustomerShipments(customer.getCustomerId());
            if (shipments.isEmpty()) {
                ConsoleUtil.printInfo("No payment transactions recorded.");
            } else {
                System.out.printf("%-14s | %-15s | %-10s | %-18s | %-10s\n",
                    "Tracking ID", "Receiver", "Amount", "Method", "Payment Status");
                System.out.println("-------------------------------------------------------------------------------");
                for (Shipment s : shipments) {
                    Optional<Payment> payOpt = paymentService.getPaymentByShipmentId(s.getShipmentId());
                    if (payOpt.isPresent()) {
                        Payment p = payOpt.get();
                        System.out.printf("%-14s | %-15s | Rs. %-6.2f | %-18s | %-10s\n",
                            s.getTrackingId(),
                            s.getReceiverName(),
                            p.getAmount().doubleValue(),
                            p.getPaymentMethod(),
                            p.getPaymentStatus());
                    }
                }
            }
        } catch (Exception e) {
            ConsoleUtil.printError("Failed to fetch payments: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }

    private void viewProfile(Customer customer, User user) {
        ConsoleUtil.printHeader("MY CUSTOMER PROFILE");
        System.out.println("Customer ID  : " + customer.getCustomerId());
        System.out.println("Username     : " + user.getUsername());
        System.out.println("Full Name    : " + customer.getCustomerName());
        System.out.println("Email        : " + user.getEmail());
        System.out.println("Phone Number : " + customer.getPhone());
        System.out.println("Address      : " + customer.getAddress());
        System.out.println("Account Role : " + user.getRole());
        System.out.println("Member Since : " + (user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate() : "Recent"));
        ConsoleUtil.pressEnterToContinue();
    }
}
