package org.example.ingestionpipeline.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ExpenseItem - represents individual items purchased in an expense
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItem {

    private String itemName;
    private String category;
    private String subCategory;
    private Double amount;
    private String quantity;
    private String unit;
    private String comments;

//    @JsonIgnore
//    public String getPrimaryCategory() {
//        return category != null ? category : "Other";
//    }
}