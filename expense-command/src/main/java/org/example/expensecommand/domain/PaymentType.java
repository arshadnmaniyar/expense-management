package org.example.expensecommand.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_type", schema = "expense_management", uniqueConstraints = {
        @UniqueConstraint(name = "payment_type_payment_type_cd_key", columnNames = "payment_type_cd")
})
public class PaymentType {

    @Id
    @Column(name = "payment_type_id", nullable = false)
    private UUID paymentTypeId;

    @Column(name = "payment_type_cd", nullable = false, unique = true)
    private String paymentTypeCd;

    @Column(name = "payment_type_desc")
    private String paymentTypeDesc;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTs;

    @Column(name = "updated_ts", nullable = false)
    private LocalDateTime updatedTs;

    public PaymentType() {}

    public PaymentType(UUID paymentTypeId, String paymentTypeCd, String paymentTypeDesc) {
        this.paymentTypeId = paymentTypeId;
        this.paymentTypeCd = paymentTypeCd;
        this.paymentTypeDesc = paymentTypeDesc;
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
    public UUID getPaymentTypeId() { return paymentTypeId; }
    public void setPaymentTypeId(UUID paymentTypeId) { this.paymentTypeId = paymentTypeId; }

    public String getPaymentTypeCd() { return paymentTypeCd; }
    public void setPaymentTypeCd(String paymentTypeCd) { this.paymentTypeCd = paymentTypeCd; }

    public String getPaymentTypeDesc() { return paymentTypeDesc; }
    public void setPaymentTypeDesc(String paymentTypeDesc) { this.paymentTypeDesc = paymentTypeDesc; }

    public LocalDateTime getCreatedTs() { return createdTs; }
    public void setCreatedTs(LocalDateTime createdTs) { this.createdTs = createdTs; }

    public LocalDateTime getUpdatedTs() { return updatedTs; }
    public void setUpdatedTs(LocalDateTime updatedTs) { this.updatedTs = updatedTs; }
}
