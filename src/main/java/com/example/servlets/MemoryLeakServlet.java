package com.example.servlets;

import com.example.MemoryLeakGenerator;
import com.example.DatabaseManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/memory-leak")
public class MemoryLeakServlet extends HttpServlet {
    
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
        if ("cleanup".equals(action)) {
            MemoryLeakGenerator.cleanup();
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Memory leak cleaned up\"}");
            return;
        }
        
        String typeParam = request.getParameter("type");
        String sizeParam = request.getParameter("size");
        
        if (typeParam == null || sizeParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing parameters\"}");
            return;
        }
        
        try {
            int size = Integer.parseInt(sizeParam);
            
            switch (typeParam) {
                case "simple":
                    MemoryLeakGenerator.leakMemory(size);
                    break;
                case "map":
                    int count = size * 10; // Create 10 objects per MB requested
                    MemoryLeakGenerator.leakMemoryWithMap(count, size * 100);
                    break;
                case "classloader":
                    MemoryLeakGenerator.classLoaderLeak();
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"status\":\"error\",\"message\":\"Unknown leak type\"}");
                    return;
            }
            
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Memory leak of type '" + 
                    typeParam + "' with size " + size + " MB initiated\"}");
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid size parameter\"}");
        }
    }
} 