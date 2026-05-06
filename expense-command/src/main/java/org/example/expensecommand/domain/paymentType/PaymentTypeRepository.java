package org.example.expensecommand.domain.paymentType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, UUID> {

    Optional<PaymentType> findByPaymentTypeCd(String paymentTypeCd);
}
