package org.example.expensecommand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class CreateExpenseRequest {

    @NotNull
//    @JsonProperty("idempotency-key")
    private UUID idempotencyKey;
    @NotNull
    private UUID userId;
    @NotNull
    private LocalDate date;

    // Default constructor
    public CreateExpenseRequest() {}

    // getters and setters

    public UUID getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(UUID idempotencyKey) {this.idempotencyKey = idempotencyKey;}

    public UUID  getUserId() { return userId; }
    public void setUserId(UUID  userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}