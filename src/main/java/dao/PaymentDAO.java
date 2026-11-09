package dao;

import database.DatabaseConnection;
import model.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO {

    private Payment mapResultSet(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setShipmentId(rs.getInt("shipment_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(Payment.PaymentMethod.valueOf(rs.getString("payment_method")));
        p.setPaymentStatus(Payment.PaymentStatus.valueOf(rs.getString("payment_status")));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
        try {
            p.setTrackingId(rs.getString("tracking_id"));
        } catch (SQLException ignored) {
        }
        return p;
    }

    private static final String BASE_SELECT = 
        "SELECT p.*, s.tracking_id FROM payments p " +
        "JOIN shipments s ON p.shipment_id = s.shipment_id ";

    public Optional<Payment> findByShipmentId(int shipmentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.shipment_id = ?";
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

    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY p.payment_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public int insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (shipment_id, amount, payment_method, payment_status, payment_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, p.getShipmentId());
            pstmt.setBigDecimal(2, p.getAmount());
            pstmt.setString(3, p.getPaymentMethod().name());
            pstmt.setString(4, p.getPaymentStatus().name());
            pstmt.setTimestamp(5, Timestamp.valueOf(p.getPaymentDate()));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setPaymentId(keys.getInt(1));
                        return p.getPaymentId();
                    }
                }
            }
        }
        return -1;
    }

    public boolean updateStatus(int paymentId, Payment.PaymentStatus status) throws SQLException {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, paymentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public BigDecimal calculateTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'PAID'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }
}
