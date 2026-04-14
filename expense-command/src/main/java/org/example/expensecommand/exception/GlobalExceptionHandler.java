package org.example.expensecommand.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final PostgresExceptionTranslator translator;

    public GlobalExceptionHandler(PostgresExceptionTranslator translator) {
        this.translator = translator;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {

        RuntimeException translated = translator.translate(ex);
        if (translated instanceof MissingRequiredFieldException e) {
            return build(e.getMessage(), "MISSING_REQUIRED_FIELD", HttpStatus.BAD_REQUEST);
        }

        if (translated instanceof DuplicateRequestException e) {
            return build(e.getMessage(), "DUPLICATE_REQUEST", HttpStatus.CONFLICT);
        }

        if (translated instanceof InvalidReferenceException e) {
            return build(e.getMessage(), "INVALID_REFERENCE", HttpStatus.BAD_REQUEST);
        }

        if (translated instanceof BusinessRuleViolationException e) {
            return build(e.getMessage(), "BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST);
        }

        return build(ex.getMessage(), "GENERIC_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> build(String msg, String code, HttpStatus status) {
        return new ResponseEntity<>(
                new ErrorResponse(msg, code, LocalDateTime.now()),
                status
        );


    }
}
