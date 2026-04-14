package org.example.expensecommand.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "expense", schema = "expense_management", uniqueConstraints = {
        @UniqueConstraint(name = "expense_transaction_id_key", columnNames = "transaction_id")})

public class Expense {

    @Id
    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "payment_type_id", nullable = false)
    private UUID paymentTypeId;

//    @CreationTimestamp
//    @Column(name = "created_ts")
//    private LocalDateTime createdTs;
//
//    @UpdateTimestamp
//    @Column(name = "updated_ts")
//    private LocalDateTime updatedTs;

    // Constructors
    public Expense() {
    }

    public Expense(UUID expenseId, UUID transactionId, LocalDate purchaseDate, BigDecimal totalAmount, UUID storeId, UUID paymentTypeId) {
        this.expenseId = expenseId;
        this.transactionId = transactionId;
        this.purchaseDate = purchaseDate;
        this.totalAmount = totalAmount;
        this.storeId = storeId;
        this.paymentTypeId = paymentTypeId;
    }

    // Getters and Setters


    public UUID getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(UUID expenseId) {
        this.expenseId = expenseId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public UUID getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(UUID paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }
}
