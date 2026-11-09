package service;

import dao.PaymentDAO;
import model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    public Optional<Payment> getPaymentByShipmentId(int shipmentId) throws SQLException {
        return paymentDAO.findByShipmentId(shipmentId);
    }

    public List<Payment> getAllPayments() throws SQLException {
        return paymentDAO.findAll();
    }

    public boolean updatePaymentStatus(int paymentId, Payment.PaymentStatus status) throws SQLException {
        return paymentDAO.updateStatus(paymentId, status);
    }

    public BigDecimal getTotalRevenue() throws SQLException {
        return paymentDAO.calculateTotalRevenue();
    }
}
