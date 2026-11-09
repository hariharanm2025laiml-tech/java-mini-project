package dao;

import database.DatabaseConnection;
import model.DeliveryStaff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryStaffDAO {

    private DeliveryStaff mapResultSet(ResultSet rs) throws SQLException {
        DeliveryStaff staff = new DeliveryStaff();
        staff.setStaffId(rs.getInt("staff_id"));
        staff.setUserId(rs.getInt("user_id"));
        staff.setStaffName(rs.getString("staff_name"));
        staff.setPhone(rs.getString("phone"));
        staff.setVehicleNumber(rs.getString("vehicle_number"));
        staff.setAvailabilityStatus(DeliveryStaff.AvailabilityStatus.valueOf(rs.getString("availability_status")));
        try {
            staff.setUsername(rs.getString("username"));
            staff.setEmail(rs.getString("email"));
        } catch (SQLException ignored) {
        }
        return staff;
    }

    private static final String BASE_SELECT = 
        "SELECT ds.*, u.username, u.email FROM delivery_staff ds " +
        "JOIN users u ON ds.user_id = u.user_id ";

    public Optional<DeliveryStaff> findById(int staffId) throws SQLException {
        String sql = BASE_SELECT + "WHERE ds.staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<DeliveryStaff> findByUserId(int userId) throws SQLException {
        String sql = BASE_SELECT + "WHERE ds.user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<DeliveryStaff> findAll() throws SQLException {
        List<DeliveryStaff> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY ds.staff_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public int insert(DeliveryStaff staff) throws SQLException {
        String sql = "INSERT INTO delivery_staff (user_id, staff_name, phone, vehicle_number, availability_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, staff.getUserId());
            pstmt.setString(2, staff.getStaffName());
            pstmt.setString(3, staff.getPhone());
            pstmt.setString(4, staff.getVehicleNumber());
            pstmt.setString(5, staff.getAvailabilityStatus().name());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        staff.setStaffId(keys.getInt(1));
                        return staff.getStaffId();
                    }
                }
            }
        }
        return -1;
    }

    public boolean update(DeliveryStaff staff) throws SQLException {
        String sql = "UPDATE delivery_staff SET staff_name = ?, phone = ?, vehicle_number = ?, availability_status = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getStaffName());
            pstmt.setString(2, staff.getPhone());
            pstmt.setString(3, staff.getVehicleNumber());
            pstmt.setString(4, staff.getAvailabilityStatus().name());
            pstmt.setInt(5, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int staffId, DeliveryStaff.AvailabilityStatus status) throws SQLException {
        String sql = "UPDATE delivery_staff SET availability_status = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, staffId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int staffId) throws SQLException {
        // Need to delete underlying user account which cascades to delivery_staff
        String getUserIdSql = "SELECT user_id FROM delivery_staff WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getUserIdSql)) {
            pstmt.setInt(1, staffId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String delUserSql = "DELETE FROM users WHERE user_id = ?";
                    try (PreparedStatement delStmt = conn.prepareStatement(delUserSql)) {
                        delStmt.setInt(1, userId);
                        return delStmt.executeUpdate() > 0;
                    }
                }
            }
        }
        return false;
    }

    public int countDeliveryStaff() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_staff";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
