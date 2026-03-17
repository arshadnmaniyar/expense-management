package org.example.expensecommand.service;

import org.example.expensecommand.domain.Outbox;
import org.example.expensecommand.domain.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxProcessor(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000) // every 5 seconds
    @Transactional
    public void processOutbox() {
        var outboxes = outboxRepository.findByProcessedFalse();
        for (Outbox outbox : outboxes) {
            try {
                kafkaTemplate.send("expense-events", outbox.getEventType(), outbox.getPayload());
                outbox.setProcessed(true);
                outboxRepository.save(outbox);
            } catch (Exception e) {
                // log error, perhaps retry later
            }
        }
    }
}
