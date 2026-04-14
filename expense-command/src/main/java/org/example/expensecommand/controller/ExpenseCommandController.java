package org.example.expensecommand.controller;

import org.example.expensecommand.domain.Transaction;
import org.example.expensecommand.dto.CreateExpenseRequest;
import org.example.expensecommand.service.ExpenseCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    public ResponseEntity<Transaction> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        Transaction transaction = expenseCommandService.createExpense(request);
        return ResponseEntity.ok(transaction);
    }
}
