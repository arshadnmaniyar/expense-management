package org.example.aiworker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aiworker.domain.Insight;
import org.example.aiworker.domain.InsightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiWorkerService {

    private static final Logger logger = LoggerFactory.getLogger(AiWorkerService.class);

    private final InsightRepository insightRepository;
    private final OllamaChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AiWorkerService(InsightRepository insightRepository, OllamaChatModel chatModel, ObjectMapper objectMapper) {
        this.insightRepository = insightRepository;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "expense-events", groupId = "ai-worker")
    public void handleExpenseCreated(String message) {
        try {
            // Assume the message is the payload of ExpenseCreatedEvent
            ExpenseCreatedEvent event = objectMapper.readValue(message, ExpenseCreatedEvent.class);
            logger.info("Received event for expense: {}", event.getExpenseId());

            // Generate insight using LLM
            String prompt = "Generate a financial insight based on this expense: " + event.getDescription() + " for amount " + event.getAmount() + " in category " + event.getCategory();
            String insightText = chatModel.call(prompt);

            // Update or insert insight
            Insight insight = insightRepository.findByUserId(event.getUserId())
                .orElse(new Insight(event.getUserId(), "", LocalDateTime.now()));
            insight.setInsightText(insightText);
            insight.setGeneratedAt(LocalDateTime.now());
            insightRepository.save(insight);

        } catch (Exception e) {
            logger.error("Error processing event", e);
        }
    }

    public static class ExpenseCreatedEvent {
        private Long expenseId;
        private String userId;
        private String description;
        private String category;
        // other fields if needed

        // getters and setters
        public Long getExpenseId() { return expenseId; }
        public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
