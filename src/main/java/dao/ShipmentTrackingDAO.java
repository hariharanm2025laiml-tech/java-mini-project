package dao;

import database.DatabaseConnection;
import model.Shipment;
import model.ShipmentTracking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ShipmentTrackingDAO {

    public boolean insertTracking(ShipmentTracking t) throws SQLException {
        String sql = "INSERT INTO shipment_tracking (tracking_id, shipment_id, shipment_status, location, remarks, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getTrackingId());
            pstmt.setInt(2, t.getShipmentId());
            pstmt.setString(3, t.getShipmentStatus().name());
            pstmt.setString(4, t.getLocation());
            pstmt.setString(5, t.getRemarks());
            pstmt.setTimestamp(6, Timestamp.valueOf(t.getUpdatedAt()));
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<ShipmentTracking> getTrackingHistory(String trackingId) throws SQLException {
        List<ShipmentTracking> list = new ArrayList<>();
        String sql = "SELECT * FROM shipment_tracking WHERE tracking_id = ? ORDER BY updated_at ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trackingId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ShipmentTracking t = new ShipmentTracking();
                    t.setTrackingEntryId(rs.getInt("tracking_entry_id"));
                    t.setTrackingId(rs.getString("tracking_id"));
                    t.setShipmentId(rs.getInt("shipment_id"));
                    t.setShipmentStatus(Shipment.Status.valueOf(rs.getString("shipment_status")));
                    t.setLocation(rs.getString("location"));
                    t.setRemarks(rs.getString("remarks"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    if (ts != null) t.setUpdatedAt(ts.toLocalDateTime());
                    list.add(t);
                }
            }
        }
        return list;
    }
}
