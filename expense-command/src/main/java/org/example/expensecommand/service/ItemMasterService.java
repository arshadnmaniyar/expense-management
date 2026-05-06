package org.example.expensecommand.service;

import org.example.expensecommand.service.CategoryResolutionService;
import org.example.expensecommand.domain.items.ItemsMaster;
import org.example.expensecommand.domain.items.ItemsMasterRepository;
import org.example.expensecommand.domain.items.ItemsMasterQueryRepository;
import org.example.expensecommand.exception.ItemCategoryConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ItemMasterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemMasterService.class);

    private final ItemsMasterRepository itemsMasterRepository;
    private final ItemsMasterQueryRepository itemsMasterQueryRepository;
    private final CategoryResolutionService categoryResolutionService;

    public ItemMasterService(ItemsMasterRepository itemsMasterRepository,
                             ItemsMasterQueryRepository itemsMasterQueryRepository,
                             CategoryResolutionService categoryResolutionService) {
        this.itemsMasterRepository = itemsMasterRepository;
        this.itemsMasterQueryRepository = itemsMasterQueryRepository;
        this.categoryResolutionService = categoryResolutionService;
    }

    @Transactional
    public ItemsMaster getOrCreateItemMaster(String itemName, String category, String subCategory) {
        // Step 1: Try to find existing item with the exact category/subcategory combination in one trip
        Optional<ItemsMaster> existingItem;
        if (subCategory != null && !subCategory.isBlank()) {
            existingItem = itemsMasterQueryRepository.findItemWithCategoryAndSubCategory(itemName, category, subCategory);
        } else {
            existingItem = itemsMasterQueryRepository.findItemWithRootCategory(itemName, category);
        }

        if (existingItem.isPresent()) {
            LOGGER.debug("Found existing ItemMaster for itemName '{}' with provided category/subcategory", itemName);
            return existingItem.get();
        }

        // Step 2: If not found by exact match, check if the itemName exists at all (Conflict Check)
        // This is to ensure that if "Milk" exists under "Dairy", we don't create "Milk" under "Groceries"
        Optional<ItemsMaster> itemWithSameNameButDifferentCategory = itemsMasterRepository.findByItemName(itemName);
        if (itemWithSameNameButDifferentCategory.isPresent()) {
            ItemsMaster conflictingItem = itemWithSameNameButDifferentCategory.get();
            LOGGER.error("Conflict: Item '{}' already exists with category ID '{}', but a different category combination ('{}', '{}') was provided.",
                    itemName, conflictingItem.getCategoryId(), category, subCategory);
            throw new ItemCategoryConflictException(
                    String.format("Item '%s' is already associated with a different category. Existing category ID: %s",
                            itemName, conflictingItem.getCategoryId()));
        }

        // Step 3: ItemMaster does not exist at all, create a new one
        // Resolve or create categories and create new ItemMaster
        UUID resolvedCategoryId = categoryResolutionService.resolveCategoryId(category, subCategory);
        ItemsMaster newItemMaster = new ItemsMaster(UUID.randomUUID(), itemName, resolvedCategoryId, "Y");
        itemsMasterRepository.save(newItemMaster);
        LOGGER.info("Created new ItemMaster for itemName '{}' with category ID '{}'", itemName, resolvedCategoryId);
        return newItemMaster;
    }
}
