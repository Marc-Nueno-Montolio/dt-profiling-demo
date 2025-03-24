package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:troubleshooting-lab.db";
    private static DatabaseManager instance;
    
    private Connection connection;
    
    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "username TEXT NOT NULL," +
                         "email TEXT NOT NULL," +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS log_entries (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "action_type TEXT NOT NULL," +
                         "details TEXT," +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Insert sample data if tables are empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                insertSampleUsers(conn);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private void insertSampleUsers(Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO users (username, email) VALUES (?, ?)")) {
            
            for (int i = 1; i <= 100; i++) {
                pstmt.setString(1, "user" + i);
                pstmt.setString(2, "user" + i + "@example.com");
                pstmt.executeUpdate();
            }
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    public void logAction(String actionType, String details) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO log_entries (action_type, details) VALUES (?, ?)")) {
            
            pstmt.setString(1, actionType);
            pstmt.setString(2, details);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Inefficient query for demonstrating DB performance issues
    public List<Map<String, Object>> runInefficentQuery(int iterations) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            for (int i = 0; i < iterations; i++) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM users, log_entries")) {
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        
                        for (int j = 1; j <= columnCount; j++) {
                            row.put(metaData.getColumnName(j), rs.getObject(j));
                        }
                        
                        results.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    // Method that leaks connections
    public void leakConnections(int count) {
        try {
            for (int i = 0; i < count; i++) {
                // Deliberately not closing these connections
                Connection leakedConn = DriverManager.getConnection(DB_URL);
                Statement stmt = leakedConn.createStatement();
                stmt.execute("SELECT * FROM users LIMIT 1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 