package org.example.expensecommand.controller;

import org.example.expensecommand.dto.CreateExpenseRequestDto;
import org.example.expensecommand.dto.TransactionResult;
import org.example.expensecommand.service.ExpenseCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseCommandController {

    private final ExpenseCommandService expenseCommandService;

    public ExpenseCommandController(ExpenseCommandService expenseCommandService) {
        this.expenseCommandService = expenseCommandService;
    }

    @PostMapping
    public ResponseEntity<TransactionResult> createExpense(@Valid @RequestBody CreateExpenseRequestDto request) {
        TransactionResult transaction = expenseCommandService.createExpense(request);
        return ResponseEntity.status(201).body(transaction);
    }
}
