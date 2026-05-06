package org.example.expensecommand.dto;

import java.util.UUID;

public class TransactionResult {
    private UUID transactionId;
    private String status;
    // getters/setters/constructor

    public TransactionResult() {}
    public UUID getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}