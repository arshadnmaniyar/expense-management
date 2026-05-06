package org.example.expensecommand.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter for setting correlation IDs (traceId and requestId) in MDC.
 * Do NOT use @Component - registration is handled via WebConfig's FilterRegistrationBean.
 */
public class RequestCorrelationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    public static final String TRACE_ID = "traceId";
    public static final String REQUEST_ID = "requestId";
    public static final String HEADER_TRACE_ID = "X-B3-TraceId";   // common tracing header
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            if (request instanceof HttpServletRequest http) {
                // Prefer incoming headers if present
                String traceId = firstNonNull(http.getHeader(HEADER_TRACE_ID), http.getHeader("traceparent"), http.getHeader("trace-id"));
                if (traceId == null || traceId.isBlank()) {
                    traceId = UUID.randomUUID().toString().replace("-", "");
                }

                String requestId = firstNonNull(http.getHeader(HEADER_REQUEST_ID), UUID.randomUUID().toString());

                MDC.put(TRACE_ID, traceId);
                MDC.put(REQUEST_ID, requestId);

                LOGGER.info("MDC Correlation IDs set: traceId={}, requestId={}", traceId, requestId);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
            MDC.remove(REQUEST_ID);
        }
    }

    private static String firstNonNull(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}


