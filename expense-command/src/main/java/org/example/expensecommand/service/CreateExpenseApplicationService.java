package org.example.expensecommand.service;

import org.example.expensecommand.domain.Transaction.Transaction;
import org.example.expensecommand.domain.Transaction.TransactionRepository;
import org.example.expensecommand.domain.Transaction.TransactionType;
import org.example.expensecommand.domain.expense.Expense;
import org.example.expensecommand.domain.expense.ExpenseItem;
import org.example.expensecommand.domain.expense.ExpenseItemRepository;
import org.example.expensecommand.domain.expense.ExpenseRepository;
import org.example.expensecommand.domain.items.ItemsMaster;
import org.example.expensecommand.domain.outbox.Outbox;
import org.example.expensecommand.domain.outbox.OutboxRepository;
import org.example.expensecommand.dto.CreateExpenseRequestDto;
import org.example.expensecommand.dto.ExpenseItemDto;
import org.example.expensecommand.event.ExpenseCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application Service for Creating Expenses
 *
 * DDD Principle: Application Service
 * - Orchestrates domain services and repositories
 * - Implements the complete workflow: Transaction → Expense → ExpenseItems → Event
 * - Handles transactional boundaries
 *
 * Workflow:
 * 1. Create Transaction domain model
 * 2. Create Expense domain model
 * 3. For each item: Resolve category and create ExpenseItem
 * 4. Record integration event in Outbox (for event sourcing)
 * 5. Return result
 */
@Service
public class CreateExpenseApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateExpenseApplicationService.class);

    private final TransactionRepository transactionRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final OutboxRepository outboxRepository;
    private final StoreService storeService;
    private final ItemMasterService itemMasterService; // Inject ItemMasterService
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;


    public CreateExpenseApplicationService(
            TransactionRepository transactionRepository,
            ExpenseRepository expenseRepository,
            ExpenseItemRepository expenseItemRepository,
            OutboxRepository outboxRepository,
            StoreService storeService,
            ItemMasterService itemMasterService,
            ObjectMapper objectMapper,
            PaymentService paymentService) {
        this.transactionRepository = transactionRepository;
        this.expenseRepository = expenseRepository;
        this.expenseItemRepository = expenseItemRepository;
        this.outboxRepository = outboxRepository;
        this.storeService = storeService;
        this.itemMasterService = itemMasterService; // Assign ItemMasterService
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    /**
     * Execute create expense command
     *
     * @param request CreateExpenseRequestDto
     * @return expenseId (transaction ID)
     * @throws RuntimeException if persistence fails
     */
    @Transactional
    public UUID executeCreateExpense(CreateExpenseRequestDto request) {
        LOGGER.info("Executing CreateExpenseApplicationService for userId: {}, purchaseDate: {}",
                request.getUserId(), request.getPurchaseDate());

        try {
            // Step 1: Resolve store (get existing or create new)
            UUID storeId = storeService.getOrCreateStore(request.getStore());
            LOGGER.debug("Resolved store ID: {}", storeId);

            UUID paymentTypeId = paymentService.getPaymentTypeId(request.getPaymentType());

            // Step 2: Create Transaction aggregate
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = new Transaction(
                    transactionId,
                    UUID.fromString(request.getIdempotencyKey()),
                    request.getPurchaseDate(),
                    TransactionType.EXPENSE,
                    request.getUserId()
            );
            transaction = transactionRepository.save(transaction);
            LOGGER.debug("Transaction created: transactionId={}", transactionId);

            // Step 3: Create Expense aggregate
            UUID expenseId = UUID.randomUUID();
            BigDecimal totalAmount = calculateTotalAmount(request);

            Expense expense = new Expense(
                    expenseId,
                    transactionId,
                    request.getPurchaseDate(),
                    totalAmount,
                    storeId,
                    paymentTypeId  // Default payment type
            );
            expense = expenseRepository.save(expense);
            LOGGER.debug("Expense created: expenseId={}, totalAmount={}", expenseId, totalAmount);

            // Step 4: Create ExpenseItems for each item in request
            for (ExpenseItemDto itemDto : request.getItems()) {
                createExpenseItem(expense, itemDto);
            }

            // Step 5: Record integration event in Outbox
            publishExpenseCreatedEvent(transaction,objectMapper.writeValueAsString(request));

            LOGGER.info("CreateExpenseApplicationService completed successfully. TransactionId: {}", transactionId);
            return transactionId;

        }
        catch (JsonProcessingException jpe) {
            LOGGER.error("Error serializing CreateExpenseRequestDto for userId: {}", request.getUserId(), jpe);
            throw new RuntimeException("Failed to serialize expense event payload", jpe);
        }
        catch (Exception e) {
            LOGGER.error("Error in CreateExpenseApplicationService for userId: {}", request.getUserId(), e);
            throw e;
        }

    }

    /**
     * Create an ExpenseItem for a given Expense and item details
     *
     * Process:
     * 1. Resolve or create ItemMaster using ItemMasterService
     * 2. Parse quantity as Integer
     * 3. Create ExpenseItem entity with itemMasterId
     * 4. Persist to database
     *
     * @param expense Parent Expense entity
     * @param itemDto ExpenseItemDto from request
     */
    private void createExpenseItem(Expense expense, ExpenseItemDto itemDto) {
        LOGGER.debug("Creating ExpenseItem: itemName={}, category={}, subCategory={}",
                itemDto.getItemName(), itemDto.getCategory(), itemDto.getSubCategory());

        // Step 1: Resolve or create ItemMaster
        ItemsMaster itemMaster = itemMasterService.getOrCreateItemMaster(
                itemDto.getItemName(),
                itemDto.getCategory(),
                itemDto.getSubCategory()
        );
        LOGGER.debug("Resolved ItemMaster ID: {} for item: {}", itemMaster.getItemMasterId(), itemDto.getItemName());

        // Step 2: Parse quantity
        Integer quantity = parseQuantity(itemDto.getQuantity());

        // Step 3: Create and save ExpenseItem
        UUID expenseItemId = UUID.randomUUID();
        ExpenseItem expenseItem = new ExpenseItem(
                expense.getExpenseId(),
                itemMaster.getItemMasterId(), // Use itemMasterId
                BigDecimal.valueOf(itemDto.getAmount()),
                quantity,
                itemDto.getComments()
        );
        expenseItem.setExpenseItemId(expenseItemId);
        expenseItemRepository.save(expenseItem);

        LOGGER.debug("ExpenseItem created: expenseItemId={}, itemMasterId={}, amount={}, quantity={}",
                expenseItemId, itemMaster.getItemMasterId(), itemDto.getAmount(), quantity);
    }

    /**
     * Publish ExpenseCreated event to Outbox
     * This ensures transactional outbox pattern - event is logged in same transaction as entity creation
     *
     * @param transaction Transaction entity
     */
    private void publishExpenseCreatedEvent(Transaction transaction, String requestBody) {
        try {
            ExpenseCreatedEvent event = new ExpenseCreatedEvent(
                    transaction.getTransactionId().toString(),
                    transaction.getTransactionDate(),
                    requestBody
            );
            
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = new Outbox("ExpenseCreated", payload);
            outboxRepository.save(outbox);

            LOGGER.debug("ExpenseCreatedEvent published to Outbox: transactionId={}",
                    transaction.getTransactionId());
        } catch (Exception e) {
            LOGGER.error("Error publishing ExpenseCreatedEvent", e);
            throw new RuntimeException("Failed to publish expense event", e);
        }
    }

    /**
     * Calculate total amount from all items
     */
    private BigDecimal calculateTotalAmount(CreateExpenseRequestDto request) {
        return request.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Parse quantity string to Integer
     * Validates that quantity is a valid positive integer
     */
    private Integer parseQuantity(String quantityStr) {
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity format: " + quantityStr, e);
        }
    }

    /**
     * Get default payment type ID
     * TODO: This should be parameterized or fetched from request when payment type support is added
     */
    private UUID getDefaultPaymentTypeId() {
        // For now, returning a placeholder UUI
        // In future, this should be resolved from request or configuration
        return UUID.fromString("12345678-1234-1234-1234-123456789012");
    }
}
