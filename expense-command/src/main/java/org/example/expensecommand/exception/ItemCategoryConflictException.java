package org.example.expensecommand.exception;

public class ItemCategoryConflictException extends RuntimeException {
    public ItemCategoryConflictException(String message) {
        super(message);
    }
}
