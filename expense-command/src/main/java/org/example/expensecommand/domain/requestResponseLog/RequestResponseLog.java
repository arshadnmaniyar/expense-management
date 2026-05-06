package org.example.expensecommand.domain.requestResponseLog;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to store HTTP request/response logs for audit and debugging purposes.
 * All requests and responses are logged to the database for persistent tracking.
 */
@Entity
@Table(name = "request_response_log", schema = "expense_management")
public class RequestResponseLog {

    @Id
    @Column(name = "log_id", columnDefinition = "uuid")
    private UUID logId;

    @Column(name = "trace_id", length = 255)
    private String traceId;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "http_method", length = 50)
    private String httpMethod;

    @Column(name = "request_uri", length = 2048)
    private String requestUri;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_ts", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTs;

    // Constructors
    public RequestResponseLog() {
    }

    public RequestResponseLog(UUID logId, String traceId, String requestId, LocalDateTime timestamp,
                             String httpMethod, String requestUri, String requestHeaders,
                             String requestBody, Integer responseStatus, String responseHeaders,
                             String responseBody, Long responseTimeMs, UUID userId,
                             String clientIp, String errorMessage) {
        this.logId = logId;
        this.traceId = traceId;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.responseStatus = responseStatus;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.responseTimeMs = responseTimeMs;
        this.userId = userId;
        this.clientIp = clientIp;
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(LocalDateTime createdTs) {
        this.createdTs = createdTs;
    }

    @Override
    public String toString() {
        return "RequestResponseLog{" +
                "logId=" + logId +
                ", traceId='" + traceId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", responseStatus=" + responseStatus +
                ", responseTimeMs=" + responseTimeMs +
                ", userId=" + userId +
                ", clientIp='" + clientIp + '\'' +
                '}';
    }
}
