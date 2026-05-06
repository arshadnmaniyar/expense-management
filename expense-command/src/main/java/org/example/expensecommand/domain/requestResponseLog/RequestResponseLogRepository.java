package org.example.expensecommand.domain.requestResponseLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for RequestResponseLog entities.
 * Provides database access for audit logs.
 */
@Repository
public interface RequestResponseLogRepository extends JpaRepository<RequestResponseLog, UUID> {

    /**
     * Find logs by trace ID.
     */
    List<RequestResponseLog> findByTraceId(String traceId);

    /**
     * Find logs by request ID.
     */
    List<RequestResponseLog> findByRequestId(String requestId);

    /**
     * Find logs by user ID.
     */
    List<RequestResponseLog> findByUserId(UUID userId);

    /**
     * Find logs created after a certain timestamp.
     */
    List<RequestResponseLog> findByCreatedTsAfter(LocalDateTime timestamp);

    /**
     * Find logs by HTTP status code.
     */
    List<RequestResponseLog> findByResponseStatus(Integer responseStatus);

    /**
     * Find logs by HTTP method.
     */
    List<RequestResponseLog> findByHttpMethod(String httpMethod);
}

