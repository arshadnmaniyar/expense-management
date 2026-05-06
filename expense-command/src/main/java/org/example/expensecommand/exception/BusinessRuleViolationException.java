package org.example.expensecommand.exception;

public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) { super(message); }
}
