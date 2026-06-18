package org.example.ingestionpipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.ingestionpipeline.domain.ExpenseEmbedding;
import org.example.ingestionpipeline.event.CreateExpenseRequest;
import org.example.ingestionpipeline.event.ExpenseCreatedEvent;
import org.example.ingestionpipeline.event.ExpenseItem;
import org.example.ingestionpipeline.repository.ExpenseEmbeddingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpenseIngestionService
 */
class ExpenseIngestionServiceTest {

    @Mock
    private EmbeddingGenerationService embeddingGenerationService;

    @Mock
    private ExpenseEmbeddingRepository expenseEmbeddingRepository;

    private ExpenseIngestionService expenseIngestionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        expenseIngestionService = new ExpenseIngestionService(
                embeddingGenerationService,
                expenseEmbeddingRepository,
                objectMapper
        );
    }

    @Test
    void testProcessExpenseEvent_Success() throws JsonProcessingException {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateExpenseRequest request = CreateExpenseRequest.builder()
                .userId(userId)
                .purchaseDate(LocalDate.now())
                .store("Target")
                .paymentType("Credit Card")
                .items(Arrays.asList(
                        ExpenseItem.builder()
                                .itemName("Groceries")
                                .category("Food")
                                .amount(50.0)
                                .build()
                ))
                .build();

        ExpenseCreatedEvent event = ExpenseCreatedEvent.builder()
                .transactionId("txn-123")
                .date(LocalDate.now())
                .requestBody(objectMapper.writeValueAsString(request))
                .build();

        // Mock embedding generation
        float[] mockEmbedding = new float[]{0.1F, 0.2F, 0.3F};
        when(embeddingGenerationService.generateEmbedding(anyString()))
                .thenReturn(mockEmbedding);
        when(embeddingGenerationService.getModelName())
                .thenReturn("nomic-embed-text");
        when(expenseEmbeddingRepository.existsByTransactionId("txn-123"))
                .thenReturn(false);
        when(expenseEmbeddingRepository.save(any(ExpenseEmbedding.class)))
                .thenReturn(ExpenseEmbedding.builder()
                        .embeddingId(UUID.randomUUID())
                        .transactionId("txn-123")
                        .userId(userId)
                        .build());

        // Act
        String result = expenseIngestionService.processExpenseEvent(event);

        // Assert
        assertNotNull(result);
        verify(embeddingGenerationService).generateEmbedding(anyString());
        verify(expenseEmbeddingRepository).save(any(ExpenseEmbedding.class));
    }

    @Test
    void testProcessExpenseEvent_DuplicateEvent() throws JsonProcessingException {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateExpenseRequest request = CreateExpenseRequest.builder()
                .userId(userId)
                .purchaseDate(LocalDate.now())
                .items(List.of())
                .build();

        ExpenseCreatedEvent event = ExpenseCreatedEvent.builder()
                .transactionId("txn-456")
                .date(LocalDate.now())
                .requestBody(objectMapper.writeValueAsString(request))
                .build();

        when(expenseEmbeddingRepository.existsByTransactionId("txn-456"))
                .thenReturn(true);

        // Act
        String result = expenseIngestionService.processExpenseEvent(event);

        // Assert
        assertNull(result);
        verify(embeddingGenerationService, never()).generateEmbedding(anyString());
        verify(expenseEmbeddingRepository, never()).save(any(ExpenseEmbedding.class));
    }

    @Test
    void testProcessExpenseEvent_InvalidEvent() {
        // Arrange
        ExpenseCreatedEvent invalidEvent = ExpenseCreatedEvent.builder()
                .transactionId(null)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> expenseIngestionService.processExpenseEvent(invalidEvent));
    }

    @Test
    void testIsHealthy() {
        // Arrange
        when(embeddingGenerationService.healthCheck()).thenReturn(true);

        // Act
        boolean healthy = expenseIngestionService.isHealthy();

        // Assert
        assertTrue(healthy);
    }
}

