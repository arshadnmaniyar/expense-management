package org.example.expensecommand.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "transaction", schema = "expense_management")

public class Transaction {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column (name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;


    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_type_cd", columnDefinition = "expense_management.transaction_type", nullable = false)
    private TransactionType transactionTypeCd;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "created_ts")
    private LocalDateTime createdTs;

    @UpdateTimestamp
    @Column(name = "updated_ts")
    private LocalDateTime updatedTs;

    // Constructors
    public Transaction() {}

    public Transaction(UUID transactionId, UUID idempotencyKey, LocalDate transactionDate, TransactionType transactionTypeCd, UUID userId) {
        this.transactionId = transactionId;
        this.idempotencyKey = idempotencyKey;
        this.transactionDate = transactionDate;
        this.transactionTypeCd = transactionTypeCd;
        this.userId = userId;
    }

    // Getters and Setters
    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }


    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionTypeCd() {
        return transactionTypeCd;
    }

    public void setTransactionTypeCd(TransactionType transactionTypeCd) {
        this.transactionTypeCd = transactionTypeCd;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(LocalDateTime createdTs) {
        this.createdTs = createdTs;
    }

    public LocalDateTime getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(LocalDateTime updatedTs) {
        this.updatedTs = updatedTs;
    }
}
