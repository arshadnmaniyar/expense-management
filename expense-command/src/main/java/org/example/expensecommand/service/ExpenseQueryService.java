package org.example.expensecommand.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expensecommand.model.ExpenseItemSummary;
import org.example.expensecommand.model.ExpenseSearchResponse;
import org.example.expensecommand.model.ExpenseSummary;
import org.example.expensecommand.service.query.CursorPaginationHelper;
import org.example.expensecommand.service.query.ExpenseSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExpenseQueryService {

    @PersistenceContext
    private EntityManager entityManager;

    public ExpenseSearchResponse searchExpenses(LocalDate purchaseDate, LocalDate transactionDate,
                                                String paymentType, String itemName, String category,
                                                String store, int limit, String sort, String cursor) {

        log.info("Searching expenses with criteria: purchaseDate={}, transactionDate={}, paymentType={}, itemName={}, category={}, store={}, limit={}, sort={}, cursor={}",
                purchaseDate, transactionDate, paymentType, itemName, category, store, limit, sort, cursor);

        // Parse cursor to get offset
        int offset = CursorPaginationHelper.decodeOffset(cursor);

        // Parse and validate sort parameters
        String[] sortParts = ExpenseSearchQueryBuilder.parseSortParameter(sort);
        String sortField = sortParts[0];
        String sortOrder = sortParts[1];

        // Build dynamic query using builder pattern
        ExpenseSearchQueryBuilder queryBuilder = new ExpenseSearchQueryBuilder()
                .buildBaseQuery()
                .addPurchaseDateFilter(purchaseDate)
                .addTransactionDateFilter(transactionDate)
                .addPaymentTypeFilter(paymentType)
                .addItemNameFilter(itemName)
                .addCategoryFilter(category)
                .addStoreFilter(store)
                .addOrderBy(sortField, sortOrder);

        String jpql = queryBuilder.getQuery();
        Map<String, Object> params = queryBuilder.getParams();

        log.debug("Generated JPQL: {}", jpql);
        log.debug("Query parameters: {}", params);

        // Execute query with pagination
        Query query = entityManager.createQuery(jpql);
        params.forEach(query::setParameter);
        query.setFirstResult(offset);
        query.setMaxResults(limit + 1); // +1 to check if there are more

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        log.debug("Query returned {} results", results.size());

        // Build response
        List<ExpenseSummary> summaries = new ArrayList<>();
        Set<UUID> transactionIds = new HashSet<>();

        for (Object[] row : results) {
            if (transactionIds.size() >= limit) break;
            UUID transactionId = (UUID) row[0];
            if (transactionIds.add(transactionId)) {
                LocalDate tDate = (LocalDate) row[1];
                LocalDate pDate = (LocalDate) row[2];
                String storeName = (String) row[4];
                String paymentTypeCode = (String) row[5];

                ExpenseSummary summary = new ExpenseSummary();
                summary.setTransactionDate(tDate);
                summary.setPurchaseDate(pDate);
                summary.setStore(storeName);
                summary.setPaymentType(paymentTypeCode);

                // Fetch items for this transaction
                List<ExpenseItemSummary> items = getItemsForTransaction(transactionId);
                summary.setItems(items);

                summaries.add(summary);
            }
        }

        log.info("Returning {} expense records", summaries.size());

        String nextCursor = CursorPaginationHelper.encodeOffset(offset, results.size(), limit);

        ExpenseSearchResponse response = new ExpenseSearchResponse();
        response.setExpenses(summaries);
        response.setNextCursor(nextCursor);

        return response;
    }

    private List<ExpenseItemSummary> getItemsForTransaction(UUID transactionId) {
        // Correct relationship: ExpenseItem -> ItemsMaster -> Category
        String jpql = "SELECT im.itemName, c.categoryName, " +
                      "COALESCE(parent.categoryName, '') AS parentCategoryName, " +
                      "ei.amount, ei.quantity, " +
                      "COALESCE(im.activeIn, '') AS unit, ei.comments " +
                      "FROM Expense e " +
                      "JOIN ExpenseItem ei ON e.expenseId = ei.expenseId " +
                      "JOIN ItemsMaster im ON ei.itemMasterId = im.itemMasterId " +
                      "JOIN Category c ON im.categoryId = c.categoryId " +
                      "LEFT JOIN Category parent ON c.parentCategoryId = parent.categoryId " +
                      "WHERE e.transactionId = :transactionId " +
                      "ORDER BY ei.expenseItemId";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("transactionId", transactionId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream().map(row -> {
            ExpenseItemSummary item = new ExpenseItemSummary();
            item.setItemName((String) row[0]);

            // If there's a parent category (subcategory case), use parent name
            String parentCategoryName = (String) row[2];
            String categoryName = (String) row[1];
            if (parentCategoryName != null && !parentCategoryName.isEmpty()) {
                item.setCategory(parentCategoryName);
                item.setSubCategory(categoryName);
            } else {
                item.setCategory(categoryName);
                item.setSubCategory(null);
            }

            item.setAmount(((Number) row[3]).doubleValue());
            item.setQuantity(row[4].toString());
            item.setUnit((String) row[5]);
            item.setComments((String) row[6]);
            return item;
        }).collect(Collectors.toList());
    }
}
