-- Migration: Create request_response_log table
-- Date: 2026-04-22
-- Description: Creates table for storing HTTP request/response logs for audit and debugging

CREATE TABLE IF NOT EXISTS request_response_log (
    log_id UUID PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    request_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    http_method VARCHAR(50) NOT NULL,
    request_uri VARCHAR(2048) NOT NULL,
    request_headers TEXT,
    request_body TEXT,
    response_status INTEGER,
    response_headers TEXT,
    response_body TEXT,
    response_time_ms BIGINT,
    user_id UUID,
    client_ip VARCHAR(50),
    error_message TEXT,
    created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_request_response_log_trace_id
    ON request_response_log(trace_id);

CREATE INDEX IF NOT EXISTS idx_request_response_log_request_id
    ON request_response_log(request_id);

CREATE INDEX IF NOT EXISTS idx_request_response_log_user_id
    ON request_response_log(user_id);

CREATE INDEX IF NOT EXISTS idx_request_response_log_http_method
    ON request_response_log(http_method);

CREATE INDEX IF NOT EXISTS idx_request_response_log_response_status
    ON request_response_log(response_status);

CREATE INDEX IF NOT EXISTS idx_request_response_log_created_ts
    ON request_response_log(created_ts);

-- Add comment to table
COMMENT ON TABLE request_response_log IS 'Stores HTTP request/response logs for audit, debugging, and compliance tracking';
COMMENT ON COLUMN request_response_log.trace_id IS 'Distributed trace ID for correlating requests across services';
COMMENT ON COLUMN request_response_log.request_id IS 'Unique identifier for individual requests';
COMMENT ON COLUMN request_response_log.response_time_ms IS 'Time taken to process the request in milliseconds';
