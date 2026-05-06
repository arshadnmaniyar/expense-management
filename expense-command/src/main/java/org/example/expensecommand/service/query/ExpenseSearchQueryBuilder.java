package org.example.expensecommand.service.query;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for building dynamic JPQL queries for expense search.
 * Handles the correct entity relationships and WHERE clause construction.
 *
 * Entity Relationships:
 * - Transaction (t) -> Expense (e) -> ExpenseItem (ei)
 * - ExpenseItem (ei) -> ItemsMaster (im) -> Category (c)
 * - Expense (e) -> Store (s)
 * - Expense (e) -> PaymentType (pt)
 */
@Slf4j
public class ExpenseSearchQueryBuilder {

    private final StringBuilder jpql = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();

    public ExpenseSearchQueryBuilder buildBaseQuery() {
        jpql.append("SELECT DISTINCT t.transactionId, t.transactionDate, e.purchaseDate, s.storeId, s.storeName, pt.paymentTypeCd ");
        jpql.append("FROM Transaction t ");
        jpql.append("LEFT JOIN Expense e ON t.transactionId = e.transactionId ");
        jpql.append("LEFT JOIN ExpenseItem ei ON e.expenseId = ei.expenseId ");
        jpql.append("LEFT JOIN ItemsMaster im ON ei.itemMasterId = im.itemMasterId ");
        jpql.append("LEFT JOIN Category c ON im.categoryId = c.categoryId ");
        jpql.append("LEFT JOIN Store s ON e.storeId = s.storeId ");
        jpql.append("LEFT JOIN PaymentType pt ON e.paymentTypeId = pt.paymentTypeId ");
        jpql.append("WHERE 1=1 ");
        return this;
    }

    public ExpenseSearchQueryBuilder addPurchaseDateFilter(LocalDate purchaseDate) {
        if (purchaseDate != null) {
            jpql.append("AND e.purchaseDate = :purchaseDate ");
            params.put("purchaseDate", purchaseDate);
            log.debug("Added purchaseDate filter: {}", purchaseDate);
        }
        return this;
    }

    public ExpenseSearchQueryBuilder addTransactionDateFilter(LocalDate transactionDate) {
        if (transactionDate != null) {
            jpql.append("AND t.transactionDate = :transactionDate ");
            params.put("transactionDate", transactionDate);
            log.debug("Added transactionDate filter: {}", transactionDate);
        }
        return this;
    }

    public ExpenseSearchQueryBuilder addPaymentTypeFilter(String paymentType) {
        if (paymentType != null && !paymentType.isEmpty()) {
            jpql.append("AND pt.paymentTypeCd = :paymentType ");
            params.put("paymentType", paymentType);
            log.debug("Added paymentType filter: {}", paymentType);
        }
        return this;
    }

    public ExpenseSearchQueryBuilder addItemNameFilter(String itemName) {
        if (itemName != null && !itemName.isEmpty()) {
            // itemName is on ItemsMaster, not ExpenseItem
            jpql.append("AND LOWER(im.itemName) LIKE LOWER(:itemName) ");
            params.put("itemName", "%" + itemName + "%");
            log.debug("Added itemName filter: {}", itemName);
        }
        return this;
    }

    public ExpenseSearchQueryBuilder addCategoryFilter(String category) {
        if (category != null && !category.isEmpty()) {
            // category is on Category entity, searched by name
            jpql.append("AND LOWER(c.categoryName) LIKE LOWER(:category) ");
            params.put("category", "%" + category + "%");
            log.debug("Added category filter: {}", category);
        }
        return this;
    }

    public ExpenseSearchQueryBuilder addStoreFilter(String store) {
        if (store != null && !store.isEmpty()) {
            jpql.append("AND LOWER(s.storeName) LIKE LOWER(:store) ");
            params.put("store", "%" + store + "%");
            log.debug("Added store filter: {}", store);
        }
        return this;
    }

    public ExpenseSearchQueryBuilder addOrderBy(String sortField, String sortOrder) {
        jpql.append("ORDER BY ").append(sortField).append(" ").append(sortOrder);
        jpql.append(", t.transactionId DESC"); // secondary sort for stability
        log.debug("Added ORDER BY: {} {}", sortField, sortOrder);
        return this;
    }

    public String getQuery() {
        return jpql.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Maps user-provided sort field name to actual JPQL field path.
     * Defaults to transactionDate DESC if invalid.
     */
    public static String mapSortField(String field) {
        if (field == null || field.isEmpty()) {
            return "t.transactionDate";
        }

        return switch (field.toLowerCase()) {
            case "purchasedate" -> "e.purchaseDate";
            case "store" -> "s.storeName";
            case "paymenttype" -> "pt.paymentTypeCd";
            case "transactiondate" -> "t.transactionDate";
            default -> "t.transactionDate";
        };
    }

    /**
     * Parses sort parameter in format "field:order" (e.g., "transactionDate:desc")
     * Returns an array [sortField, sortOrder]
     */
    public static String[] parseSortParameter(String sort) {
        String sortField = "t.transactionDate";
        String sortOrder = "DESC";

        if (sort != null && !sort.isEmpty() && sort.contains(":")) {
            String[] parts = sort.split(":");
            sortField = mapSortField(parts[0]);
            sortOrder = "ASC".equalsIgnoreCase(parts[1]) ? "ASC" : "DESC";
        }

        return new String[]{sortField, sortOrder};
    }
}
