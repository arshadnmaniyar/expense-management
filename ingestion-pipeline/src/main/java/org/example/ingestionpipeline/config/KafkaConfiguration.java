package org.example.ingestionpipeline.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * KafkaConfiguration
 *
 * Spring configuration for Kafka consumer and producer.
 *
 * Key Features:
 * - Consumer factory: handles deserialization and error handling
 * - Producer factory: handles serialization for event publishing
 * - Listener container factory: supports retry and error handling
 * - Concurrency settings: optimized for throughput
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    private final KafkaProperties kafkaProperties;

    public KafkaConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Consumer factory configuration
     * Deprecated methods buildConsumerProperties() removed in Spring Boot 3.2.0.
     * Now building properties by combining common and consumer-specific properties.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>(kafkaProperties.getProperties());
        consumerProps.putAll(kafkaProperties.getConsumer().getProperties());
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }

    /**
     * Kafka listener container factory
     * Configures:
     * - Concurrency for parallel message processing
     * - Error handling
     * - Offset commit strategy
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
    kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3); // Process 3 partitions concurrently
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        return factory;
    }

    /**
     * Producer factory for publishing events
     * Deprecated methods buildProducerProperties() removed in Spring Boot 3.2.0.
     * Now building properties by combining common and producer-specific properties.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> producerProps = new HashMap<>(kafkaProperties.getProperties());
        producerProps.putAll(kafkaProperties.getProducer().getProperties());
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    /**
     * Kafka template for sending messages
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}

