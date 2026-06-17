package org.example.expensecommand.service;

import org.example.expensecommand.domain.outbox.Outbox;
import org.example.expensecommand.domain.outbox.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxProcessor(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 60000) // every 1 minutes
    @Transactional
    public void processOutbox() {
        LOGGER.info("Entering processOutbox: Checking for unprocessed outbox messages.");
        var outboxes = outboxRepository.findByProcessedFalse();
        
        if (outboxes.isEmpty()) {
            LOGGER.debug("No unprocessed outbox messages found.");
            LOGGER.info("Exiting processOutbox: No messages to process.");
            return;
        }

        LOGGER.info("Found {} unprocessed outbox messages.", outboxes.size());

        for (Outbox outbox : outboxes) {
            LOGGER.debug("Processing outbox message with ID: {} and EventType: {}", outbox.getOutboxId(), outbox.getEventType());
            try {
                kafkaTemplate.send("expense-events", outbox.getEventType(), outbox.getPayload());
                outbox.setProcessed(true);
                outboxRepository.save(outbox);
                LOGGER.debug("Successfully processed and marked as sent outbox message with ID: {}", outbox.getOutboxId());
            } catch (Exception e) {
                LOGGER.error("Failed to process outbox message with ID: {}. Error: {}", outbox.getOutboxId(), e.getMessage(), e);
                // Depending on requirements, you might want to mark it as failed or retry later
            }
        }
        LOGGER.info("Exiting processOutbox: Finished processing outbox messages.");
    }
}
