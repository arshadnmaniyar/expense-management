package org.example.ingestionpipeline.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ExpenseCreatedEvent
 *
 * Represents the event published when an expense is created in the expense-command service.
 * This event is consumed by the ingestion pipeline to generate embeddings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCreatedEvent {

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("requestBody")
    private String requestBody; // Contains serialized CreateExpenseRequestDto as JSON
}





