package org.example.expensecommand.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find a root category (parent_category_id IS NULL) by name (case-insensitive)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) = LOWER(:categoryName) AND c.parentCategoryId IS NULL")
    Optional<Category> findRootCategoryByNameIgnoreCase(@Param("categoryName") String categoryName);

    /**
     * Find a subcategory by name (case-insensitive) under a specific parent category
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) = LOWER(:subCategoryName) AND c.parentCategoryId = :parentCategoryId")
    Optional<Category> findSubCategoryByNameIgnoreCase(@Param("subCategoryName") String subCategoryName, @Param("parentCategoryId") UUID parentCategoryId);
}
