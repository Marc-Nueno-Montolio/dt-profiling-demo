package com.example.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import javax.servlet.ServletException;

@WebServlet("/exception")
public class ExceptionServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String type = request.getParameter("type");
            Exception exception = generateException(type);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Exception generated successfully");
            jsonResponse.put("exceptionType", exception.getClass().getName());
            jsonResponse.put("exceptionMessage", exception.getMessage());
            jsonResponse.put("stackTrace", sw.toString());
            
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to generate exception");
            errorResponse.put("error", e.getMessage());
            
            response.getWriter().write(errorResponse.toString());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    private Exception generateException(String type) {
        if (type == null) {
            return new IllegalArgumentException("Exception type cannot be null");
        }
        
        switch (type) {
            case "nullPointer":
                String str = null;
                try {
                    str.length();
                } catch (NullPointerException e) {
                    return e;
                }
                break;
                
            case "arithmetic":
                try {
                    int result = 1 / 0;
                } catch (ArithmeticException e) {
                    return e;
                }
                break;
                
            case "outOfMemory":
                try {
                    List<byte[]> list = new ArrayList<>();
                    while (true) {
                        list.add(new byte[1024 * 1024]); // 1MB each
                    }
                } catch (OutOfMemoryError e) {
                    return new Exception("Out of Memory Error: " + e.getMessage(), e);
                }
                
            case "stackOverflow":
                try {
                    recursiveMethod(1);
                } catch (StackOverflowError e) {
                    return new Exception("Stack Overflow Error: " + e.getMessage(), e);
                }
                break;
                
            default:
                return new IllegalArgumentException("Unknown exception type: " + type);
        }
        
        return new IllegalStateException("Failed to generate exception");
    }
    
    private void recursiveMethod(int depth) {
        recursiveMethod(depth + 1);
    }
} 