package model;

public class DeliveryStaff {
    public enum AvailabilityStatus {
        AVAILABLE, BUSY, OFF_DUTY
    }

    private int staffId;
    private int userId;
    private String staffName;
    private String phone;
    private String vehicleNumber;
    private AvailabilityStatus availabilityStatus;
    private String username;
    private String email;

    public DeliveryStaff() {
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
    }

    public DeliveryStaff(int staffId, int userId, String staffName, String phone, 
                         String vehicleNumber, AvailabilityStatus availabilityStatus) {
        this.staffId = staffId;
        this.userId = userId;
        this.staffName = staffName;
        this.phone = phone;
        this.vehicleNumber = vehicleNumber;
        this.availabilityStatus = availabilityStatus;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("Staff[ID=%d, Name='%s', Phone='%s', Vehicle='%s', Status=%s]",
                staffId, staffName, phone, vehicleNumber, availabilityStatus);
    }
}
