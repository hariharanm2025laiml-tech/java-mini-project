package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static Properties properties = new Properties();

    private static String host = "localhost";
    private static String port = "3306";
    private static String dbName = "delivery_logistics_db";
    private static String username = "root";
    private static String password = "";

    static {
        loadConfiguration();
    }

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static void loadConfiguration() {
        // Step 1: Read classpath application.properties
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                properties.load(in);
                host = properties.getProperty("db.host", host);
                port = properties.getProperty("db.port", port);
                dbName = properties.getProperty("db.name", dbName);
                username = properties.getProperty("db.username", username);
                password = properties.getProperty("db.password", password);
            }
        } catch (Exception ignored) {
        }

        // Step 2: Read .env in working directory if available
        File envFile = new File(".env");
        if (envFile.exists() && envFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(envFile)) {
                Properties envProps = new Properties();
                envProps.load(fis);
                if (envProps.containsKey("DB_HOST")) host = envProps.getProperty("DB_HOST").trim();
                if (envProps.containsKey("DB_PORT")) port = envProps.getProperty("DB_PORT").trim();
                if (envProps.containsKey("DB_NAME")) dbName = envProps.getProperty("DB_NAME").trim();
                if (envProps.containsKey("DB_USERNAME")) username = envProps.getProperty("DB_USERNAME").trim();
                if (envProps.containsKey("DB_PASSWORD")) password = envProps.getProperty("DB_PASSWORD").trim();
            } catch (Exception ignored) {
            }
        }

        // Step 3: Check OS environment variables
        if (System.getenv("DB_HOST") != null) host = System.getenv("DB_HOST");
        if (System.getenv("DB_PORT") != null) port = System.getenv("DB_PORT");
        if (System.getenv("DB_NAME") != null) dbName = System.getenv("DB_NAME");
        if (System.getenv("DB_USERNAME") != null) username = System.getenv("DB_USERNAME");
        if (System.getenv("DB_PASSWORD") != null) password = System.getenv("DB_PASSWORD");

        // Load Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            host, port, dbName
        );
        return DriverManager.getConnection(url, username, password);
    }

    public Connection getServerConnection() throws SQLException {
        String url = String.format(
            "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            host, port
        );
        return DriverManager.getConnection(url, username, password);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static String getDbName() { return dbName; }
    public static String getHost() { return host; }
    public static String getPort() { return port; }
}
