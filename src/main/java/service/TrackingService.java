package service;

import dao.ShipmentDAO;
import dao.ShipmentTrackingDAO;
import model.Shipment;
import model.ShipmentTracking;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TrackingService {

    private final ShipmentTrackingDAO trackingDAO = new ShipmentTrackingDAO();
    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public List<ShipmentTracking> getTrackingHistory(String trackingId) throws SQLException {
        return trackingDAO.getTrackingHistory(trackingId);
    }

    public void displayTrackingFlow(String trackingId) throws SQLException {
        Optional<Shipment> shipOpt = shipmentDAO.findByTrackingId(trackingId);
        if (shipOpt.isEmpty()) {
            System.out.println("\n[ERROR] No shipment found with Tracking ID: " + trackingId);
            return;
        }

        Shipment shipment = shipOpt.get();
        List<ShipmentTracking> history = trackingDAO.getTrackingHistory(trackingId);

        System.out.println();
        System.out.println("==================================================");
        System.out.println("               SHIPMENT TRACKING                  ");
        System.out.println("==================================================");
        System.out.println("Tracking ID     : " + shipment.getTrackingId());
        System.out.println("Current Status  : " + shipment.getShipmentStatus());
        System.out.println("Sender          : " + shipment.getSenderName());
        System.out.println("Receiver        : " + shipment.getReceiverName() + " (" + shipment.getReceiverPhone() + ")");
        System.out.println("Delivery Address: " + shipment.getDeliveryAddress());
        System.out.println("Expected Date   : " + shipment.getExpectedDeliveryDate());
        System.out.println("--------------------------------------------------");
        System.out.println("STATUS HISTORY & AUDIT TRAIL:");
        System.out.println("--------------------------------------------------");

        if (history.isEmpty()) {
            System.out.println("No status transition history recorded yet.");
        } else {
            for (int i = 0; i < history.size(); i++) {
                ShipmentTracking t = history.get(i);
                String formattedTime = t.getUpdatedAt() != null ? t.getUpdatedAt().format(FORMATTER) : "N/A";
                System.out.printf("[%s]\n", t.getShipmentStatus());
                System.out.printf("  Time    : %s\n", formattedTime);
                System.out.printf("  Location: %s\n", t.getLocation());
                System.out.printf("  Remarks : %s\n", t.getRemarks());

                if (i < history.size() - 1) {
                    System.out.println("  |");
                    System.out.println("  v");
                }
            }
        }
        System.out.println("==================================================");
    }
}
