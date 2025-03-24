package com.example.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.*;

@WebServlet("/performance")
public class PerformanceServlet extends HttpServlet {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean shouldStop = false;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        String durationStr = request.getParameter("duration");
        
        if (type == null || durationStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid duration");
            return;
        }

        switch (type) {
            case "memory":
                generateMemoryLeak(response, duration);
                break;
            case "cpu":
                generateHighCPU(response, duration);
                break;
            case "thread":
                generateThreadDeadlock(response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type");
        }
    }

    private void generateMemoryLeak(HttpServletResponse response, int duration) throws IOException {
        try {
            ConcurrentLinkedQueue<byte[]> leakedMemory = new ConcurrentLinkedQueue<>();
            long endTime = System.currentTimeMillis() + (duration * 1000L);

            while (System.currentTimeMillis() < endTime && !shouldStop) {
                leakedMemory.add(new byte[1024 * 1024]); // Leak 1MB at a time
                Thread.sleep(100);
            }

            response.getWriter().write("Generated memory leak for " + duration + " seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.getWriter().write("Memory leak generation interrupted");
        }
    }

    private void generateHighCPU(HttpServletResponse response, int duration) throws IOException {
        executor.submit(() -> {
            long endTime = System.currentTimeMillis() + (duration * 1000L);
            while (System.currentTimeMillis() < endTime && !shouldStop) {
                // CPU-intensive calculation
                for (int i = 0; i < 1000000; i++) {
                    Math.sqrt(i);
                }
            }
            return null;
        });
        response.getWriter().write("Generated high CPU usage for " + duration + " seconds");
    }

    private void generateThreadDeadlock(HttpServletResponse response) throws IOException {
        Object lock1 = new Object();
        Object lock2 = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                try {
                    Thread.sleep(100);
                    synchronized (lock2) {
                        // Never reaches here
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lock2) {
                try {
                    Thread.sleep(100);
                    synchronized (lock1) {
                        // Never reaches here
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        t1.start();
        t2.start();

        response.getWriter().write("Generated thread deadlock");
    }

    @Override
    public void destroy() {
        shouldStop = true;
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 