package org.example.expensecommand.service;

import org.example.expensecommand.dto.CreateExpenseRequestDto;
import org.example.expensecommand.dto.TransactionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ExpenseCommandService
 *
 * DDD Principle: Presentation/API Service
 * - Delegates to application service
 * - Handles request/response transformation
 * - Returns transactional results
 */
@Service
public class ExpenseCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpenseCommandService.class);

    private final CreateExpenseApplicationService createExpenseApplicationService;

    public ExpenseCommandService(CreateExpenseApplicationService createExpenseApplicationService) {
        this.createExpenseApplicationService = createExpenseApplicationService;
    }

    /**
     * Create expense command endpoint handler
     *
     * @param request CreateExpenseRequestDto
     * @return TransactionResult with created transaction ID
     */
    @Transactional
    public TransactionResult createExpense(CreateExpenseRequestDto request) {
        LOGGER.info("ExpenseCommandService.createExpense: userId={}, purchaseDate={}",
                request.getUserId(), request.getPurchaseDate());

        try {
            // Delegate to application service
            UUID transactionId = createExpenseApplicationService.executeCreateExpense(request);

            // Build response
            TransactionResult result = new TransactionResult();
            result.setTransactionId(transactionId);
            result.setStatus("S01");

            LOGGER.info("ExpenseCommandService.createExpense completed successfully: transactionId={}", transactionId);
            return result;

        } catch (Exception e) {
            LOGGER.error("Error in ExpenseCommandService.createExpense: userId={}", request.getUserId(), e);
            throw e;
        }
    }
}
