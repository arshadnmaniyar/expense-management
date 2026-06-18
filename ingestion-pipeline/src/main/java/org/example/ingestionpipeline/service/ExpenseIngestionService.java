package org.example.ingestionpipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.example.ingestionpipeline.domain.ExpenseEmbedding;
import org.example.ingestionpipeline.event.CreateExpenseRequest;
import org.example.ingestionpipeline.event.ExpenseCreatedEvent;
import org.example.ingestionpipeline.event.ExpenseItem;
import org.example.ingestionpipeline.repository.ExpenseEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ExpenseIngestionService
 *
 * Orchestrates the ingestion pipeline for expense events:
 * 1. Validates and parses expense event
 * 2. Builds semantic text representation
 * 3. Generates embeddings using Ollama
 * 4. Stores embeddings in pgvector
 *
 * Best Practices:
 * - Transactional consistency: all-or-nothing processing
 * - Retry logic with exponential backoff for transient failures
 * - Detailed logging for observability and debugging
 * - Idempotency: checks for duplicate embeddings before processing
 * - Exception handling with clear error messages
 */
@Slf4j
@Service
public class ExpenseIngestionService {

    private final EmbeddingGenerationService embeddingGenerationService;
    private final ExpenseEmbeddingRepository expenseEmbeddingRepository;
    private final ObjectMapper objectMapper;

    public ExpenseIngestionService(
            EmbeddingGenerationService embeddingGenerationService,
            ExpenseEmbeddingRepository expenseEmbeddingRepository,
            ObjectMapper objectMapper) {

        this.embeddingGenerationService = embeddingGenerationService;
        this.expenseEmbeddingRepository = expenseEmbeddingRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Process an expense event and generate embeddings
     *
     * @param event The expense created event from Kafka
     * @return The created embedding ID
     * @throws IllegalArgumentException if event data is invalid
     * @throws IllegalStateException if embedding generation fails
     */
    @Transactional
    @Retry(name = "expenseIngestion", fallbackMethod = "handleIngestionFailure")
    public String processExpenseEvent(ExpenseCreatedEvent event) {
        log.info("Processing expense event: transactionId={}", event.getTransactionId());

        // Validation - let IllegalArgumentException propagate (don't retry on validation errors)
        if (event == null || event.getTransactionId() == null) {
            throw new IllegalArgumentException("Invalid event: transaction ID is required");
        }

        try {
            // Idempotency check - avoid processing duplicate events
            if (expenseEmbeddingRepository.existsByTransactionId(event.getTransactionId())) {
                log.warn("Embedding already exists for transaction: {}", event.getTransactionId());
                return null;
            }

            // Parse expense request from event payload
            CreateExpenseRequest expenseRequest = parseExpenseRequest(event.getRequestBody());

            // Build semantic text representation
            String embeddingText = expenseRequest.buildEmbeddingText();
            log.debug("Built embedding text for transaction: {} (length: {})",
                    event.getTransactionId(), embeddingText.length());

            // Generate embedding vector
            float[] embeddingVector = embeddingGenerationService.generateEmbedding(embeddingText);
            log.debug("Generated embedding vector with {} dimensions", embeddingVector.length);

            // Create and store embedding
            ExpenseEmbedding embedding = ExpenseEmbedding.create(
                    event.getTransactionId(),
                    expenseRequest.getUserId(),
                    event.getDate(),
                    expenseRequest.getStore(),
                    getPrimaryCategory(expenseRequest),
                    calculateTotalAmount(expenseRequest),
                    embeddingText,
                    embeddingVector,
                    embeddingGenerationService.getModelName()
            );

            ExpenseEmbedding savedEmbedding = expenseEmbeddingRepository.save(embedding);

            log.info("Successfully processed expense event: transactionId={}, embeddingId={}",
                    event.getTransactionId(), savedEmbedding.getEmbeddingId());

            return savedEmbedding.getEmbeddingId().toString();

        } catch (IllegalArgumentException e) {
            // Re-throw validation errors directly (don't retry)
            throw e;
        } catch (Exception e) {
            log.error("Error processing expense event: transactionId={}",
                    event.getTransactionId(), e);
            // Wrap other exceptions for retry logic
            throw new IllegalStateException("Failed to process expense event", e);
        }
    }

    /**
     * Parse the JSON requestBody to CreateExpenseRequest
     */
    private CreateExpenseRequest parseExpenseRequest(String requestBody) {
        try {
            CreateExpenseRequest request = objectMapper.readValue(requestBody, CreateExpenseRequest.class);

            if (request.getUserId() == null) {
                throw new IllegalArgumentException("User ID is required in expense request");
            }

            if (request.getPurchaseDate() == null) {
                throw new IllegalArgumentException("Purchase date is required in expense request");
            }

            if (request.getItems() == null) {
                request.setItems(new ArrayList<>());
            }

            log.debug("Parsed expense request: userId={}, itemCount={}",
                    request.getUserId(), request.getItems().size());

            return request;

        } catch (Exception e) {
            log.error("Failed to parse expense request body", e);
            throw new IllegalArgumentException("Invalid expense request body: " + e.getMessage(), e);
        }
    }

    /**
     * Get the primary category from expense items
     * Uses the first non-null category, or defaults to "General"
     */
    private String getPrimaryCategory(CreateExpenseRequest request) {
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (ExpenseItem item : request.getItems()) {
                if (item.getCategory() != null) {
                    return item.getCategory();
                }
            }
        }
        return "General";
    }

    /**
     * Calculate total amount from all items
     */
    private BigDecimal calculateTotalAmount(CreateExpenseRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return request.getItems().stream()
                .map(item -> {
                    if (item.getAmount() != null) {
                        return BigDecimal.valueOf(item.getAmount());
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Fallback method for retry failure
     * Logs and returns null status
     */
    @SuppressWarnings("unused")
    public String handleIngestionFailure(ExpenseCreatedEvent event, Exception e) {
        log.error("Failed to process expense event after retries: transactionId={}",
                event.getTransactionId(), e);
        return null;
    }

    /**
     * Get service health status
     */
    public boolean isHealthy() {
        return embeddingGenerationService.healthCheck();
    }
}

