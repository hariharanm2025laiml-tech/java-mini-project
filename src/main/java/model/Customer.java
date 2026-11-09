package model;

public class Customer {
    private int customerId;
    private int userId;
    private String customerName;
    private String phone;
    private String address;
    private String username;
    private String email;

    public Customer() {
    }

    public Customer(int customerId, int userId, String customerName, String phone, String address) {
        this.customerId = customerId;
        this.userId = userId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
    }

    public Customer(int userId, String customerName, String phone, String address) {
        this(0, userId, customerName, phone, address);
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        return String.format("Customer[ID=%d, Name='%s', Phone='%s', Address='%s']",
                customerId, customerName, phone, address);
    }
}
