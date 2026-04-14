package org.example.expensecommand.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expensecommand.domain.*;
import org.example.expensecommand.domain.TransactionType;
import org.example.expensecommand.dto.CreateExpenseRequest;
import org.example.expensecommand.event.ExpenseCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ExpenseCommandService {

    private final TransactionRepository transactionRepository;
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ExpenseCommandService(TransactionRepository transactionRepository, OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Transaction createExpense(CreateExpenseRequest request) {
        Transaction transaction = new Transaction(UUID.randomUUID(), request.getIdempotencyKey(), request.getDate(), TransactionType.EXPENSE, request.getUserId());
        transaction = transactionRepository.save(transaction);

        ExpenseCreatedEvent event = new ExpenseCreatedEvent(transaction.getTransactionId().toString(), request.getUserId(), request.getDate());
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = new Outbox("ExpenseCreated", payload);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        return transaction;
    }
}
