package org.example.expensecommand.service;

import org.example.expensecommand.domain.category.Category;
import org.example.expensecommand.domain.category.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;

/**
 * Domain Service for Category Resolution
 * Responsible for resolving category hierarchies with case-insensitive lookup
 *
 * DDD Principle: Domain Service
 * - Encapsulates category resolution logic
 * - Handles hierarchical category lookups
 */
@Service
public class CategoryResolutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryResolutionService.class);

    private final CategoryRepository categoryRepository;

    public CategoryResolutionService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Resolve category ID for an expense item.
     * If a category or subcategory does not exist, it will be created.
     *
     * Logic:
     * - If both category and subCategory are provided: Find or create subcategory under category
     * - If only category is provided: Find or create root category
     *
     * @param categoryName Root category name (required)
     * @param subCategoryName Sub-category name (optional)
     * @return UUID of resolved category (subcategory ID if both provided, category ID otherwise)
     */
    @Transactional
    public UUID resolveCategoryId(String categoryName, String subCategoryName) {
        LOGGER.debug("Resolving category: name={}, subCategory={}", categoryName, subCategoryName);

        // Step 1: Find or create root category (case-insensitive)
        Optional<Category> rootCategoryOpt = categoryRepository.findRootCategoryByNameIgnoreCase(categoryName);
        Category rootCategory;

        if (rootCategoryOpt.isEmpty()) {
            rootCategory = new Category(UUID.randomUUID(), categoryName, null);
            categoryRepository.save(rootCategory);
            LOGGER.info("Created new root category: id={}, name={}", rootCategory.getCategoryId(), rootCategory.getCategoryName());
        } else {
            rootCategory = rootCategoryOpt.get();
            LOGGER.debug("Found root category: id={}, name={}", rootCategory.getCategoryId(), rootCategory.getCategoryName());
        }

        // Step 2: If subcategory is provided, find or create it under the root category
        if (subCategoryName != null && !subCategoryName.isBlank()) {
            Optional<Category> subCategoryOpt = categoryRepository.findSubCategoryByNameIgnoreCase(
                    subCategoryName,
                    rootCategory.getCategoryId()
            );
            Category subCategory;

            if (subCategoryOpt.isEmpty()) {
                subCategory = new Category(UUID.randomUUID(), subCategoryName, rootCategory.getCategoryId());
                categoryRepository.save(subCategory);
                LOGGER.info("Created new subcategory: id={}, name={}, parentId={}",
                        subCategory.getCategoryId(), subCategory.getCategoryName(), subCategory.getParentCategoryId());
            } else {
                subCategory = subCategoryOpt.get();
                LOGGER.debug("Found subcategory: id={}, name={}, parentId={}",
                        subCategory.getCategoryId(), subCategory.getCategoryName(), subCategory.getParentCategoryId());
            }
            return subCategory.getCategoryId();
        }

        // Step 3: Return root category ID
        LOGGER.debug("Returning root category ID: {}", rootCategory.getCategoryId());
        return rootCategory.getCategoryId();
    }
}
