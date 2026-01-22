package com.simple.crud.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;


@Component
@Order(1) 
public class LoggingMDCFilter implements Filter {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String SESSION_ID_KEY = "sessionId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        try {
            // Add request ID (UUID for unique tracking)
            String requestId = UUID.randomUUID().toString();
            MDC.put(REQUEST_ID_KEY, requestId);
            
            // Add user ID if authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                if (username != null) {
                    MDC.put(USER_ID_KEY, username);
                }
            }
            
            // Add session ID if available
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String sessionId = httpRequest.getSession(false) != null 
                    ? httpRequest.getSession(false).getId() 
                    : null;
                if (sessionId != null) {
                    MDC.put(SESSION_ID_KEY, sessionId);
                }
            }
            
            // Continue filter chain
            chain.doFilter(request, response);
            
        } finally {
            // Thread pools reuse threads, so MDC must be cleaned up
            MDC.clear();
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }
    
    @Override
    public void destroy() {
        // Ensure MDC is cleared on filter destruction
        MDC.clear();
    }
}
