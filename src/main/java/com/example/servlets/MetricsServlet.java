package com.example.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.*;
import org.json.JSONObject;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/metrics/*")
public class MetricsServlet extends HttpServlet {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean shouldStop = false;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            getAllMetrics(response);
            return;
        }

        switch (pathInfo.substring(1)) {
            case "gc":
                getGCMetrics(response);
                break;
            case "memory":
                getMemoryMetrics(response);
                break;
            case "threads":
                getThreadMetrics(response);
                break;
            case "classes":
                getClassLoadingMetrics(response);
                break;
            case "os":
                getOSMetrics(response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getGCMetrics(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject metrics = new JSONObject();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            metrics.put(gcBean.getName(), new JSONObject()
                .put("collectionCount", gcBean.getCollectionCount())
                .put("collectionTime", gcBean.getCollectionTime())
                .put("pools", gcBean.getMemoryPoolNames()));
        }
        
        response.getWriter().write(metrics.toString());
    }

    private void getMemoryMetrics(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject metrics = new JSONObject();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        
        metrics.put("heap", getMemoryUsageJson(memory.getHeapMemoryUsage()));
        metrics.put("nonHeap", getMemoryUsageJson(memory.getNonHeapMemoryUsage()));
        
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        JSONObject poolMetrics = new JSONObject();
        for (MemoryPoolMXBean pool : pools) {
            poolMetrics.put(pool.getName(), getMemoryUsageJson(pool.getUsage()));
        }
        metrics.put("pools", poolMetrics);
        
        response.getWriter().write(metrics.toString());
    }

    private void getThreadMetrics(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject metrics = new JSONObject();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        
        metrics.put("threadCount", threads.getThreadCount())
            .put("peakThreadCount", threads.getPeakThreadCount())
            .put("totalStartedThreadCount", threads.getTotalStartedThreadCount())
            .put("daemonThreadCount", threads.getDaemonThreadCount());
        
        long[] deadlockedThreads = threads.findDeadlockedThreads();
        metrics.put("deadlockedThreadCount", deadlockedThreads != null ? deadlockedThreads.length : 0);
        
        response.getWriter().write(metrics.toString());
    }

    private void getClassLoadingMetrics(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject metrics = new JSONObject();
        ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
        
        metrics.put("loadedClassCount", classLoading.getLoadedClassCount())
            .put("totalLoadedClassCount", classLoading.getTotalLoadedClassCount())
            .put("unloadedClassCount", classLoading.getUnloadedClassCount());
        
        response.getWriter().write(metrics.toString());
    }

    private void getOSMetrics(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject metrics = new JSONObject();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        
        metrics.put("arch", os.getArch())
            .put("availableProcessors", os.getAvailableProcessors())
            .put("name", os.getName())
            .put("version", os.getVersion())
            .put("systemLoadAverage", os.getSystemLoadAverage());
        
        response.getWriter().write(metrics.toString());
    }

    private JSONObject getMemoryUsageJson(MemoryUsage usage) {
        return new JSONObject()
            .put("init", usage.getInit())
            .put("used", usage.getUsed())
            .put("committed", usage.getCommitted())
            .put("max", usage.getMax());
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
            case "frequent_gc":
                generateFrequentGC(response, duration);
                break;
            case "long_gc":
                generateLongGCPause(response, duration);
                break;
            case "concurrent_gc":
                generateConcurrentModeFailure(response, duration);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type");
        }
    }

    private void getAllMetrics(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject metrics = new JSONObject();
        
        // Memory metrics
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        MemoryUsage nonHeap = memory.getNonHeapMemoryUsage();
        
        metrics.put("heap", new JSONObject()
            .put("used", heap.getUsed())
            .put("committed", heap.getCommitted())
            .put("max", heap.getMax()));
        
        metrics.put("nonHeap", new JSONObject()
            .put("used", nonHeap.getUsed())
            .put("committed", nonHeap.getCommitted()));

        // Thread metrics
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        metrics.put("threads", new JSONObject()
            .put("count", threads.getThreadCount())
            .put("peakCount", threads.getPeakThreadCount())
            .put("daemonCount", threads.getDaemonThreadCount())
            .put("deadlockedCount", threads.findDeadlockedThreads() != null ? 
                threads.findDeadlockedThreads().length : 0));

        // Class loading metrics
        ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
        metrics.put("classes", new JSONObject()
            .put("loaded", classLoading.getLoadedClassCount())
            .put("unloaded", classLoading.getUnloadedClassCount())
            .put("total", classLoading.getTotalLoadedClassCount()));

        // Operating system metrics
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        metrics.put("os", new JSONObject()
            .put("processors", os.getAvailableProcessors())
            .put("systemLoad", os.getSystemLoadAverage()));

        response.getWriter().write(metrics.toString());
    }

    private void generateFrequentGC(HttpServletResponse response, int duration) throws IOException {
        executor.submit(() -> {
            long endTime = System.currentTimeMillis() + (duration * 1000L);
            ArrayList<byte[]> list = new ArrayList<>();
            
            while (System.currentTimeMillis() < endTime && !shouldStop) {
                for (int i = 0; i < 100; i++) {
                    list.add(new byte[1024 * 1024]); // 1MB
                }
                list.clear();
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return null;
        });
        
        response.getWriter().write("Generated frequent GC for " + duration + " seconds");
    }

    private void generateLongGCPause(HttpServletResponse response, int duration) throws IOException {
        executor.submit(() -> {
            long endTime = System.currentTimeMillis() + (duration * 1000L);
            ArrayList<byte[]> list = new ArrayList<>();
            
            while (System.currentTimeMillis() < endTime && !shouldStop) {
                for (int i = 0; i < 1000; i++) {
                    list.add(new byte[1024 * 1024]); // 1MB
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            list.clear();
            System.gc();
            return null;
        });
        
        response.getWriter().write("Generated long GC pause for " + duration + " seconds");
    }

    private void generateConcurrentModeFailure(HttpServletResponse response, int duration) throws IOException {
        executor.submit(() -> {
            long endTime = System.currentTimeMillis() + (duration * 1000L);
            ArrayList<ArrayList<byte[]>> lists = new ArrayList<>();
            
            while (System.currentTimeMillis() < endTime && !shouldStop) {
                ArrayList<byte[]> list = new ArrayList<>();
                lists.add(list);
                
                for (int i = 0; i < 100; i++) {
                    list.add(new byte[1024 * 1024]); // 1MB
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return null;
        });
        
        response.getWriter().write("Generated concurrent mode failure for " + duration + " seconds");
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