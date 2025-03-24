package com.example.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

@WebServlet("/database")
public class DatabaseServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final List<Connection> leakedConnections = new ArrayList<>();

    @Override
    public void init() {
        try {
            // Initialize H2 database and create test table
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INT AUTO_INCREMENT, data VARCHAR(255))");
                    // Insert some test data
                    for (int i = 0; i < 10000; i++) {
                        stmt.execute("INSERT INTO test_table (data) VALUES ('Test data " + i + "')");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        int duration = Integer.parseInt(request.getParameter("duration"));
        int connections = Integer.parseInt(request.getParameter("connections"));

        try {
            switch (type) {
                case "slow_query":
                    generateSlowQuery(response, duration);
                    break;
                case "connection_leak":
                    generateConnectionLeak(response, connections);
                    break;
                case "deadlock":
                    generateDeadlock(response, connections);
                    break;
                case "full_table_scan":
                    generateFullTableScan(response, duration);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid issue type");
            }
        } catch (Exception e) {
            response.getWriter().write("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    private void generateSlowQuery(HttpServletResponse response, int duration) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "SELECT t1.* FROM test_table t1 " +
                          "CROSS JOIN test_table t2 " +
                          "WHERE t1.data LIKE '%test%' " +
                          "GROUP BY t1.id " +
                          "HAVING COUNT(*) > 0 " +
                          "ORDER BY t1.data";

            long startTime = System.currentTimeMillis();
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(duration);
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next() && (System.currentTimeMillis() - startTime) < duration * 1000) {
                    // Process results slowly
                    Thread.sleep(100);
                }
            }
            response.getWriter().write("Slow query executed for " + duration + " seconds");
        } catch (Exception e) {
            response.getWriter().write("Error executing slow query: " + e.getMessage());
        }
    }

    private void generateConnectionLeak(HttpServletResponse response, int count) throws IOException {
        try {
            for (int i = 0; i < count; i++) {
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                leakedConnections.add(conn); // Deliberately not closing connections
            }
            response.getWriter().write("Leaked " + count + " database connections");
        } catch (Exception e) {
            response.getWriter().write("Error generating connection leak: " + e.getMessage());
        }
    }

    private void generateDeadlock(HttpServletResponse response, int count) throws IOException {
        List<Future<?>> tasks = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                final int threadId = i;
                tasks.add(executor.submit(() -> {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                        conn.setAutoCommit(false);
                        try (Statement stmt = conn.createStatement()) {
                            // Each thread locks rows in different order to create deadlock
                            int firstId = (threadId % 2 == 0) ? 1 : 2;
                            int secondId = (threadId % 2 == 0) ? 2 : 1;
                            
                            stmt.execute("UPDATE test_table SET data = 'locked' WHERE id = " + firstId);
                            Thread.sleep(1000); // Wait to increase deadlock chance
                            stmt.execute("UPDATE test_table SET data = 'locked' WHERE id = " + secondId);
                            
                            conn.commit();
                        }
                    } catch (Exception e) {
                        // Expected deadlock exception
                    }
                    return null;
                }));
            }
            
            // Wait for all tasks to complete
            for (Future<?> task : tasks) {
                try {
                    task.get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // Expected timeout/deadlock
                }
            }
            
            response.getWriter().write("Generated database deadlock with " + count + " connections");
        } catch (Exception e) {
            response.getWriter().write("Error generating deadlock: " + e.getMessage());
        }
    }

    private void generateFullTableScan(HttpServletResponse response, int duration) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "SELECT * FROM test_table WHERE data LIKE '%test%' ORDER BY data";
            
            long startTime = System.currentTimeMillis();
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(duration);
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next() && (System.currentTimeMillis() - startTime) < duration * 1000) {
                    // Process each row slowly to simulate a long-running full table scan
                    Thread.sleep(10);
                }
            }
            response.getWriter().write("Full table scan executed for " + duration + " seconds");
        } catch (Exception e) {
            response.getWriter().write("Error executing full table scan: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Clean up leaked connections
        for (Connection conn : leakedConnections) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
        leakedConnections.clear();
    }
} 