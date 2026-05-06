package org.example.expensecommand.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expensecommand.model.ExpenseSearchResponse;
import org.example.expensecommand.service.ExpenseQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ExpenseQueryController {

    private final ExpenseQueryService expenseQueryService;

    @GetMapping("/expenses")
    public ResponseEntity<ExpenseSearchResponse> getExpenses(
            @RequestParam(required = false) LocalDate purchaseDate,
            @RequestParam(required = false) LocalDate transactionDate,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String store,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "transactionDate:desc") String sort,
            @RequestParam(required = false) String cursor) {

        log.info("Getting expenses with criteria: purchaseDate={}, transactionDate={}, paymentType={}, itemName={}, category={}, store={}, limit={}, sort={}, cursor={}",
                purchaseDate, transactionDate, paymentType, itemName, category, store, limit, sort, cursor);

        ExpenseSearchResponse response = expenseQueryService.searchExpenses(
                purchaseDate, transactionDate, paymentType, itemName, category, store, limit, sort, cursor);

        return ResponseEntity.ok(response);
    }
}
