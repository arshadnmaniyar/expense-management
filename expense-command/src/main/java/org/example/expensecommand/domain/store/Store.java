package org.example.expensecommand.domain.store;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "store", schema = "expense_management")
public class Store {

    @Id
    @GeneratedValue
    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_ts", nullable = false)
    private LocalDateTime updatedTs;

    public Store() {}

    public Store(UUID storeId, String storeName) {
        this.storeId = storeId;
        this.storeName = storeName;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdTs == null) createdTs = now;
        updatedTs = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedTs = LocalDateTime.now();
    }

    // getters and setters
    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public LocalDateTime getCreatedTs() { return createdTs; }
    public void setCreatedTs(LocalDateTime createdTs) { this.createdTs = createdTs; }

    public LocalDateTime getUpdatedTs() { return updatedTs; }
    public void setUpdatedTs(LocalDateTime updatedTs) { this.updatedTs = updatedTs; }
}
