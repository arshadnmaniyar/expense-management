package org.example.expensecommand.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Authentication Entry Point that logs authentication failures.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ErrorLogger");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        String traceId = MDC.get("traceId");
        String requestId = MDC.get("requestId");

        ERROR_LOGGER.error("Authentication failed [traceId: {}, requestId: {}, path: {}, method: {}]: {}",
                traceId, requestId, request.getRequestURI(), request.getMethod(), authException.getMessage(), authException);

        LOGGER.error("Authentication failed [traceId: {}, requestId: {}]", traceId, requestId, authException);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("traceId", traceId);
        body.put("requestId", requestId);
        body.put("path", request.getRequestURI());

        response.getOutputStream().println(objectMapper.writeValueAsString(body));
    }
}

