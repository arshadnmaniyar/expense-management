package org.example.expensecommand.domain.category;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "category", schema = "expense_management")
public class Category {

    @Id
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "parent_category_id")
    private UUID parentCategoryId;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_ts", nullable = false)
    private LocalDateTime updatedTs;

    public Category() {}

    public Category(UUID categoryId, String categoryName, UUID parentCategoryId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
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
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public UUID getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(UUID parentCategoryId) { this.parentCategoryId = parentCategoryId; }

    public LocalDateTime getCreatedTs() { return createdTs; }
    public void setCreatedTs(LocalDateTime createdTs) { this.createdTs = createdTs; }

    public LocalDateTime getUpdatedTs() { return updatedTs; }
    public void setUpdatedTs(LocalDateTime updatedTs) { this.updatedTs = updatedTs; }
}
