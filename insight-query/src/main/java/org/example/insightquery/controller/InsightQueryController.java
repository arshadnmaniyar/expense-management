package org.example.insightquery.controller;

import org.example.insightquery.service.InsightQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/insights")
public class InsightQueryController {

    private final InsightQueryService insightQueryService;

    public InsightQueryController(InsightQueryService insightQueryService) {
        this.insightQueryService = insightQueryService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<InsightResponse> getInsights(@PathVariable String userId) {
        return insightQueryService.getInsights(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    public static class InsightResponse {
        private String insightText;
        private String lastRefreshed;

        public InsightResponse(String insightText, String lastRefreshed) {
            this.insightText = insightText;
            this.lastRefreshed = lastRefreshed;
        }

        public String getInsightText() { return insightText; }
        public void setInsightText(String insightText) { this.insightText = insightText; }

        public String getLastRefreshed() { return lastRefreshed; }
        public void setLastRefreshed(String lastRefreshed) { this.lastRefreshed = lastRefreshed; }
    }
}
