package org.example.expensecommand.service;

import org.example.expensecommand.domain.paymentType.PaymentType;
import org.example.expensecommand.domain.paymentType.PaymentTypeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
//@RequiredArgsConstructor
public class PaymentService {
    private final PaymentTypeRepository repository;

    public PaymentService(PaymentTypeRepository repository) {
        this.repository = repository;
    }

    public UUID getPaymentTypeId(String paymentTypeCd) {

        return repository.findByPaymentTypeCd(paymentTypeCd)
                .map(PaymentType::getPaymentTypeId)
                .orElseGet(() -> {
                    throw new IllegalArgumentException("Invalid payment type code: " + paymentTypeCd);
                });
    }
}
