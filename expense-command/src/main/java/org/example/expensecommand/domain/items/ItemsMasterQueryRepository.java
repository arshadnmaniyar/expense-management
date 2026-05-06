package org.example.expensecommand.domain.items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemsMasterQueryRepository extends JpaRepository<ItemsMaster, UUID> {

    @Query("""
    SELECT i
    FROM ItemsMaster i
    JOIN Category c ON i.categoryId = c.categoryId
    JOIN Category parent ON c.parentCategoryId = parent.categoryId
    WHERE i.itemName = :itemName
      AND c.categoryName = :subCategoryName
      AND parent.categoryName = :categoryName
""")
    Optional<ItemsMaster> findItemWithCategoryAndSubCategory(
            @Param("itemName") String itemName,
            @Param("categoryName") String categoryName,
            @Param("subCategoryName") String subCategoryName
    );

    @Query("""
    SELECT i
    FROM ItemsMaster i
    JOIN Category c ON i.categoryId = c.categoryId
    WHERE i.itemName = :itemName
      AND c.categoryName = :categoryName
      AND c.parentCategoryId IS NULL
""")
    Optional<ItemsMaster> findItemWithRootCategory(
            @Param("itemName") String itemName,
            @Param("categoryName") String categoryName
    );
}
