package org.example.ingestionpipeline.domain;

import lombok.*;
import com.pgvector.PGvector;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.example.ingestionpipeline.config.hibernate.PGvectorType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ExpenseEmbedding Entity
 *
 * Stores expense embeddings and metadata for vector similarity search.
 * This entity enables semantic search capabilities for expenses and supports
 * future AI-driven analytics and recommendations.
 *
 * Architecture Considerations:
 * - Denormalized store for performance: avoids joins for embeddings
 * - Metadata preserved for filtering and context: enables multi-dimensional queries
 * - Vector indexes (HNSW): optimized for similarity searches at scale
 * - Embedding model versioning: supports model updates and compatibility
 */
@Entity
@Table(name = "expense_embeddings", schema = "expense_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseEmbedding {

    @Id
    @Column(name = "embedding_id", nullable = false)
    private UUID embeddingId;

    // Reference to original transaction
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    // User reference for multi-tenant queries
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Temporal data for filtering and analytics
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    // Expense context for filtering and metadata
    @Column(name = "store_name", length = 500)
    private String storeName;

    @Column(name = "category", length = 200)
    private String category;

    @Column(name = "sub_category", length = 200)
    private String subCategory;

    // Numerical value for range queries and analytics
    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    // Raw text representation for explainability and future vector updates
    @Column(name = "embedding_text", nullable = false, columnDefinition = "TEXT")
    private String embeddingText;

    // The vector itself - stored as pgvector type
    @Type(PGvectorType.class)
    @Column(name = "embedding_vector", nullable = false, columnDefinition = "vector")
    private PGvector embeddingVector;

    // Model identifier for versioning (supports switching/updating embedding models)
    @Column(name = "embedding_model", length = 100)
    @Builder.Default
    private String embeddingModel = "ollama-nomic-embed-text";

    // Lifecycle tracking for auditing
    @Column(name = "embedding_generated_at", nullable = false)
    @Builder.Default
    private LocalDateTime embeddingGeneratedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Factory method to create an embedding from expense data
     * Implements best practices for embedding creation:
     * - Combines multiple text sources for semantic richness
     * - Uses consistent formatting for reproducibility
     */
    public static ExpenseEmbedding create(
            String transactionId,
            UUID userId,
            LocalDate expenseDate,
            String storeName,
            String category,
            BigDecimal amount,
            String embeddingText,
            float[] embeddingVector,
            String embeddingModel) {

        return ExpenseEmbedding.builder()
                .embeddingId(UUID.randomUUID())
                .transactionId(transactionId)
                .userId(userId)
                .expenseDate(expenseDate)
                .storeName(storeName)
                .category(category)
                .amount(amount)
                .embeddingText(embeddingText)
                .embeddingVector(new PGvector(embeddingVector))
                .embeddingModel(embeddingModel)
                .embeddingGeneratedAt(LocalDateTime.now())
                .build();
    }
}
