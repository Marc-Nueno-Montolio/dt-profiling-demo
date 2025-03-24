package com.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrashSimulator {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    // Simulate a process crash (will be triggered after a delay)
    public static void simulateCrash(String type, int delaySeconds) {
        DatabaseManager.getInstance().logAction("CRASH_SCHEDULED", 
                "Scheduling " + type + " crash in " + delaySeconds + " seconds");
        
        executorService.submit(() -> {
            try {
                Thread.sleep(delaySeconds * 1000L);
                
                switch (type) {
                    case "halt":
                        // Most severe - terminate the JVM without any cleanup
                        Runtime.getRuntime().halt(1);
                        break;
                        
                    case "exit":
                        // Exit with error code
                        System.exit(1);
                        break;
                        
                    case "oom":
                        // Try to cause OutOfMemoryError
                        byte[][] arrays = new byte[Integer.MAX_VALUE][];
                        for (int i = 0; i < Integer.MAX_VALUE; i++) {
                            arrays[i] = new byte[Integer.MAX_VALUE];
                        }
                        break;
                        
                    case "stackoverflow":
                        // Force a StackOverflowError
                        infiniteRecursion(1);
                        break;
                        
                    case "deadlock":
                        // Create a deadlock
                        createDeadlock();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private static void infiniteRecursion(int depth) {
        // This will eventually crash with StackOverflowError
        infiniteRecursion(depth + 1);
    }
    
    private static void createDeadlock() {
        final Object lock1 = new Object();
        final Object lock2 = new Object();
        
        // Thread 1 locks lock1 then tries to lock lock2
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                synchronized (lock2) {
                    System.out.println("Thread 1 has both locks");
                }
            }
        });
        
        // Thread 2 locks lock2 then tries to lock lock1
        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                synchronized (lock1) {
                    System.out.println("Thread 2 has both locks");
                }
            }
        });
        
        thread1.start();
        thread2.start();
    }
    
    // Cancel a scheduled crash
    public static void cancelCrash() {
        DatabaseManager.getInstance().logAction("CRASH_CANCELLED", "Cancelling scheduled crash");
        
        executorService.shutdownNow();
    }
} 