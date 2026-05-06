package org.example.expensecommand.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for standardized logging across the application.
 * Provides methods for structured logging with correlation IDs.
 */
@Component
public class LoggingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtil.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Value("${spring.application.name:expense-command}")
    private String applicationName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Logs an info message with correlation IDs
     */
    public void logInfo(String message) {
        String traceId = org.slf4j.MDC.get(RequestCorrelationFilter.TRACE_ID);
        String requestId = org.slf4j.MDC.get(RequestCorrelationFilter.REQUEST_ID);
        LOGGER.info("[{}] [traceId: {}] [requestId: {}] {}",
            applicationName, traceId, requestId, message);
    }

    /**
     * Logs a debug message with correlation IDs
     */
    public void logDebug(String message) {
        String traceId = org.slf4j.MDC.get(RequestCorrelationFilter.TRACE_ID);
        String requestId = org.slf4j.MDC.get(RequestCorrelationFilter.REQUEST_ID);
        LOGGER.debug("[{}] [traceId: {}] [requestId: {}] {}",
            applicationName, traceId, requestId, message);
    }

    /**
     * Logs a warning message with correlation IDs
     */
    public void logWarn(String message) {
        String traceId = org.slf4j.MDC.get(RequestCorrelationFilter.TRACE_ID);
        String requestId = org.slf4j.MDC.get(RequestCorrelationFilter.REQUEST_ID);
        LOGGER.warn("[{}] [traceId: {}] [requestId: {}] {}",
            applicationName, traceId, requestId, message);
    }

    /**
     * Logs an error message with correlation IDs
     */
    public void logError(String message, Throwable throwable) {
        String traceId = org.slf4j.MDC.get(RequestCorrelationFilter.TRACE_ID);
        String requestId = org.slf4j.MDC.get(RequestCorrelationFilter.REQUEST_ID);
        LOGGER.error("[{}] [traceId: {}] [requestId: {}] {}",
            applicationName, traceId, requestId, message, throwable);
    }

    /**
     * Logs an error message with correlation IDs without exception
     */
    public void logError(String message) {
        String traceId = org.slf4j.MDC.get(RequestCorrelationFilter.TRACE_ID);
        String requestId = org.slf4j.MDC.get(RequestCorrelationFilter.REQUEST_ID);
        LOGGER.error("[{}] [traceId: {}] [requestId: {}] {}",
            applicationName, traceId, requestId, message);
    }

    /**
     * Logs request start
     */
    public void logRequestStart(String method, String uri, String userAgent) {
        logInfo(String.format("Request started: %s %s | User-Agent: %s", method, uri, userAgent));
    }

    /**
     * Logs request completion
     */
    public void logRequestComplete(String method, String uri, int status, long durationMs) {
        logInfo(String.format("Request completed: %s %s | Status: %d | Duration: %dms",
            method, uri, status, durationMs));
    }

    /**
     * Logs a business event
     */
    public void logBusinessEvent(String eventType, String details) {
        logInfo(String.format("BUSINESS_EVENT [%s]: %s", eventType, details));
    }

    /**
     * Logs security event
     */
    public void logSecurityEvent(String eventType, String details, String ipAddress) {
        logWarn(String.format("SECURITY_EVENT [%s]: %s | IP: %s", eventType, details, ipAddress));
    }

    /**
     * Gets the current timestamp in ISO format
     */
    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Gets the current active profile
     */
    public String getActiveProfile() {
        return activeProfile;
    }

    /**
     * Gets the application name
     */
    public String getApplicationName() {
        return applicationName;
    }
}

