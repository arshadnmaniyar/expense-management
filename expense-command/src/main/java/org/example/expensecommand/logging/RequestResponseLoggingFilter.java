package org.example.expensecommand.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expensecommand.domain.requestResponseLog.RequestResponseLog;
import org.example.expensecommand.domain.requestResponseLog.RequestResponseLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring filter for logging all HTTP requests and responses to the database.
 * This filter captures request/response details and stores them in the request_response_log table.
 * Respects the profile and logs according to the configured logging strategy.
 * Note: Do NOT use @Component - registration is done via WebConfig to control filter order.
 */
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final int MAX_BODY_LENGTH = 10000; // 10KB max for body storage
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/actuator",
            "/health",
            "/metrics",
            "/v3/api-docs"
    );

    private final RequestResponseLogRepository requestResponseLogRepository;

    public RequestResponseLoggingFilter(RequestResponseLogRepository requestResponseLogRepository) {
        this.requestResponseLogRepository = requestResponseLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        // Skip excluded paths
        if (isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        LOGGER.info("=== REQUEST START === {} {}", request.getMethod(), request.getRequestURI());

        // Wrap request and response to allow multiple reads
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String traceId = MDC.get(RequestCorrelationFilter.TRACE_ID);
        String requestId = MDC.get(RequestCorrelationFilter.REQUEST_ID);

        LOGGER.info("Correlation IDs - traceId: {}, requestId: {}", traceId, requestId);
        LOGGER.debug("Request Headers: {}", getHeadersAsString(wrappedRequest));

        try {
            // Pass wrapped request and response through filter chain
            // ContentCachingWrapper will automatically capture content as it flows through
            LOGGER.debug("Passing request through filter chain...");
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            LOGGER.info("Filter chain completed, response status: {}", wrappedResponse.getStatus());
        } catch (Exception e) {
            LOGGER.error("Error in filter chain: {}", e.getMessage(), e);
            throw e;
        } finally {
            try {
                // Ensure response is committed and content is available
                if (!wrappedResponse.isCommitted()) {
                    LOGGER.debug("Flushing response buffer...");
                    wrappedResponse.flushBuffer();
                }
                long responseTime = System.currentTimeMillis() - startTime;
                LOGGER.debug("Response time: {}ms", responseTime);
                logRequestResponse(wrappedRequest, wrappedResponse, responseTime, traceId, requestId);
            } catch (Exception e) {
                LOGGER.error("Error logging request/response: {}", e.getMessage(), e);
            }

            // Copy response content to the actual response
            LOGGER.debug("Copying response body to actual response...");
            wrappedResponse.copyBodyToResponse();
            LOGGER.info("=== REQUEST END === {} {} - Status: {}", request.getMethod(), request.getRequestURI(), wrappedResponse.getStatus());
        }
    }


    /**
     * Logs request and response details to the database.
     */
    private void logRequestResponse(ContentCachingRequestWrapper request,
                                   ContentCachingResponseWrapper response,
                                   long responseTimeMs,
                                   String traceId,
                                   String requestId) {
        try {
            LOGGER.debug("Building RequestResponseLog entity...");
            RequestResponseLog log = new RequestResponseLog();
            log.setLogId(UUID.randomUUID());
            log.setTraceId(traceId);
            log.setRequestId(requestId);
            log.setTimestamp(LocalDateTime.now());
            log.setHttpMethod(request.getMethod());
            log.setRequestUri(getRequestUri(request));
            log.setRequestHeaders(getHeadersAsString(request));

            String requestBody = getRequestBody(request);
            log.setRequestBody(requestBody);
            LOGGER.debug("Request body captured: {} bytes", requestBody != null ? requestBody.length() : 0);

            log.setResponseStatus(response.getStatus());
            log.setResponseHeaders(getResponseHeadersAsString(response));

            String responseBody = getResponseBody(response);
            log.setResponseBody(responseBody);
            LOGGER.debug("Response body captured: {} bytes", responseBody != null ? responseBody.length() : 0);

            log.setResponseTimeMs(responseTimeMs);
            log.setUserId(extractUserIdFromRequest(request));
            log.setClientIp(getClientIp(request));
            log.setErrorMessage(response.getStatus() >= 400 ? getErrorMessage(response) : null);

            LOGGER.info("Log entity built: method={}, uri={}, status={}, traceId={}, requestId={}",
                    log.getHttpMethod(), log.getRequestUri(), log.getResponseStatus(), traceId, requestId);

            // Save asynchronously to avoid blocking the request
            saveLogAsync(log);

        } catch (Exception e) {
            LOGGER.error("Failed to log request/response: {}", e.getMessage(), e);
        }
    }

    /**
     * Saves log asynchronously using a separate thread.
     * Copies MDC context to the async thread to preserve correlation IDs.
     */
    private void saveLogAsync(RequestResponseLog log) {
        // Capture MDC context from current thread
        java.util.Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        new Thread(() -> {
            try {
                // Restore MDC context in async thread
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }

                LOGGER.debug("Saving log entry [logId: {}] to database...", log.getLogId());
                requestResponseLogRepository.save(log);
                LOGGER.info("Successfully saved log entry [logId: {}, traceId: {}, requestId: {}, status: {}]",
                        log.getLogId(), log.getTraceId(), log.getRequestId(), log.getResponseStatus());

            } catch (Exception e) {
                LOGGER.error("Failed to save request/response log [traceId: {}, requestId: {}]: {}",
                        log.getTraceId(), log.getRequestId(), e.getMessage(), e);
            } finally {
                // Always clear MDC in async thread
                MDC.clear();
            }
        }, "RequestResponseLogAsync-Thread").start();
    }

    /**
     * Extracts user ID from JWT token or authentication context.
     */
    private UUID extractUserIdFromRequest(HttpServletRequest request) {
        try {
            // Try to extract from header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // In a real implementation, decode JWT to get user ID
                // For now, return null
                return null;
            }

            // Try to extract from request attributes (set by security context)
            //Object principal = request.getUserPrincipal();
            java.security.Principal principal = request.getUserPrincipal();
            if (principal != null) {
                String name = principal.getName();
                try {
                    return UUID.fromString(name);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Could not parse user ID from principal: {}", name);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error extracting user ID from request", e);
        }
        return null;
    }

    /**
     * Gets the full request URI with query string.
     */
    private String getRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null) {
            uri += "?" + queryString;
        }
        return uri;
    }

    /**
     * Converts request headers to a string for storage.
     */
    private String getHeadersAsString(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                // Mask sensitive headers
                if (isSensitiveHeader(name)) {
                    value = "***MASKED***";
                }
                sb.append(name).append(": ").append(value).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.debug("Error converting request headers", e);
            return "";
        }
    }

    /**
     * Gets request body content.
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            LOGGER.info("Request content length: {} bytes", content.length);
            if (content.length > 0) {
                String body = new String(content, request.getCharacterEncoding() != null ?
                        request.getCharacterEncoding() : "UTF-8");
                LOGGER.info("Request body captured (first 200 chars): {}", body.substring(0, Math.min(200, body.length())));
                return body.length() > MAX_BODY_LENGTH ?
                        body.substring(0, MAX_BODY_LENGTH) + "...[TRUNCATED]" : body;
            } else {
                LOGGER.info("Request content is empty (GET, DELETE, etc.)");
            }
        } catch (Exception e) {
            LOGGER.error("Error reading request body: {}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * Converts response headers to a string for storage.
     */
    private String getResponseHeadersAsString(HttpServletResponse response) {
        try {
            return response.getHeaderNames().stream()
                    .map(name -> {
                        String value = response.getHeader(name);
                        if (isSensitiveHeader(name)) {
                            value = "***MASKED***";
                        }
                        return name + ": " + value;
                    })
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            LOGGER.debug("Error converting response headers", e);
            return "";
        }
    }

    /**
     * Gets response body content.
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            // Ensure content is available
            response.flushBuffer();
            byte[] content = response.getContentAsByteArray();
            LOGGER.info("Response content length: {} bytes, content type: {}", content.length, response.getContentType());
            if (content.length > 0) {
                String body = new String(content, response.getCharacterEncoding() != null ?
                        response.getCharacterEncoding() : "UTF-8");
                LOGGER.info("Response body captured (first 200 chars): {}", body.substring(0, Math.min(200, body.length())));
                return body.length() > MAX_BODY_LENGTH ?
                        body.substring(0, MAX_BODY_LENGTH) + "...[TRUNCATED]" : body;
            } else {
                LOGGER.info("Response content is empty (status 204, 304, etc.), committed: {}", response.isCommitted());
            }
        } catch (Exception e) {
            LOGGER.error("Error reading response body: {}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * Extracts error message from response body.
     */
    private String getErrorMessage(ContentCachingResponseWrapper response) {
        try {
            String body = getResponseBody(response);
            // Extract error message from JSON response (if applicable)
            if (body.contains("error") || body.contains("message")) {
                return body.length() > 500 ? body.substring(0, 500) : body;
            }
        } catch (Exception e) {
            LOGGER.debug("Error extracting error message", e);
        }
        return null;
    }

    /**
     * Gets the client IP address from the request.
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    /**
     * Checks if a header is sensitive and should be masked.
     */
    private boolean isSensitiveHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.contains("authorization") || lower.contains("password") ||
               lower.contains("token") || lower.contains("secret") ||
               lower.contains("cookie") || lower.contains("x-api-key");
    }

    /**
     * Checks if a path should be excluded from logging.
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isExcludedPath(request.getRequestURI());
    }
}

