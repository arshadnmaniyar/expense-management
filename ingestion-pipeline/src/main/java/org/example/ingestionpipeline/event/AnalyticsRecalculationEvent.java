package org.example.ingestionpipeline.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AnalyticsRecalculationEvent
 *
 * Event published by the ingestion pipeline after successfully storing embeddings.
 * This event triggers analytics recalculation in the analytics service.
 *
 * Architecture:
 * - Decouples embedding generation from analytics computation
 * - Enables asynchronous analytics processing
 * - Provides traceability between embedding and analytics events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsRecalculationEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("embeddingId")
    private String embeddingId;

    @JsonProperty("eventType")
    private String eventType; // e.g., "EXPENSE_EMBEDDED"

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("source")
    @Builder.Default
    private String source = "ingestion-pipeline";

    /**
     * Factory method to create an analytics event from embedding data
     */
    public static AnalyticsRecalculationEvent fromEmbedding(
            String transactionId,
            UUID userId,
            String embeddingId) {

        return AnalyticsRecalculationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .transactionId(transactionId)
                .userId(userId)
                .embeddingId(embeddingId)
                .eventType("EXPENSE_EMBEDDED")
                .timestamp(LocalDateTime.now())
                .source("ingestion-pipeline")
                .build();
    }
}

