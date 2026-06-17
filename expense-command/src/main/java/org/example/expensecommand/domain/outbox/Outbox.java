package org.example.expensecommand.domain.outbox;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table (name = "outbox", schema = "expense_management")
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "outbox_id", nullable = false)
    private UUID outboxId;

    @Column (name = "event_type", nullable = false)
    private String eventType;

    @Column (name = "payload", nullable = false)
    private String payload;

    @Column (name = "processed", nullable = false, columnDefinition = "boolean default false")
    private boolean processed = false;

    // Constructors, getters, setters

    public Outbox() {}

    public Outbox(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public UUID getOutboxId() { return outboxId; }
    public void setOutboxId(UUID outboxId) { this.outboxId = outboxId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}
