package service;

import dao.DeliveryStaffDAO;
import dao.PaymentDAO;
import dao.ShipmentDAO;
import dao.ShipmentTrackingDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.DeliveryStaff;
import model.Payment;
import model.Shipment;
import model.ShipmentTracking;
import model.User;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DeliveryService {

    private final DeliveryStaffDAO staffDAO = new DeliveryStaffDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final ShipmentTrackingDAO trackingDAO = new ShipmentTrackingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public boolean addDeliveryStaff(String username, String password, String email,
                                    String name, String phone, String vehicleNumber) throws Exception {
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Create User
            String hashed = PasswordUtil.hashPassword(password);
            User user = new User(username, hashed, User.Role.DELIVERY_STAFF, email);
            int userId = userDAO.insert(user);
            if (userId <= 0) {
                conn.rollback();
                return false;
            }

            // 2. Create Staff Profile
            DeliveryStaff staff = new DeliveryStaff(0, userId, name, phone, vehicleNumber, DeliveryStaff.AvailabilityStatus.AVAILABLE);
            int staffId = staffDAO.insert(staff);
            if (staffId <= 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
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

    public List<DeliveryStaff> getAllStaff() throws SQLException {
        return staffDAO.findAll();
    }

    public Optional<DeliveryStaff> getStaffById(int staffId) throws SQLException {
        return staffDAO.findById(staffId);
    }

    public boolean updateStaff(DeliveryStaff staff) throws SQLException {
        return staffDAO.update(staff);
    }

    public boolean deleteStaff(int staffId) throws SQLException {
        return staffDAO.delete(staffId);
    }

    public int countDeliveryStaff() throws SQLException {
        return staffDAO.countDeliveryStaff();
    }

    public boolean updateShipmentStatus(int shipmentId, Shipment.Status newStatus, 
                                        String location, String remarks, Integer staffId) throws Exception {
        Optional<Shipment> opt = shipmentDAO.findById(shipmentId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Shipment ID " + shipmentId + " not found.");
        }

        Shipment shipment = opt.get();

        // Verification if called by staff member
        if (staffId != null && staffId > 0) {
            if (shipment.getStaffId() == null || shipment.getStaffId().intValue() != staffId.intValue()) {
                throw new SecurityException("Unauthorized: You are only allowed to update shipments assigned to you.");
            }
        }

        // Validate state transitions
        validateStatusTransition(shipment.getShipmentStatus(), newStatus);

        boolean updated = shipmentDAO.updateStatus(shipmentId, newStatus);
        if (updated) {
            // Insert tracking entry
            ShipmentTracking tracking = new ShipmentTracking(
                shipment.getTrackingId(), shipmentId, newStatus,
                (location != null && !location.trim().isEmpty()) ? location.trim() : "Logistics Center",
                (remarks != null && !remarks.trim().isEmpty()) ? remarks.trim() : ("Status transitioned to " + newStatus)
            );
            trackingDAO.insertTracking(tracking);

            // If DELIVERED, automatically update payment if pending
            if (newStatus == Shipment.Status.DELIVERED) {
                Optional<Payment> payOpt = paymentDAO.findByShipmentId(shipmentId);
                if (payOpt.isPresent() && payOpt.get().getPaymentStatus() == Payment.PaymentStatus.PENDING) {
                    paymentDAO.updateStatus(payOpt.get().getPaymentId(), Payment.PaymentStatus.PAID);
                }
            }
        }
        return updated;
    }

    private void validateStatusTransition(Shipment.Status current, Shipment.Status next) {
        if (current == Shipment.Status.DELIVERED) {
            throw new IllegalStateException("Shipment has already been delivered. No further status changes are permitted.");
        }
        if (current == Shipment.Status.CANCELLED) {
            throw new IllegalStateException("Shipment is cancelled. No status changes are permitted.");
        }

        // Allowed sequential progression
        switch (current) {
            case BOOKED:
                if (next != Shipment.Status.CONFIRMED && next != Shipment.Status.CANCELLED) {
                    throw new IllegalStateException("A BOOKED shipment must be CONFIRMED before advancing.");
                }
                break;
            case CONFIRMED:
                if (next != Shipment.Status.PICKED_UP && next != Shipment.Status.CANCELLED) {
                    throw new IllegalStateException("A CONFIRMED shipment must be marked as PICKED_UP next.");
                }
                break;
            case PICKED_UP:
                if (next != Shipment.Status.IN_TRANSIT) {
                    throw new IllegalStateException("A PICKED_UP shipment must move to IN_TRANSIT next.");
                }
                break;
            case IN_TRANSIT:
                if (next != Shipment.Status.OUT_FOR_DELIVERY) {
                    throw new IllegalStateException("An IN_TRANSIT shipment must move to OUT_FOR_DELIVERY next.");
                }
                break;
            case OUT_FOR_DELIVERY:
                if (next != Shipment.Status.DELIVERED) {
                    throw new IllegalStateException("An OUT_FOR_DELIVERY shipment must be marked DELIVERED next.");
                }
                break;
            default:
                break;
        }
    }
}
