package com.example.servlets;

import com.example.CpuLoadGenerator;
import com.example.DatabaseManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/cpu-load")
public class CpuLoadServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        String action = request.getParameter("action");
        if ("stop".equals(action)) {
            CpuLoadGenerator.stopLoad();
            response.getWriter().write("{\"status\":\"success\",\"message\":\"CPU load stopped\"}");
            return;
        }
        
        String typeParam = request.getParameter("type");
        String durationParam = request.getParameter("duration");
        String threadsParam = request.getParameter("threads");
        
        if (typeParam == null || durationParam == null || threadsParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing parameters\"}");
            return;
        }
        
        try {
            int duration = Integer.parseInt(durationParam);
            int threads = Integer.parseInt(threadsParam);
            
            // Limit duration to prevent excessive load
            if (duration > 300) {
                duration = 300;
            }
            
            // Limit threads to prevent system crash
            int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
            if (threads > maxThreads) {
                threads = maxThreads;
            }
            
            // Generate CPU load
            CpuLoadGenerator.generateLoad(duration, threads, typeParam);
            
            response.getWriter().write("{\"status\":\"success\",\"message\":\"CPU load of type '" + 
                    typeParam + "' with " + threads + " threads started for " + duration + " seconds\"}");
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid numeric parameters\"}");
        }
    }
} 