package org.example.expensecommand.domain.expense;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_item", schema = "expense_management")

public class ExpenseItem {

    @Id
    @Column(name = "expense_item_id")
    private UUID expenseItemId;

    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "item_master_id", nullable = false)
    private UUID itemMasterId;

    @Column(name ="amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "comments")
    private String comments;

    public ExpenseItem() {}

    public ExpenseItem(UUID expenseId, UUID itemMasterId, BigDecimal amount, Integer quantity, String comments) {
        this.expenseId = expenseId;
        this.itemMasterId = itemMasterId;
        this.amount = amount;
        this.quantity = quantity;
        this.comments = comments;
    }

    // Getters and Setters


    public UUID getExpenseItemId() {
        return expenseItemId;
    }

    public void setExpenseItemId(UUID expenseItemId) {
        this.expenseItemId = expenseItemId;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(UUID expenseId) {
        this.expenseId = expenseId;
    }

    public UUID getItemMasterId() {
        return itemMasterId;
    }

    public void setItemMasterId(UUID itemMasterId) {
        this.itemMasterId = itemMasterId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
