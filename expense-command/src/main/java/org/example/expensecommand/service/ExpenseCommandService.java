package org.example.expensecommand.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expensecommand.domain.Expense;
import org.example.expensecommand.domain.ExpenseRepository;
import org.example.expensecommand.domain.Outbox;
import org.example.expensecommand.domain.OutboxRepository;
import org.example.expensecommand.event.ExpenseCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ExpenseCommandService {

    private final ExpenseRepository expenseRepository;
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ExpenseCommandService(ExpenseRepository expenseRepository, OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.expenseRepository = expenseRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Expense createExpense(String userId, BigDecimal amount, String description, String category, LocalDate date) {
        Expense expense = new Expense(userId, amount, description, category, date);
        expense = expenseRepository.save(expense);

        ExpenseCreatedEvent event = new ExpenseCreatedEvent(expense.getId(), userId, amount, description, category, date);
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = new Outbox("ExpenseCreated", payload);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        return expense;
    }
}
