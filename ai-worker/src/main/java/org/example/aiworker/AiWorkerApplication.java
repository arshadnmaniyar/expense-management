package org.example.aiworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class AiWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiWorkerApplication.class, args);
    }

}
