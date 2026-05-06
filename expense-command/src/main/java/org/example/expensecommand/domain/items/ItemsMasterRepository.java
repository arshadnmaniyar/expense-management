package org.example.expensecommand.domain.items;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ItemsMasterRepository extends JpaRepository<ItemsMaster, UUID> {
    Optional<ItemsMaster> findByItemName(String itemName);
}
