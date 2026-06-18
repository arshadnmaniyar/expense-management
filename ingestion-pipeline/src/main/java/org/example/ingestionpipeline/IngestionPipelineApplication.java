package org.example.ingestionpipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * IngestionPipelineApplication
 *
 * Main entry point for the ingestion pipeline microservice.
 *
 * Responsibilities:
 * - Consumes ExpenseCreatedEvent from Kafka
 * - Generates embeddings for expense data
 * - Stores embeddings in PostgreSQL with pgvector
 * - Publishes AnalyticsRecalculationEvent for downstream processing
 *
 * Configuration:
 * - @EnableKafka: Enables Kafka consumer auto-configuration
 * - @EnableAsync: Enables asynchronous method execution (optional for future use)
 *
 * Architecture:
 * - Part of microservices architecture
 * - Event-driven communication via Kafka
 * - Decoupled from analytics service (out of scope)
 * - Supports future integration with AI models for semantic search
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
public class IngestionPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestionPipelineApplication.class, args);
    }
}

