package org.example.insightquery.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InsightRepository extends JpaRepository<Insight, Long> {
    Optional<Insight> findByUserId(String userId);
}
