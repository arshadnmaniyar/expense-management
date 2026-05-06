package org.example.expensecommand.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ErrorLogger");

    private final PostgresExceptionTranslator translator;

    public GlobalExceptionHandler(PostgresExceptionTranslator translator) {
        this.translator = translator;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // Log to ERROR_LOGGER to ensure it goes to error.log
        ERROR_LOGGER.error("Validation error [traceId: {}, requestId: {}]: {}",
                MDC.get("traceId"), MDC.get("requestId"), msg);
        ERROR_LOGGER.error("Validation error details", ex);

        return build(msg, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {

        RuntimeException translated = translator.translate(ex);
        if (translated instanceof MissingRequiredFieldException e) {
            ERROR_LOGGER.error("Missing required field error [traceId: {}, requestId: {}]: {}",
                    MDC.get("traceId"), MDC.get("requestId"), e.getMessage(), e);
            return build(e.getMessage(), "MISSING_REQUIRED_FIELD", HttpStatus.BAD_REQUEST);
        }

        if (translated instanceof DuplicateRequestException e) {
            ERROR_LOGGER.error("Duplicate request error [traceId: {}, requestId: {}]: {}",
                    MDC.get("traceId"), MDC.get("requestId"), e.getMessage(), e);
            return build(e.getMessage(), "DUPLICATE_REQUEST", HttpStatus.CONFLICT);
        }

        if (translated instanceof InvalidReferenceException e) {
            ERROR_LOGGER.error("Invalid reference error [traceId: {}, requestId: {}]: {}",
                    MDC.get("traceId"), MDC.get("requestId"), e.getMessage(), e);
            return build(e.getMessage(), "INVALID_REFERENCE", HttpStatus.BAD_REQUEST);
        }

        if (translated instanceof BusinessRuleViolationException e) {
            ERROR_LOGGER.error("Business rule violation error [traceId: {}, requestId: {}]: {}",
                    MDC.get("traceId"), MDC.get("requestId"), e.getMessage(), e);
            return build(e.getMessage(), "BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST);
        }

        if (ex instanceof ItemCategoryConflictException e) {
            ERROR_LOGGER.error("Invalid Category error [traceId: {}, requestId: {}]: {}",
                    MDC.get("traceId"), MDC.get("requestId"), e.getMessage(), e);
            return build(e.getMessage(), "INVALID CATEGORY EXCEPTION", HttpStatus.BAD_REQUEST);
        }

        ERROR_LOGGER.error("Unexpected error [traceId: {}, requestId: {}]: {}",
                MDC.get("traceId"), MDC.get("requestId"), ex.getMessage(), ex);
        return build(ex.getMessage(), "GENERIC_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> build(String msg, String code, HttpStatus status) {
        return new ResponseEntity<>(
                new ErrorResponse(msg, code, LocalDateTime.now()),
                status
        );

    }
}
