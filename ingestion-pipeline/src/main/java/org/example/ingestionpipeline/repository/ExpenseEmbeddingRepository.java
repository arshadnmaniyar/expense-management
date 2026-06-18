package org.example.ingestionpipeline.repository;

import org.example.ingestionpipeline.domain.ExpenseEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * ExpenseEmbeddingRepository
 *
 * Repository for managing expense embeddings with vector similarity search capabilities.
 */
@Repository
public interface ExpenseEmbeddingRepository extends JpaRepository<ExpenseEmbedding, UUID> {

    /**
     * Find embedding by transaction ID
     */
    Optional<ExpenseEmbedding> findByTransactionId(String transactionId);

    /**
     * Find all embeddings for a user
     */
    List<ExpenseEmbedding> findByUserId(UUID userId);

    /**
     * Find embeddings by user and category
     */
    List<ExpenseEmbedding> findByUserIdAndCategory(UUID userId, String category);

    /**
     * Find embeddings by user and date range
     */
    List<ExpenseEmbedding> findByUserIdAndExpenseDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    /**
     * Vector similarity search using cosine distance
     * Returns expenses similar to a given embedding vector
     *
     * @param userId User ID for filtering
     * @param embeddingVector The query vector
     * @param limit Maximum number of results
     * @return List of similar embeddings ordered by similarity
     */
    @Query(value = """
        SELECT e.* FROM expense_management.expense_embeddings e
        WHERE e.user_id = :userId
        ORDER BY e.embedding_vector <=> :embeddingVector
        LIMIT :limit
        """, nativeQuery = true)
    List<ExpenseEmbedding> findSimilarByVector(
            @Param("userId") UUID userId,
            @Param("embeddingVector") String embeddingVector,
            @Param("limit") int limit
    );

    /**
     * Find embeddings by user ID and search across embedding text
     * Uses PostgreSQL full-text search for keyword matching
     */
    @Query(value = """
        SELECT e.* FROM expense_management.expense_embeddings e
        WHERE e.user_id = :userId
        AND e.embedding_text ILIKE CONCAT('%', :searchText, '%')
        """, nativeQuery = true)
    List<ExpenseEmbedding> searchByText(
            @Param("userId") UUID userId,
            @Param("searchText") String searchText
    );

    /**
     * Check if embedding already exists for a transaction
     */
    boolean existsByTransactionId(String transactionId);
}

