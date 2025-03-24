package com.example.servlets;

import com.example.CrashSimulator;
import com.example.DatabaseManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/crash")
public class CrashServlet extends HttpServlet {
    
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
        if ("cancel".equals(action)) {
            CrashSimulator.cancelCrash();
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Scheduled crash cancelled\"}");
            return;
        }
        
        String typeParam = request.getParameter("type");
        String delayParam = request.getParameter("delay");
        
        if (typeParam == null || delayParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing parameters\"}");
            return;
        }
        
        try {
            int delay = Integer.parseInt(delayParam);
            
            // Limit delay to be reasonable
            if (delay < 5) delay = 5;
            if (delay > 300) delay = 300;
            
            CrashSimulator.simulateCrash(typeParam, delay);
            
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Crash of type '" + 
                    typeParam + "' scheduled in " + delay + " seconds\"}");
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid delay parameter\"}");
        }
    }
} 