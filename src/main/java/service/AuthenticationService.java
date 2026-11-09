package service;

import dao.CustomerDAO;
import dao.DeliveryStaffDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.Customer;
import model.DeliveryStaff;
import model.User;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {

    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final DeliveryStaffDAO staffDAO = new DeliveryStaffDAO();

    public User login(String username, String password, User.Role expectedRole) throws Exception {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            throw new IllegalStateException("Your account has been deactivated. Contact the administrator.");
        }

        if (user.getRole() != expectedRole) {
            throw new IllegalArgumentException("Access denied: You cannot log into this portal with role '" + user.getRole() + "'.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return user;
    }

    public boolean registerCustomer(String username, String password, String email, 
                                    String fullName, String phone, String address) throws Exception {
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Create User
            String hashed = PasswordUtil.hashPassword(password);
            User user = new User(username, hashed, User.Role.CUSTOMER, email);
            int userId = userDAO.insert(user);
            if (userId <= 0) {
                conn.rollback();
                return false;
            }

            // 2. Create Customer Profile
            Customer customer = new Customer(userId, fullName, phone, address);
            int customerId = customerDAO.insert(customer);
            if (customerId <= 0) {
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

    public Optional<Customer> getCustomerByUserId(int userId) throws SQLException {
        return customerDAO.findByUserId(userId);
    }

    public Optional<DeliveryStaff> getStaffByUserId(int userId) throws SQLException {
        return staffDAO.findByUserId(userId);
    }
}
