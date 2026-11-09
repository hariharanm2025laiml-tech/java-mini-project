package model;

import java.time.LocalDateTime;

public class ShipmentTracking {
    private int trackingEntryId;
    private String trackingId;
    private int shipmentId;
    private Shipment.Status shipmentStatus;
    private String location;
    private String remarks;
    private LocalDateTime updatedAt;

    public ShipmentTracking() {
        this.updatedAt = LocalDateTime.now();
    }

    public ShipmentTracking(String trackingId, int shipmentId, Shipment.Status shipmentStatus, 
                            String location, String remarks) {
        this.trackingId = trackingId;
        this.shipmentId = shipmentId;
        this.shipmentStatus = shipmentStatus;
        this.location = location;
        this.remarks = remarks;
        this.updatedAt = LocalDateTime.now();
    }

    public int getTrackingEntryId() {
        return trackingEntryId;
    }

    public void setTrackingEntryId(int trackingEntryId) {
        this.trackingEntryId = trackingEntryId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public int getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(int shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Shipment.Status getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(Shipment.Status shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] Status: %-16s | Location: %-25s | Remarks: %s",
                updatedAt.toString(), shipmentStatus, location, remarks);
    }
}
