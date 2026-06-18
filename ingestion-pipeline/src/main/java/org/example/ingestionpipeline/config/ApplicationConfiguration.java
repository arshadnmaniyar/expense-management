package org.example.ingestionpipeline.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ApplicationConfiguration
 *
 * General Spring configuration for the ingestion pipeline.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Jackson ObjectMapper bean for JSON serialization/deserialization
     *
     * Configured to handle Java 8 date/time types (LocalDate, LocalDateTime, Instant)
     * by registering the JavaTimeModule.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register JavaTimeModule to handle java.time.* types
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}

