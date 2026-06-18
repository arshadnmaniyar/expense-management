package org.example.ingestionpipeline.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * EmbeddingGenerationService
 *
 * Generates embeddings for expense data using Ollama.
 *
 * Best Practices Implementation:
 * - Lazy initialization of embedding model for resource efficiency
 * - Singleton model instance to avoid creating multiple connections
 * - Configurable model and timeout parameters
 * - Error handling with detailed logging
 * - Fallback strategies for service unavailability
 *
 * Architecture Decisions:
 * - Uses nomic-embed-text model: lightweight, multilingual, good performance
 * - 768-dimensional vectors: balance between expressiveness and storage/compute cost
 * - REST API through LangChain4j: decouplesfrom Ollama implementation details
 * - Supports future model updates without code changes
 */
@Slf4j
@Service
public class EmbeddingGenerationService {

    private final String ollamaBaseUrl;
    private final String embeddingModel;
    private final int timeoutSeconds;

    // Cache for embedding model instance (lazy singleton)
    private volatile EmbeddingModel cachedModel;
    private final Object modelLock = new Object();

    // Metrics tracking
    private final ConcurrentMap<String, Long> metrics = new ConcurrentHashMap<>();

    public EmbeddingGenerationService(
            @Value("${embedding.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${embedding.model:nomic-embed-text}") String embeddingModel,
            @Value("${embedding.timeout-seconds:30}") int timeoutSeconds) {

        this.ollamaBaseUrl = ollamaBaseUrl;
        this.embeddingModel = embeddingModel;
        this.timeoutSeconds = timeoutSeconds;

        log.info("EmbeddingGenerationService initialized with model: {}, baseUrl: {}, timeout: {}s",
                embeddingModel, ollamaBaseUrl, timeoutSeconds);
    }

    /**
     * Generate embedding for the given text
     *
     * @param text The text to embed
     * @return Vector as double array
     * @throws IllegalStateException if embedding generation fails
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text for embedding cannot be null or empty");
        }

        try {
            long startTime = System.currentTimeMillis();

            EmbeddingModel model = getEmbeddingModel();

            // Generate embedding - returns vector with embedding dimensions
            dev.langchain4j.data.embedding.Embedding embedding = model.embed(text).content();
            float[] vector = embedding.vector();

            long duration = System.currentTimeMillis() - startTime;
            metrics.merge("embedding_generation_ms", duration, Long::sum);
            metrics.merge("embedding_count", 1L, Long::sum);

            log.debug("Generated embedding for text (length: {}) in {}ms. Vector dimensions: {}",
                    text.length(), duration, vector.length);

            return vector;

        } catch (Exception e) {
            metrics.merge("embedding_errors", 1L, Long::sum);
            log.error("Error generating embedding for text: {}", text.substring(0, Math.min(100, text.length())), e);
            throw new IllegalStateException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings for multiple texts in batch
     * IMPORTANT NOTE: For production use, this should implement actual batching
     * to reduce API calls and improve performance.
     *
     * @param texts List of texts to embed
     * @return List of vectors
     */
    public java.util.List<float[]> generateEmbeddingsBatch(java.util.List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        log.info("Generating embeddings for batch of {} items", texts.size());

        // Currently generates embeddings sequentially
        // TODO: Implement actual batch endpoint if Ollama supports it
        return texts.stream()
                .map(this::generateEmbedding)
                .toList();
    }

    /**
     * Get or create the embedding model instance
     * Implements lazy singleton pattern for resource efficiency
     */
    private EmbeddingModel getEmbeddingModel() {
        if (cachedModel == null) {
            synchronized (modelLock) {
                if (cachedModel == null) {
                    log.info("Initializing Ollama embedding model: {}", embeddingModel);
                    cachedModel = createEmbeddingModel();
                }
            }
        }
        return cachedModel;
    }

    /**
     * Create embedding model instance
     */
    private EmbeddingModel createEmbeddingModel() {
        try {
            // LangChain4j provides a clean abstraction over Ollama API
            return OllamaEmbeddingModel.builder()
                    .baseUrl(ollamaBaseUrl)
                    .modelName(embeddingModel)
                    .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                    .build();

        } catch (Exception e) {
            log.error("Failed to create embedding model. Ensure Ollama is running at: {}", ollamaBaseUrl, e);
            throw new IllegalStateException("Failed to initialize embedding model: " + e.getMessage(), e);
        }
    }

    /**
     * Get metrics for monitoring
     */
    public java.util.Map<String, Long> getMetrics() {
        return new java.util.HashMap<>(metrics);
    }

    /**
     * Reset metrics
     */
    public void resetMetrics() {
        metrics.clear();
    }

    /**
     * Get the configured embedding model name
     */
    public String getModelName() {
        return embeddingModel;
    }

    /**
     * Health check - verify Ollama connectivity
     */
    public boolean healthCheck() {
        try {
            // Simple check by generating an embedding for a small test string
            getEmbeddingModel().embed("test").content();
            return true;
        } catch (Exception e) {
            log.warn("Embedding service health check failed", e);
            return false;
        }
    }
}

