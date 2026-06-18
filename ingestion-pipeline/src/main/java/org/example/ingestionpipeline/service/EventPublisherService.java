package org.example.ingestionpipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.ingestionpipeline.event.AnalyticsRecalculationEvent;
import org.example.ingestionpipeline.event.ExpenseCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * EventPublisherService
 *
 * Publishes events from the ingestion pipeline to Kafka topics.
 *
 * Current Integration Points:
 * - Publishes AnalyticsRecalculationEvent to "analytics-recalculation" topic
 *
 * Architecture Considerations:
 * - Decouples embedding generation from analytics computation
 * - Enables asynchronous analytics processing
 * - Provides event traceability through headers
 * - Supports future integrations with other services
 *
 * Best Practices:
 * - Add trace/correlation IDs to event headers
 * - Log event publication for observability
 * - Handle publishing errors gracefully
 * - Support batch publishing for efficiency
 */
@Slf4j
@Service
public class EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisherService(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish an analytics recalculation event
     *
     * This event signals that a new expense has been processed and embeddings generated.
     * The analytics service will subscribe to this event and trigger recalculation
     * of user analytics and spending patterns.
     *
     * @param transactionId The expense transaction ID
     * @param embeddingId The embedding ID generated for this expense
     * @param originalEvent The original expense created event
     */
    public void publishAnalyticsRecalculationEvent(
            String transactionId,
            String embeddingId,
            ExpenseCreatedEvent originalEvent) {

        try {
            // Create analytics event
            AnalyticsRecalculationEvent event = AnalyticsRecalculationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .transactionId(transactionId)
                    .embeddingId(embeddingId)
                    .eventType("EXPENSE_EMBEDDED")
                    .timestamp(java.time.LocalDateTime.now())
                    .source("ingestion-pipeline")
                    .build();

            // Extract user ID from original event
            // Note: requestBody contains serialized CreateExpenseRequestDto
            // Parse it to get userId
            try {
                org.example.ingestionpipeline.event.CreateExpenseRequest request =
                        objectMapper.readValue(
                                originalEvent.getRequestBody(),
                                org.example.ingestionpipeline.event.CreateExpenseRequest.class
                        );
                if (request.getUserId() != null) {
                    event.setUserId(request.getUserId());
                }
            } catch (Exception e) {
                log.warn("Could not extract userId from expense event", e);
            }

            publishEvent("analytics-recalculation", event);

        } catch (Exception e) {
            log.error("Error publishing analytics recalculation event for transaction: {}",
                    transactionId, e);
            // Don't throw - this should not block the main ingestion pipeline
            // In production, would send to dead letter queue or alert monitoring system
        }
    }

    /**
     * Generic method to publish an event to a Kafka topic
     *
     * @param topic The Kafka topic name
     * @param event The event object to publish
     */
    private void publishEvent(String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            // Build message with headers for traceability
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader("correlation_id", UUID.randomUUID().toString())
                    .setHeader("source", "ingestion-pipeline")
                    .setHeader("timestamp", System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(message);

            log.info("Published event to topic: {} (type: {})", topic, event.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("Error publishing event to topic: {}", topic, e);
            // Optionally re-throw or handle based on business requirements
        }
    }

    /**
     * Publish a custom event to the specified topic
     * Useful for future integrations
     */
    public void publishCustomEvent(String topic, String eventType, String payload) {
        try {
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader("event_type", eventType)
                    .setHeader("correlation_id", UUID.randomUUID().toString())
                    .setHeader("source", "ingestion-pipeline")
                    .setHeader("timestamp", System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(message);

            log.info("Published custom event to topic: {}, type: {}", topic, eventType);

        } catch (Exception e) {
            log.error("Error publishing custom event to topic: {}", topic, e);
        }
    }
}

