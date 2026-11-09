package service;

import dao.PaymentDAO;
import dao.ShipmentDAO;
import dao.ShipmentTrackingDAO;
import database.DatabaseConnection;
import model.Payment;
import model.Shipment;
import model.ShipmentTracking;
import util.TrackingIdGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final ShipmentTrackingDAO trackingDAO = new ShipmentTrackingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public static final BigDecimal BASE_CHARGE = new BigDecimal("50.00");
    public static final BigDecimal RATE_PER_KG = new BigDecimal("40.00");

    public BigDecimal calculateDeliveryCharge(BigDecimal weight) {
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            return BASE_CHARGE;
        }
        return BASE_CHARGE.add(weight.multiply(RATE_PER_KG)).setScale(2, RoundingMode.HALF_UP);
    }

    public Shipment bookShipment(int customerId, String senderName, String senderAddress,
                                 String receiverName, String receiverPhone, String pickupAddress,
                                 String deliveryAddress, String packageDescription, BigDecimal weight,
                                 String packageType, int deliveryDays, Payment.PaymentMethod paymentMethod) throws Exception {

        String trackingId = TrackingIdGenerator.generateTrackingId();
        BigDecimal charge = calculateDeliveryCharge(weight);
        LocalDate expectedDelivery = LocalDate.now().plusDays(deliveryDays > 0 ? deliveryDays : 3);

        Shipment shipment = new Shipment();
        shipment.setTrackingId(trackingId);
        shipment.setCustomerId(customerId);
        shipment.setSenderName(senderName);
        shipment.setSenderAddress(senderAddress);
        shipment.setReceiverName(receiverName);
        shipment.setReceiverPhone(receiverPhone);
        shipment.setPickupAddress(pickupAddress);
        shipment.setDeliveryAddress(deliveryAddress);
        shipment.setPackageDescription(packageDescription);
        shipment.setPackageWeight(weight);
        shipment.setPackageType(packageType);
        shipment.setBookingDate(LocalDateTime.now());
        shipment.setExpectedDeliveryDate(expectedDelivery);
        shipment.setShipmentStatus(Shipment.Status.BOOKED);
        shipment.setDeliveryCharge(charge);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Insert shipment
            int shipmentId = shipmentDAO.insert(shipment);
            if (shipmentId <= 0) {
                conn.rollback();
                throw new SQLException("Failed to save shipment record.");
            }
            shipment.setShipmentId(shipmentId);

            // 2. Insert initial tracking log
            ShipmentTracking tracking = new ShipmentTracking(
                trackingId, shipmentId, Shipment.Status.BOOKED, 
                pickupAddress, "Shipment booked by customer."
            );
            trackingDAO.insertTracking(tracking);

            // 3. Create payment record
            Payment.PaymentStatus pStatus = (paymentMethod == Payment.PaymentMethod.ONLINE_PAYMENT) 
                ? Payment.PaymentStatus.PAID : Payment.PaymentStatus.PENDING;
            Payment payment = new Payment(shipmentId, charge, paymentMethod, pStatus);
            paymentDAO.insert(payment);

            conn.commit();
            return shipment;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<Shipment> getAllShipments() throws SQLException {
        return shipmentDAO.findAll();
    }

    public Optional<Shipment> getShipmentById(int shipmentId) throws SQLException {
        return shipmentDAO.findById(shipmentId);
    }

    public Optional<Shipment> getShipmentByTrackingId(String trackingId) throws SQLException {
        return shipmentDAO.findByTrackingId(trackingId);
    }

    public List<Shipment> getCustomerShipments(int customerId) throws SQLException {
        return shipmentDAO.findByCustomerId(customerId);
    }

    public List<Shipment> getStaffShipments(int staffId) throws SQLException {
        return shipmentDAO.findByStaffId(staffId);
    }

    public boolean assignStaff(int shipmentId, int staffId, String location) throws Exception {
        Optional<Shipment> opt = shipmentDAO.findById(shipmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Shipment ID " + shipmentId + " not found.");
        }

        Shipment shipment = opt.get();
        if (shipment.getShipmentStatus() == Shipment.Status.DELIVERED || shipment.getShipmentStatus() == Shipment.Status.CANCELLED) {
            throw new IllegalStateException("Cannot assign staff to a " + shipment.getShipmentStatus() + " shipment.");
        }

        boolean updated = shipmentDAO.assignStaff(shipmentId, staffId);
        if (updated) {
            ShipmentTracking tracking = new ShipmentTracking(
                shipment.getTrackingId(), shipmentId, Shipment.Status.CONFIRMED,
                location != null ? location : "Hub Sorting Center",
                "Delivery personnel assigned. Shipment confirmed for pickup."
            );
            trackingDAO.insertTracking(tracking);
        }
        return updated;
    }

    public boolean cancelShipment(int shipmentId, int customerId) throws Exception {
        Optional<Shipment> opt = shipmentDAO.findById(shipmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Shipment ID " + shipmentId + " not found.");
        }

        Shipment shipment = opt.get();
        if (customerId > 0 && shipment.getCustomerId() != customerId) {
            throw new SecurityException("Unauthorized: You can only cancel your own shipments.");
        }

        if (!shipment.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel shipment. Current status is '" + shipment.getShipmentStatus() + "'.");
        }

        boolean updated = shipmentDAO.updateStatus(shipmentId, Shipment.Status.CANCELLED);
        if (updated) {
            ShipmentTracking tracking = new ShipmentTracking(
                shipment.getTrackingId(), shipmentId, Shipment.Status.CANCELLED,
                "Customer Request", "Shipment cancelled by user/admin."
            );
            trackingDAO.insertTracking(tracking);
        }
        return updated;
    }

    public boolean deleteShipment(int shipmentId) throws SQLException {
        return shipmentDAO.delete(shipmentId);
    }

    public int countTotalShipments() throws SQLException {
        return shipmentDAO.countTotalShipments();
    }

    public int countByStatus(Shipment.Status status) throws SQLException {
        return shipmentDAO.countByStatus(status);
    }

    public Map<String, Integer> getStatusSummary() throws SQLException {
        return shipmentDAO.getStatusSummary();
    }
}
