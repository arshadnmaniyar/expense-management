package org.example.insightquery.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String insightText;

    private LocalDateTime generatedAt;

    // Constructors, getters, setters

    public Insight() {}

    public Insight(String userId, String insightText, LocalDateTime generatedAt) {
        this.userId = userId;
        this.insightText = insightText;
        this.generatedAt = generatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getInsightText() { return insightText; }
    public void setInsightText(String insightText) { this.insightText = insightText; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
