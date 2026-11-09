package dao;

import database.DatabaseConnection;
import model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {

    private Customer mapResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setUserId(rs.getInt("user_id"));
        customer.setCustomerName(rs.getString("customer_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        try {
            customer.setUsername(rs.getString("username"));
            customer.setEmail(rs.getString("email"));
        } catch (SQLException ignored) {
        }
        return customer;
    }

    private static final String BASE_SELECT = 
        "SELECT c.*, u.username, u.email FROM customers c " +
        "JOIN users u ON c.user_id = u.user_id ";

    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE c.customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByUserId(int userId) throws SQLException {
        String sql = BASE_SELECT + "WHERE c.user_id = ?";
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

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.customer_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public int insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (user_id, customer_name, phone, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, customer.getUserId());
            pstmt.setString(2, customer.getCustomerName());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getAddress());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        customer.setCustomerId(keys.getInt(1));
                        return customer.getCustomerId();
                    }
                }
            }
        }
        return -1;
    }

    public boolean update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET customer_name = ?, phone = ?, address = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getCustomerName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getAddress());
            pstmt.setInt(4, customer.getCustomerId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public int countCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
