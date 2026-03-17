package org.example.expensecommand.event;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseCreatedEvent {

    private Long expenseId;
    private String userId;
    private BigDecimal amount;
    private String description;
    private String category;
    private LocalDate date;

    public ExpenseCreatedEvent() {}

    public ExpenseCreatedEvent(Long expenseId, String userId, BigDecimal amount, String description, String category, LocalDate date) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }

    // getters and setters

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
