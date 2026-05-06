package org.example.expensecommand.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateExpenseRequestDto {
    private UUID userId;
    private String idempotencyKey;
    private LocalDate purchaseDate;
    private List<ExpenseItemDto> items;
    private String store;
    private String paymentType;

    // constructors, getters, setters, builder

    public CreateExpenseRequestDto() {}

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public List<ExpenseItemDto> getItems() {
        return items;
    }

    public void setItems(List<ExpenseItemDto> items) {
        this.items = items;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
