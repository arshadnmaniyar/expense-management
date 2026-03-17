package org.example.expensecommand.controller;

import org.example.expensecommand.domain.Expense;
import org.example.expensecommand.service.ExpenseCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/expenses")
public class ExpenseCommandController {

    private final ExpenseCommandService expenseCommandService;

    public ExpenseCommandController(ExpenseCommandService expenseCommandService) {
        this.expenseCommandService = expenseCommandService;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        Expense expense = expenseCommandService.createExpense(
            request.getUserId(),
            request.getAmount(),
            request.getDescription(),
            request.getCategory(),
            request.getDate()
        );
        return ResponseEntity.ok(expense);
    }

    public static class CreateExpenseRequest {
        private String userId;
        private BigDecimal amount;
        private String description;
        private String category;
        private LocalDate date;

        // getters and setters

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
}
