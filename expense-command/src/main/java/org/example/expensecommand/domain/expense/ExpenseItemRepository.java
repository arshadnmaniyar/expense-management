package org.example.expensecommand.domain.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, UUID> {
}
