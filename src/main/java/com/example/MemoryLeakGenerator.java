package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemoryLeakGenerator {
    // Static collection that will grow without bounds
    private static final List<Object> leakedObjects = new ArrayList<>();
    
    // Map to store large objects with random keys
    private static final Map<String, byte[]> largeObjectMap = new HashMap<>();
    
    // Create a memory leak by adding objects to a static collection
    public static void leakMemory(int megabytes) {
        DatabaseManager.getInstance().logAction("MEMORY_LEAK", "Leaking " + megabytes + " MB");
        
        // Each byte array will be 1MB
        int arraySize = 1024 * 1024;
        
        for (int i = 0; i < megabytes; i++) {
            byte[] leakyArray = new byte[arraySize];
            // Fill with random data to ensure it's not optimized away
            for (int j = 0; j < arraySize; j += 1024) {
                leakyArray[j] = (byte) (j % 256);
            }
            leakedObjects.add(leakyArray);
        }
    }
    
    // Create a memory leak with key-value pairs
    public static void leakMemoryWithMap(int count, int sizeInKb) {
        DatabaseManager.getInstance().logAction("MEMORY_LEAK_MAP", 
                "Leaking " + count + " objects of " + sizeInKb + " KB each");
        
        int arraySize = sizeInKb * 1024;
        
        for (int i = 0; i < count; i++) {
            String key = UUID.randomUUID().toString();
            byte[] value = new byte[arraySize];
            
            // Fill with some data
            for (int j = 0; j < arraySize; j += 1024) {
                value[j] = (byte) (Math.random() * 256);
            }
            
            largeObjectMap.put(key, value);
        }
    }
    
    // Create a classloader leak (more advanced)
    public static void classLoaderLeak() {
        DatabaseManager.getInstance().logAction("CLASSLOADER_LEAK", "Creating classloader leak");
        
        try {
            // Create a custom classloader that will be leaked
            ClassLoader leakyLoader = new ClassLoader(MemoryLeakGenerator.class.getClassLoader()) {
                // Override nothing, just a unique instance that won't be garbage collected
            };
            
            // Create a large object and put it in a static collection
            byte[] largeArray = new byte[10 * 1024 * 1024]; // 10MB
            
            // Anonymous class that holds a reference to the classloader
            Object leakyObject = new Object() {
                private final ClassLoader loader = leakyLoader;
                private final byte[] data = largeArray;
                
                @Override
                protected void finalize() throws Throwable {
                    // Intentionally empty to show in heap dump
                    super.finalize();
                }
            };
            
            leakedObjects.add(leakyObject);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Method to clear memory leaks for recovery
    public static void cleanup() {
        DatabaseManager.getInstance().logAction("MEMORY_CLEANUP", "Clearing leaked memory");
        leakedObjects.clear();
        largeObjectMap.clear();
        System.gc(); // Suggest garbage collection
    }
} 