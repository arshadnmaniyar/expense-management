
package org.example.ingestionpipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.ingestionpipeline.event.ExpenseCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

/** * ExpenseEventConsumer * * Kafka consumer for the "expense-events" topic. * * Listens for ExpenseCreatedEvent messages and triggers the ingestion pipeline * to generate embeddings. * * Error Handling Strategy: * - RetryableTopic: automatically retries failed messages with exponential backoff * - Default retry pattern: 3 attempts with 1s, 2s, 4s delays * - DLT (Dead Letter Topic): after all retries fail, message goes to DLT for manual investigation * - Error logging: captures trace ID and partition for debugging * * Best Practices: * - Idempotent processing: service ensures duplicate events don't cause issues * - Distributed tracing: captures Kafka headers for correlation * - Graceful degradation: continues processing other events even if one fails * - Observability: detailed logging for monitoring and alerting */
@Slf4j
@Service
public class ExpenseEventConsumer {

    private final ExpenseIngestionService expenseIngestionService;
    private final ObjectMapper objectMapper;
    private final EventPublisherService eventPublisherService;

    public ExpenseEventConsumer(
            ExpenseIngestionService expenseIngestionService,
            ObjectMapper objectMapper,
            EventPublisherService eventPublisherService) {

        this.expenseIngestionService = expenseIngestionService;
        this.objectMapper = objectMapper;
        this.eventPublisherService = eventPublisherService;
    }

    /**     * Consume ExpenseCreatedEvent from Kafka topic     *     * @param message The event payload (JSON string)     * @param partition Kafka partition     * @param offset Kafka offset     * @param traceId Distributed trace ID from headers     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 5000
            ),
            include = {Exception.class},
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(
            topics = "expense-created",
            groupId = "ingestion-pipeline",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeExpenseCreatedEvent(
            @Payload String message,
            @Header(name = KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(name = KafkaHeaders.OFFSET) long offset,
            @Header(name = "trace_id", required = false) String traceId) {

        try {
            // Log for traceability
            if (traceId == null) {
                traceId = java.util.UUID.randomUUID().toString();
            }
            log.info("[{}] Processing expense event from partition: {}, offset: {}",
                    traceId, partition, offset);

            // Parse the event
            ExpenseCreatedEvent event = objectMapper.readValue(message, ExpenseCreatedEvent.class);

            // Process the event through the ingestion pipeline
            String embeddingId = expenseIngestionService.processExpenseEvent(event);

            // Publish analytics recalculation event
            if (embeddingId != null) {
                eventPublisherService.publishAnalyticsRecalculationEvent(
                        event.getTransactionId(),
                        embeddingId,
                        event
                );
                log.info("[{}] Successfully processed expense event. Generated embedding: {}",
                        traceId, embeddingId);
            } else {
                log.warn("[{}] Duplicate expense event (already processed): transactionId={}",
                        traceId, event.getTransactionId());
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid expense event received: {}", message, e);
            // Don't retry on validation errors - these won't be fixed by retrying
            throw new org.springframework.kafka.listener.ListenerExecutionFailedException(
                    "Invalid event format - not retrying", e);

        } catch (IllegalStateException e) {
            log.error("Error processing expense event (will retry): {}", message, e);
            // Retry on state errors - these might be transient
            throw new RuntimeException("Processing error - will retry", e);

        } catch (Exception e) {
            log.error("Unexpected error processing expense event: {}", message, e);
            throw new RuntimeException("Unexpected error - will retry", e);
        }
    }

    /**     * Handles messages that failed all retry attempts     * These messages are sent to the Dead Letter Topic for manual investigation     */
    @KafkaListener(
            topics = "expense-events-dlt",
            groupId = "ingestion-pipeline-dlt",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeadLetterMessage(
            @Payload String message,
            @Header(name = KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(name = KafkaHeaders.OFFSET) long offset) {

        log.error("Message sent to DLT - manual investigation required. " +
                "Partition: {}, Offset: {}, Message: {}", partition, offset, message);

        // TODO: In production, send alert to monitoring/alerting system
        // TODO: Optionally persist to database for manual review UI
    }
}