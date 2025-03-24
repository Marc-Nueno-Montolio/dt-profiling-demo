package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExceptionGenerator {
    private static final Random RANDOM = new Random();
    
    // Generate various types of exceptions
    public Throwable generateException(String type) {
        if (type == null || type.trim().isEmpty()) {
            return new IllegalArgumentException("Exception type cannot be null or empty");
        }
        
        DatabaseManager.getInstance().logAction("EXCEPTION", "Generating " + type + " exception");
        
        switch (type.toLowerCase()) {
            case "nullpointer":
                String str = null;
                try {
                    str.length(); // This will throw NPE
                    return null; // Never reached
                } catch (NullPointerException e) {
                    return e;
                }
            case "outofmemory":
                try {
                    List<byte[]> list = new ArrayList<>();
                    while (true) {
                        list.add(new byte[10 * 1024 * 1024]); // 10MB each
                    }
                } catch (OutOfMemoryError e) {
                    return e;
                }
            case "stackoverflow":
                try {
                    recursiveMethod(1);
                    return null; // Never reached
                } catch (StackOverflowError e) {
                    return e;
                }
            case "arithmetic":
                try {
                    int result = 1 / 0; // This will throw ArithmeticException
                    return null; // Never reached
                } catch (ArithmeticException e) {
                    return e;
                }
            case "classcast":
                try {
                    Object obj = "Hello";
                    Integer num = (Integer) obj; // This will throw ClassCastException
                    return null; // Never reached
                } catch (ClassCastException e) {
                    return e;
                }
            case "indexoutofbounds":
                try {
                    int[] arr = new int[5];
                    arr[10] = 1; // This will throw IndexOutOfBoundsException
                    return null; // Never reached
                } catch (IndexOutOfBoundsException e) {
                    return e;
                }
            case "io":
                return new IOException("Simulated IO error occurred while reading file");
            case "sql":
                return new SQLException("Simulated database error: Connection refused");
            case "deep":
                try {
                    return generateDeepStackTrace(50);
                } catch (Throwable e) {
                    return e;
                }
            default:
                return new IllegalArgumentException("Unknown exception type: " + type);
        }
    }
    
    private static NullPointerException generateNullPointerException() {
        String str = null;
        // This will throw a NullPointerException
        str.length();
        return new NullPointerException(); // Never reached
    }
    
    private static OutOfMemoryError generateOutOfMemoryError() {
        try {
            List<byte[]> bytes = new ArrayList<>();
            // Try to allocate a very large array but not enough to crash the JVM
            int size = 100 * 1024 * 1024; // 100MB
            for (int i = 0; i < 10; i++) {
                bytes.add(new byte[size]);
            }
        } catch (OutOfMemoryError e) {
            return e;
        }
        
        return new OutOfMemoryError("Manually created OutOfMemoryError");
    }
    
    private static StackOverflowError generateStackOverflowError() {
        try {
            recurse(0);
        } catch (StackOverflowError e) {
            return e;
        }
        
        return new StackOverflowError("Manually created StackOverflowError");
    }
    
    private static void recurse(int depth) {
        // This will eventually cause a StackOverflowError
        recurse(depth + 1);
    }
    
    private static ArithmeticException generateArithmeticException() {
        int zero = 0;
        // This will throw an ArithmeticException
        int result = 100 / zero;
        return new ArithmeticException(); // Never reached
    }
    
    private static ClassCastException generateClassCastException() {
        Object obj = new Object();
        // This will throw a ClassCastException
        String str = (String) obj;
        return new ClassCastException(); // Never reached
    }
    
    private static IndexOutOfBoundsException generateIndexOutOfBoundsException() {
        int[] array = new int[5];
        // This will throw an IndexOutOfBoundsException
        int value = array[10];
        return new IndexOutOfBoundsException(); // Never reached
    }
    
    private static IOException generateIOException() {
        try {
            // Try to open a non-existent file
            File nonExistentFile = new File("/non-existent-path/non-existent-file.txt");
            FileInputStream fis = new FileInputStream(nonExistentFile);
            fis.close();
            return new IOException(); // Never reached
        } catch (IOException e) {
            return e;
        }
    }
    
    private static SQLException generateSQLException() {
        try {
            // Try to execute an invalid SQL query
            Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM non_existent_table");
            pstmt.executeQuery();
            return new SQLException(); // Never reached
        } catch (SQLException e) {
            return e;
        }
    }
    
    // Generate exception with a deep stack trace
    public static Throwable generateDeepStackTrace(int depth) {
        if (depth <= 0) {
            return new IllegalArgumentException("Depth must be greater than 0");
        }
        if (depth > 1000) {
            return new IllegalArgumentException("Depth cannot exceed 1000");
        }
        
        try {
            return recursiveMethod(depth);
        } catch (Throwable e) {
            return e;
        }
    }
    
    private static Throwable recursiveMethod(int depth) {
        if (depth <= 0) {
            throw new RuntimeException("Maximum recursion depth reached");
        }
        return recursiveMethod(depth - 1);
    }
} 