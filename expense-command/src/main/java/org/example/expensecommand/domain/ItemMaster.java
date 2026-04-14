package org.example.expensecommand.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items_master")
public class ItemMaster {

    @Id
    @Column(name = "item_master_id", nullable = false)
    private UUID itemMasterId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "active_in")
    private String activeIn;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_ts", nullable = false)
    private LocalDateTime updatedTs;

    public ItemMaster() {}

    public ItemMaster(UUID itemMasterId, String itemName, UUID categoryId, String activeIn) {
        this.itemMasterId = itemMasterId;
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.activeIn = activeIn;
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
    public UUID getItemMasterId() { return itemMasterId; }
    public void setItemMasterId(UUID itemMasterId) { this.itemMasterId = itemMasterId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getActiveIn() { return activeIn; }
    public void setActiveIn(String activeIn) { this.activeIn = activeIn; }

    public LocalDateTime getCreatedTs() { return createdTs; }
    public void setCreatedTs(LocalDateTime createdTs) { this.createdTs = createdTs; }

    public LocalDateTime getUpdatedTs() { return updatedTs; }
    public void setUpdatedTs(LocalDateTime updatedTs) { this.updatedTs = updatedTs; }
}
