package org.example.expensecommand.exception;

import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class PostgresExceptionTranslator {

    public RuntimeException translate(Exception ex) {

        Throwable root = getRootCause(ex);

        if (root instanceof PSQLException psqlEx) {
            String sqlState = psqlEx.getSQLState();
            String message = psqlEx.getMessage();

            return switch (sqlState) {

                // NOT NULL violation
                case "23502" -> new MissingRequiredFieldException(
                        extractColumnName(message) + " is required"
                );

                // Unique constraint violation
                case "23505" -> new DuplicateRequestException(
                        extractConstraintName(message) + " already exists"
                );

                // Foreign key violation
                case "23503" -> new InvalidReferenceException(
                        "Invalid reference: " + extractConstraintName(message)
                );

                // Check constraint violation
                case "23514" -> new BusinessRuleViolationException(
                        "Constraint violated: " + extractConstraintName(message)
                );

                default -> new DatabaseOperationException("Database error occurred");
            };
        }

        // Spring's wrapper for DB errors
        if (root instanceof DataIntegrityViolationException) {
            return new DatabaseOperationException("Data integrity violation");
        }

        return new DatabaseOperationException("Unexpected database error");
    }

    private Throwable getRootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String extractColumnName(String message) {
        int start = message.indexOf("\"") + 1;
        int end = message.indexOf("\"", start);
        return message.substring(start, end);
    }

    private String extractConstraintName(String message) {
        int idx = message.indexOf("constraint");
        if (idx == -1) return "unknown";
        int start = message.indexOf("\"", idx) + 1;
        int end = message.indexOf("\"", start);
        return message.substring(start, end);
    }
}
