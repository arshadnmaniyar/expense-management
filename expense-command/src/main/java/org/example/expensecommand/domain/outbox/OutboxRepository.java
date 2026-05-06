package org.example.expensecommand.domain.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findByProcessedFalse();
}
