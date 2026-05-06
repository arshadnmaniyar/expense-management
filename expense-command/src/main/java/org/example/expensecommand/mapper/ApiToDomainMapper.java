package org.example.expensecommand.mapper;

import org.example.expensecommand.dto.CreateExpenseRequestDto;
import org.example.expensecommand.dto.ExpenseItemDto;
// import org.example.expensecommand.model.CreateExpenseRequest;
// import org.example.expensecommand.model.ExpenseItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// MapStruct mapper - will be enabled after OpenAPI generation
// @Mapper(componentModel = "spring", imports = {UUID.class, BigDecimal.class})
@Component
public class ApiToDomainMapper {

    // Temporary implementation without MapStruct annotations
    // After OpenAPI generation, this should be an interface with @Mapper annotation

    public CreateExpenseRequestDto toDto(Object createExpenseRequest) {
        // This is a placeholder implementation
        // After OpenAPI generation, MapStruct will handle the conversion
        if (createExpenseRequest == null) {
            return null;
        }

        // Reflection-based fallback for now
        CreateExpenseRequestDto dto = new CreateExpenseRequestDto();
        try {
            // Attempt to populate using reflection
            java.lang.reflect.Method[] methods = createExpenseRequest.getClass().getMethods();
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals("getIdempotencyKey")) {
                    dto.setIdempotencyKey((String) method.invoke(createExpenseRequest));
                } else if (method.getName().equals("getUserId")) {
                    dto.setUserId((UUID) method.invoke(createExpenseRequest));
                } else if (method.getName().equals("getPurchaseDate")) {
                    dto.setPurchaseDate((java.time.LocalDate) method.invoke(createExpenseRequest));
                } else if (method.getName().equals("getItems")) {
                    List<?> items = (List<?>) method.invoke(createExpenseRequest);
                    if (items != null) {
                        List<ExpenseItemDto> dtoItems = items.stream()
                            .map(this::toDomainItem)
                            .collect(Collectors.toList());
                        dto.setItems(dtoItems);
                    }
                } else if (method.getName().equals("getStore")) {
                    dto.setStore((String) method.invoke(createExpenseRequest));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map CreateExpenseRequest", e);
        }
        return dto;
    }

    public List<ExpenseItemDto> toDomainItems(List<?> items) {
        if (items == null) return null;
        return items.stream()
                .map(this::toDomainItem)
                .collect(Collectors.toList());
    }

    public ExpenseItemDto toDomainItem(Object item) {
        if (item == null) return null;

        ExpenseItemDto dto = new ExpenseItemDto();
        try {
            java.lang.reflect.Method[] methods = item.getClass().getMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName();
                if (methodName.equals("getItemName")) {
                    dto.setItemName((String) method.invoke(item));
                } else if (methodName.equals("getCategory")) {
                    dto.setCategory((String) method.invoke(item));
                } else if (methodName.equals("getSubCategory")) {
                    dto.setSubCategory((String) method.invoke(item));
                } else if (methodName.equals("getAmount")) {
                    Object amount = method.invoke(item);
                    if (amount instanceof Double) {
                        dto.setAmount((Double) amount);
                    } else if (amount instanceof BigDecimal) {
                        dto.setAmount(((BigDecimal) amount).doubleValue());
                    }
                } else if (methodName.equals("getQuantity")) {
                    dto.setQuantity((String) method.invoke(item));
                } else if (methodName.equals("getUnit")) {
                    dto.setUnit((String) method.invoke(item));
                } else if (methodName.equals("getComments")) {
                    dto.setComments((String) method.invoke(item));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ExpenseItem", e);
        }
        return dto;
    }

    public BigDecimal parseQuantity(String q) {
        if (q == null || q.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(q.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid quantity: " + q, ex);
        }
    }
}