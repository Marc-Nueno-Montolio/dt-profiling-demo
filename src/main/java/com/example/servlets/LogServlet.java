package com.example.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet(urlPatterns = {"/logs/*"})
public class LogServlet extends HttpServlet {
    private static final String TOMCAT_LOGS_DIR = "/usr/local/tomcat/logs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CURRENT_DATE = LocalDate.now().format(DATE_FORMAT);

    private static final Map<String, String> LOG_FILES = Map.of(
        "catalina.out", "Catalina Output",
        String.format("localhost.%s.log", CURRENT_DATE), "Localhost Access Log",
        String.format("manager.%s.log", CURRENT_DATE), "Manager Log",
        String.format("host-manager.%s.log", CURRENT_DATE), "Host Manager Log"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String logFile = request.getParameter("file");
        String linesParam = request.getParameter("lines");
        String action = request.getParameter("action");

        if (logFile == null) {
            logFile = "catalina.out";
        }

        int lines = 100;
        if (linesParam != null) {
            try {
                lines = Integer.parseInt(linesParam);
                lines = Math.min(Math.max(lines, 10), 1000);
            } catch (NumberFormatException e) {
                // Use default
            }
        }

        if ("list".equals(action)) {
            listLogFiles(response);
            return;
        }

        if ("download".equals(action)) {
            downloadLog(response, logFile);
            return;
        }

        viewLogContent(response, logFile, lines);
    }

    private void listLogFiles(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        File logsDir = new File(TOMCAT_LOGS_DIR);
        File[] files = logsDir.listFiles((dir, name) -> 
            name.endsWith(".log") || 
            name.endsWith(".out") || 
            name.endsWith(".txt"));
        
        List<Map<String, String>> fileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("name", file.getName());
                fileInfo.put("size", String.valueOf(file.length()));
                fileInfo.put("lastModified", String.valueOf(file.lastModified()));
                fileList.add(fileInfo);
            }
        }
        
        response.getWriter().write(new org.json.JSONArray(fileList).toString());
    }

    private void viewLogContent(HttpServletResponse response, String logFile, int lines) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        Path logPath = Paths.get(TOMCAT_LOGS_DIR, logFile);
        File file = logPath.toFile();

        if (!file.exists()) {
            // Try with date suffix
            String dateSuffixFile = logFile.replace(".log", "." + CURRENT_DATE + ".log");
            logPath = Paths.get(TOMCAT_LOGS_DIR, dateSuffixFile);
            file = logPath.toFile();
            
            if (!file.exists()) {
                response.getWriter().write("Log file not found: " + logFile);
                return;
            }
        }

        try {
            List<String> allLines = Files.readAllLines(logPath);
            int startLine = Math.max(0, allLines.size() - lines);
            List<String> lastLines = allLines.subList(startLine, allLines.size());
            
            response.setHeader("X-Log-Size", String.valueOf(file.length()));
            response.setHeader("X-Log-Lines", String.valueOf(allLines.size()));
            response.setHeader("X-Log-LastModified", String.valueOf(file.lastModified()));
            
            response.getWriter().write(String.join("\n", lastLines));
        } catch (IOException e) {
            response.getWriter().write("Error reading log file: " + e.getMessage());
        }
    }

    private void downloadLog(HttpServletResponse response, String logFile) throws IOException {
        Path logPath = Paths.get(TOMCAT_LOGS_DIR, logFile);
        File file = logPath.toFile();

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Log file not found");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + logFile + ".zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            ZipEntry entry = new ZipEntry(logFile);
            zos.putNextEntry(entry);
            Files.copy(logPath, zos);
            zos.closeEntry();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String logFile = request.getParameter("file");

        if (logFile == null) {
            logFile = "catalina.out";
        }

        if ("clear".equals(action)) {
            clearLog(response, logFile);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    private void clearLog(HttpServletResponse response, String logFile) throws IOException {
        Path logPath = Paths.get(TOMCAT_LOGS_DIR, logFile);
        File file = logPath.toFile();

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Log file not found");
            return;
        }

        try {
            // Clear file content but keep the file
            Files.write(logPath, Collections.singletonList("Log cleared at: " + new Date()));
            response.getWriter().write("Log file cleared successfully");
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error clearing log file: " + e.getMessage());
        }
    }
} 