package org.example.expensecommand.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String errorCode,
        LocalDateTime timestamp
) {}