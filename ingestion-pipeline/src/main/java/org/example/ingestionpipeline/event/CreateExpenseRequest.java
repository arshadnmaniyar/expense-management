package org.example.ingestionpipeline.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


/**
 * CreateExpenseRequest - represents the expense details extracted from the event
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExpenseRequest {

    private UUID userId;
    private String idempotencyKey;
    private LocalDate purchaseDate;
    private List<ExpenseItem> items;
    private String store;
    private String paymentType;

    /**
     * Builds a semantic text representation for embedding
     * Combines multiple dimensions: store, items, categories, amounts
     * This provides rich context for semantic similarity search
     */
    public String buildEmbeddingText() {
        StringBuilder text = new StringBuilder();

        // Store information
        if (store != null && !store.isEmpty()) {
            text.append("Store: ").append(store).append(". ");
        }

        // Payment type
        if (paymentType != null && !paymentType.isEmpty()) {
            text.append("Payment method: ").append(paymentType).append(". ");
        }

        // Items and categories
        if (items != null && !items.isEmpty()) {
            text.append("Purchased items: ");
            for (ExpenseItem item : items) {
                text.append(item.getItemName());
                if (item.getCategory() != null) {
                    text.append(" (").append(item.getCategory());
                    if (item.getSubCategory() != null) {
                        text.append("-").append(item.getSubCategory());
                    }
                    text.append(")");
                }
                if (item.getAmount() != null) {
                    text.append(" $").append(item.getAmount());
                }
                text.append(", ");
            }
            text.setLength(text.length() - 2); // Remove trailing comma
            text.append(". ");
        }

        text.append("Purchase date: ").append(purchaseDate).append(".");

        return text.toString();
    }
}
