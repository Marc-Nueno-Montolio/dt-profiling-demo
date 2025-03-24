package com.example.servlets;

import com.example.DatabaseManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/control-panel")
public class ControlPanelServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Log the access to control panel
        DatabaseManager.getInstance().logAction("CONTROL_PANEL_ACCESS", 
                "User accessed control panel from " + request.getRemoteAddr());
        
        // Forward to the JSP
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing action parameter");
            return;
        }
        
        // Log the action request
        DatabaseManager.getInstance().logAction("CONTROL_ACTION", "Requested action: " + action);
        
        // Dispatch the action to the appropriate servlet
        String redirectUrl = "";
        
        switch (action) {
            case "memory-leak":
                redirectUrl = "/memory-leak";
                break;
            case "cpu-load":
                redirectUrl = "/cpu-load";
                break;
            case "exception":
                redirectUrl = "/exception";
                break;
            case "crash":
                redirectUrl = "/crash";
                break;
            case "db-inefficient":
                redirectUrl = "/db-inefficient";
                break;
            case "clear-memory":
                // Special case for memory cleanup
                redirectUrl = "/memory-leak?action=cleanup";
                break;
            case "stop-cpu":
                // Special case for stopping CPU load
                redirectUrl = "/cpu-load?action=stop";
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Unknown action: " + action);
                return;
        }
        
        // Forward the request with all parameters to the target servlet
        request.getRequestDispatcher(redirectUrl).forward(request, response);
    }
} 