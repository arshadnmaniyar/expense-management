package org.example.ingestionpipeline.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.ingestionpipeline.service.EmbeddingGenerationService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * IngestionPipelineController
 *
 * REST endpoints for monitoring and health checks.
 */
@Slf4j
@RestController
@RequestMapping("/api/ingestion-pipeline")
public class IngestionPipelineController implements HealthIndicator {

    private final EmbeddingGenerationService embeddingGenerationService;

    public IngestionPipelineController(EmbeddingGenerationService embeddingGenerationService) {
        this.embeddingGenerationService = embeddingGenerationService;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ingestion-pipeline");
        response.put("embedding_service_healthy", embeddingGenerationService.healthCheck());
        return ResponseEntity.ok(response);
    }

    /**
     * Get metrics for embedding generation
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("embedding_metrics", embeddingGenerationService.getMetrics());
        response.put("embedding_model", embeddingGenerationService.getModelName());
        return ResponseEntity.ok(response);
    }

    /**
     * Reset metrics
     */
    @GetMapping("/metrics/reset")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        embeddingGenerationService.resetMetrics();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Metrics resetted");
        return ResponseEntity.ok(response);
    }

    /**
     * Spring Boot Actuator health indicator
     */
    @Override
    public Health health() {
        if (embeddingGenerationService.healthCheck()) {
            return Health.up()
                    .withDetail("embedding_service", "operational")
                    .withDetail("model", embeddingGenerationService.getModelName())
                    .build();
        } else {
            return Health.down()
                    .withDetail("embedding_service", "unreachable")
                    .build();
        }
    }
}

