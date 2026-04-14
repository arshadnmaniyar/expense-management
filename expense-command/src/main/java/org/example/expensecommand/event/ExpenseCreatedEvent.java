package org.example.expensecommand.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseCreatedEvent {

    private String transactionId;
    private UUID userId;
    private LocalDate date;

    public ExpenseCreatedEvent() {}

    public ExpenseCreatedEvent(String transactionId, UUID userId, LocalDate date) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.date = date;
    }

    // getters and setters

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(Long expenseId) { this.transactionId = transactionId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
