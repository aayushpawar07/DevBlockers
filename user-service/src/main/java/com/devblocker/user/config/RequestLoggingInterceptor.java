package com.devblocker.user.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Request logging interceptor to log all incoming requests
 * Adds correlation ID for tracing requests across services
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        request.setAttribute("correlationId", correlationId);
        response.setHeader("X-Correlation-Id", correlationId);
        
        log.info("Incoming request: {} {} [Correlation-ID: {}]", 
                request.getMethod(), request.getRequestURI(), correlationId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        String correlationId = (String) request.getAttribute("correlationId");
        if (ex != null) {
            log.error("Request failed: {} {} [Correlation-ID: {}]", 
                    request.getMethod(), request.getRequestURI(), correlationId, ex);
        } else {
            log.debug("Request completed: {} {} [Status: {}] [Correlation-ID: {}]", 
                    request.getMethod(), request.getRequestURI(), response.getStatus(), correlationId);
        }
    }
}

