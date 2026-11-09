package dao;

import database.DatabaseConnection;
import model.Shipment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShipmentDAO {

    private Shipment mapResultSet(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setShipmentId(rs.getInt("shipment_id"));
        s.setTrackingId(rs.getString("tracking_id"));
        s.setCustomerId(rs.getInt("customer_id"));
        int staffId = rs.getInt("staff_id");
        s.setStaffId(rs.wasNull() ? null : staffId);

        s.setSenderName(rs.getString("sender_name"));
        s.setSenderAddress(rs.getString("sender_address"));
        s.setReceiverName(rs.getString("receiver_name"));
        s.setReceiverPhone(rs.getString("receiver_phone"));
        s.setPickupAddress(rs.getString("pickup_address"));
        s.setDeliveryAddress(rs.getString("delivery_address"));
        s.setPackageDescription(rs.getString("package_description"));
        s.setPackageWeight(rs.getBigDecimal("package_weight"));
        s.setPackageType(rs.getString("package_type"));

        Timestamp bookingDate = rs.getTimestamp("booking_date");
        if (bookingDate != null) s.setBookingDate(bookingDate.toLocalDateTime());

        Date expDate = rs.getDate("expected_delivery_date");
        if (expDate != null) s.setExpectedDeliveryDate(expDate.toLocalDate());

        s.setShipmentStatus(Shipment.Status.valueOf(rs.getString("shipment_status")));
        s.setDeliveryCharge(rs.getBigDecimal("delivery_charge"));

        // Join columns if present
        try {
            s.setCustomerName(rs.getString("customer_name"));
            s.setStaffName(rs.getString("staff_name"));
            s.setStaffPhone(rs.getString("staff_phone"));
            s.setVehicleNumber(rs.getString("vehicle_number"));
        } catch (SQLException ignored) {
        }

        return s;
    }

    private static final String BASE_SELECT = 
        "SELECT s.*, c.customer_name, ds.staff_name, ds.phone AS staff_phone, ds.vehicle_number " +
        "FROM shipments s " +
        "JOIN customers c ON s.customer_id = c.customer_id " +
        "LEFT JOIN delivery_staff ds ON s.staff_id = ds.staff_id ";

    public Optional<Shipment> findById(int shipmentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE s.shipment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, shipmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Shipment> findByTrackingId(String trackingId) throws SQLException {
        String sql = BASE_SELECT + "WHERE s.tracking_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trackingId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Shipment> findAll() throws SQLException {
        List<Shipment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY s.booking_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public List<Shipment> findByCustomerId(int customerId) throws SQLException {
        List<Shipment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE s.customer_id = ? ORDER BY s.booking_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Shipment> findByStaffId(int staffId) throws SQLException {
        List<Shipment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE s.staff_id = ? ORDER BY s.booking_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    public int insert(Shipment s) throws SQLException {
        String sql = "INSERT INTO shipments (tracking_id, customer_id, staff_id, sender_name, sender_address, " +
                     "receiver_name, receiver_phone, pickup_address, delivery_address, package_description, " +
                     "package_weight, package_type, booking_date, expected_delivery_date, shipment_status, delivery_charge) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, s.getTrackingId());
            pstmt.setInt(2, s.getCustomerId());
            if (s.getStaffId() != null && s.getStaffId() > 0) {
                pstmt.setInt(3, s.getStaffId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setString(4, s.getSenderName());
            pstmt.setString(5, s.getSenderAddress());
            pstmt.setString(6, s.getReceiverName());
            pstmt.setString(7, s.getReceiverPhone());
            pstmt.setString(8, s.getPickupAddress());
            pstmt.setString(9, s.getDeliveryAddress());
            pstmt.setString(10, s.getPackageDescription());
            pstmt.setBigDecimal(11, s.getPackageWeight());
            pstmt.setString(12, s.getPackageType());
            pstmt.setTimestamp(13, Timestamp.valueOf(s.getBookingDate()));
            pstmt.setDate(14, Date.valueOf(s.getExpectedDeliveryDate()));
            pstmt.setString(15, s.getShipmentStatus().name());
            pstmt.setBigDecimal(16, s.getDeliveryCharge());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        s.setShipmentId(keys.getInt(1));
                        return s.getShipmentId();
                    }
                }
            }
        }
        return -1;
    }

    public boolean updateStatus(int shipmentId, Shipment.Status status) throws SQLException {
        String sql = "UPDATE shipments SET shipment_status = ? WHERE shipment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, shipmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean assignStaff(int shipmentId, int staffId) throws SQLException {
        String sql = "UPDATE shipments SET staff_id = ?, shipment_status = 'CONFIRMED' WHERE shipment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.setInt(2, shipmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int shipmentId) throws SQLException {
        String sql = "DELETE FROM shipments WHERE shipment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, shipmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int countTotalShipments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM shipments";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByStatus(Shipment.Status status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM shipments WHERE shipment_status = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public Map<String, Integer> getStatusSummary() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT shipment_status, COUNT(*) AS cnt FROM shipments GROUP BY shipment_status";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("shipment_status"), rs.getInt("cnt"));
            }
        }
        return map;
    }
}
