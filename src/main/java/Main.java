import controller.AdminController;
import controller.CustomerController;
import controller.DeliveryStaffController;
import database.DatabaseInitializer;
import model.User;
import service.AuthenticationService;
import util.ConsoleUtil;
import util.InputValidator;

public class Main {

    private static final AuthenticationService authService = new AuthenticationService();
    private static final AdminController adminController = new AdminController();
    private static final CustomerController customerController = new CustomerController();
    private static final DeliveryStaffController staffController = new DeliveryStaffController();

    public static void main(String[] args) {
        // Step 1: Initialize Database and seed/verify default accounts
        System.out.println("==================================================");
        System.out.println("     DELIVERY & LOGISTICS MANAGEMENT SYSTEM       ");
        System.out.println("           Core Java & MySQL Edition              ");
        System.out.println("==================================================");

        boolean dbReady = DatabaseInitializer.initializeDatabase();
        if (!dbReady) {
            ConsoleUtil.printWarning("Database auto-initialization could not connect to MySQL.");
            ConsoleUtil.printInfo("Please ensure MySQL service is running on port 3306 and check application.properties or .env.");
            ConsoleUtil.printInfo("You can also run database.sql manually in MySQL Workbench or CLI.");
        }

        // Step 2: Main Application Menu Loop
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("    DELIVERY & LOGISTICS MANAGEMENT     ");
            System.out.println("                 SYSTEM                 ");
            System.out.println("========================================");
            System.out.println("1. Admin Login");
            System.out.println("2. Customer Login");
            System.out.println("3. Delivery Staff Login");
            System.out.println("4. Customer Registration");
            System.out.println("5. Exit");
            System.out.println("========================================");

            int choice = ConsoleUtil.readInt("Enter your choice", 1, 5);
            switch (choice) {
                case 1 -> handleLogin(User.Role.ADMIN);
                case 2 -> handleLogin(User.Role.CUSTOMER);
                case 3 -> handleLogin(User.Role.DELIVERY_STAFF);
                case 4 -> handleCustomerRegistration();
                case 5 -> {
                    System.out.println("\nThank you for using the Delivery & Logistics Management System!");
                    System.out.println("System shutting down. Goodbye!");
                    running = false;
                }
            }
        }
    }

    private static void handleLogin(User.Role role) {
        String roleTitle = switch (role) {
            case ADMIN -> "ADMIN LOGIN";
            case CUSTOMER -> "CUSTOMER LOGIN";
            case DELIVERY_STAFF -> "DELIVERY STAFF LOGIN";
        };

        ConsoleUtil.printHeader(roleTitle);
        System.out.println("Default Demo Credentials:");
        switch (role) {
            case ADMIN -> System.out.println("• Username: admin    | Password: Admin@123\n");
            case CUSTOMER -> System.out.println("• Username: customer | Password: Customer@123\n");
            case DELIVERY_STAFF -> System.out.println("• Username: staff    | Password: Staff@123\n");
        }

        String username = ConsoleUtil.readRequiredString("Enter Username");
        String password = ConsoleUtil.readRequiredString("Enter Password");

        try {
            User user = authService.login(username, password, role);
            ConsoleUtil.printSuccess("Login Successful! Welcome, " + user.getUsername() + ".");

            // Route to respective role controller
            switch (role) {
                case ADMIN -> adminController.handleAdminDashboard(user);
                case CUSTOMER -> customerController.handleCustomerDashboard(user);
                case DELIVERY_STAFF -> staffController.handleStaffDashboard(user);
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            ConsoleUtil.printError(e.getMessage());
            ConsoleUtil.pressEnterToContinue();
        } catch (Exception e) {
            ConsoleUtil.printError("Database communication error: " + e.getMessage());
            ConsoleUtil.printInfo("Please check if MySQL is running on port 3306.");
            ConsoleUtil.pressEnterToContinue();
        }
    }

    private static void handleCustomerRegistration() {
        ConsoleUtil.printHeader("CUSTOMER REGISTRATION");

        String username = ConsoleUtil.readRequiredString("Desired Username");
        if (!InputValidator.isValidUsername(username)) {
            ConsoleUtil.printError("Username must be between 3 and 30 alphanumeric characters.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String password = ConsoleUtil.readRequiredString("Password (minimum 6 characters)");
        if (!InputValidator.isValidPassword(password)) {
            ConsoleUtil.printError("Password must be at least 6 characters.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String confirmPassword = ConsoleUtil.readRequiredString("Confirm Password");
        if (!password.equals(confirmPassword)) {
            ConsoleUtil.printError("Passwords do not match. Please try again.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String email = ConsoleUtil.readRequiredString("Email Address");
        if (!InputValidator.isValidEmail(email)) {
            ConsoleUtil.printError("Invalid email address format.");
            ConsoleUtil.pressEnterToContinue();
            return;
        }

        String fullName = ConsoleUtil.readRequiredString("Full Name");
        String phone = ConsoleUtil.readRequiredString("Phone Number");
        String address = ConsoleUtil.readRequiredString("Complete Delivery / Residence Address");

        try {
            boolean registered = authService.registerCustomer(username, password, email, fullName, phone, address);
            if (registered) {
                ConsoleUtil.printSuccess("Registration Successful! Your customer account has been created.");
                ConsoleUtil.printInfo("You can now log in using option 2 (Customer Login).");
            } else {
                ConsoleUtil.printError("Registration could not be completed. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            ConsoleUtil.printError(e.getMessage());
        } catch (Exception e) {
            ConsoleUtil.printError("Registration failed due to database error: " + e.getMessage());
        }
        ConsoleUtil.pressEnterToContinue();
    }
}
