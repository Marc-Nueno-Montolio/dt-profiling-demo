package com.example;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CpuLoadGenerator {
    private static final Random RANDOM = new Random();
    private static ExecutorService executorService;
    private static List<Future<?>> runningTasks = new ArrayList<>();
    
    // Initialize the thread pool
    static {
        int cores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cores);
    }
    
    // Generate CPU load with specified duration and intensity
    public static void generateLoad(int durationSeconds, int numThreads, String loadType) {
        DatabaseManager.getInstance().logAction("CPU_LOAD", 
                "Generating " + loadType + " CPU load for " + durationSeconds + 
                " seconds using " + numThreads + " threads");
        
        int cores = Runtime.getRuntime().availableProcessors();
        numThreads = Math.min(numThreads, cores * 2);
        
        for (int i = 0; i < numThreads; i++) {
            Future<?> task = executorService.submit(() -> {
                long endTime = System.currentTimeMillis() + (durationSeconds * 1000);
                
                while (System.currentTimeMillis() < endTime && !Thread.currentThread().isInterrupted()) {
                    switch (loadType) {
                        case "prime":
                            calculatePrimes();
                            break;
                        case "math":
                            performComplexMath();
                            break;
                        case "sort":
                            performSorting();
                            break;
                        default:
                            busyWait();
                    }
                }
            });
            
            runningTasks.add(task);
        }
    }
    
    // Calculate prime numbers
    private static void calculatePrimes() {
        int max = 100000 + RANDOM.nextInt(50000);
        for (int i = 2; i < max; i++) {
            boolean isPrime = true;
            for (int j = 2; j <= Math.sqrt(i); j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                // Found a prime number
            }
        }
    }
    
    // Perform complex mathematical calculations
    private static void performComplexMath() {
        BigDecimal value = new BigDecimal(RANDOM.nextDouble() + 1.0);
        MathContext mc = new MathContext(100);
        
        for (int i = 0; i < 10000; i++) {
            value = value.multiply(value, mc).add(value).divide(
                    BigDecimal.valueOf(RANDOM.nextDouble() + 1.0), mc);
            
            double sinValue = Math.sin(i * 0.01);
            double cosValue = Math.cos(i * 0.01);
            double tanValue = Math.tan(i * 0.01);
            
            // Perform more operations to consume CPU
            for (int j = 0; j < 100; j++) {
                Math.pow(sinValue + i, cosValue + j);
                Math.log(Math.abs(tanValue) + 1.0);
            }
        }
    }
    
    // Perform memory-intensive sorting operations
    private static void performSorting() {
        int size = 100000 + RANDOM.nextInt(50000);
        int[] array = new int[size];
        
        // Fill with random numbers
        for (int i = 0; i < size; i++) {
            array[i] = RANDOM.nextInt(1000000);
        }
        
        // Bubble sort (intentionally inefficient)
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }
    
    // Simple busy-wait to consume CPU
    private static void busyWait() {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 500) {
            // Busy wait
            for (int i = 0; i < 1000000; i++) {
                Math.sin(i);
            }
        }
    }
    
    // Stop all running CPU load tasks
    public static void stopLoad() {
        DatabaseManager.getInstance().logAction("CPU_LOAD_STOP", "Stopping CPU load");
        
        for (Future<?> task : runningTasks) {
            task.cancel(true);
        }
        
        runningTasks.clear();
    }
    
    // Shutdown the executor service
    public static void shutdown() {
        executorService.shutdownNow();
    }
} 