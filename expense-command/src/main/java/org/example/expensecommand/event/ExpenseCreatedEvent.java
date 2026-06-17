package org.example.expensecommand.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ExpenseCreatedEvent {

    private String transactionId;
    private LocalDate date;
    private String requestBody;

    public ExpenseCreatedEvent() {}

    public ExpenseCreatedEvent(String transactionId, LocalDate date, String requestBody) {
        this.transactionId = transactionId;
        this.date = date;
        this.requestBody=requestBody;
    }

    // getters and setters

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(Long expenseId) { this.transactionId = transactionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getRequestBody() {return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
}
