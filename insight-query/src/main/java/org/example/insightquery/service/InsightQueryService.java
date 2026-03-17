package org.example.insightquery.service;

import org.example.insightquery.controller.InsightQueryController;
import org.example.insightquery.domain.Insight;
import org.example.insightquery.domain.InsightRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class InsightQueryService {

    private final InsightRepository insightRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public InsightQueryService(InsightRepository insightRepository, RedisTemplate<String, String> redisTemplate) {
        this.insightRepository = insightRepository;
        this.redisTemplate = redisTemplate;
    }

    public Optional<InsightQueryController.InsightResponse> getInsights(String userId) {
        String cacheKey = "insight:" + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // Assume cached is JSON or something, but for simplicity, assume it's the text
            // In real, cache the response
            return Optional.of(new InsightQueryController.InsightResponse(cached, "cached"));
        }

        Optional<Insight> insightOpt = insightRepository.findByUserId(userId);
        if (insightOpt.isPresent()) {
            Insight insight = insightOpt.get();
            String lastRefreshed = insight.getGeneratedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            InsightQueryController.InsightResponse response = new InsightQueryController.InsightResponse(insight.getInsightText(), lastRefreshed);
            redisTemplate.opsForValue().set(cacheKey, insight.getInsightText(), Duration.ofMinutes(10)); // cache for 10 min
            return Optional.of(response);
        }
        return Optional.empty();
    }
}
