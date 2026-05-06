package org.example.expensecommand.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;
import jakarta.annotation.Generated;

/**
 * ExpenseItemSummary
 */

@Schema(name = "ExpenseItemSummary", description = "")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class ExpenseItemSummary {

  private String itemName;

  private String category;

  private String subCategory;

  private Double amount;

  private String quantity;

  private String unit;

  private String comments;

  public ExpenseItemSummary() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ExpenseItemSummary(String itemName, String category, Double amount, String quantity) {
    this.itemName = itemName;
    this.category = category;
    this.amount = amount;
    this.quantity = quantity;
  }

  public ExpenseItemSummary itemName(String itemName) {
    this.itemName = itemName;
    return this;
  }

  /**
   * Get itemName
   * @return itemName
  */

  @Schema(name = "itemName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemName")
  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public ExpenseItemSummary category(String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  */

  @Schema(name = "category", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public ExpenseItemSummary subCategory(String subCategory) {
    this.subCategory = subCategory;
    return this;
  }

  /**
   * Get subCategory
   * @return subCategory
  */

  @Schema(name = "subCategory", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subCategory")
  public String getSubCategory() {
    return subCategory;
  }

  public void setSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }

  public ExpenseItemSummary amount(Double amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  */

  @Schema(name = "amount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("amount")
  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public ExpenseItemSummary quantity(String quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * @return quantity
  */

  @Schema(name = "quantity", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("quantity")
  public String getQuantity() {
    return quantity;
  }

  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public ExpenseItemSummary unit(String unit) {
    this.unit = unit;
    return this;
  }

  /**
   * Get unit
   * @return unit
  */

  @Schema(name = "unit", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("unit")
  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public ExpenseItemSummary comments(String comments) {
    this.comments = comments;
    return this;
  }

  /**
   * Get comments
   * @return comments
  */

  @Schema(name = "comments", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("comments")
  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExpenseItemSummary expenseItemSummary = (ExpenseItemSummary) o;
    return Objects.equals(this.itemName, expenseItemSummary.itemName) &&
        Objects.equals(this.category, expenseItemSummary.category) &&
        Objects.equals(this.subCategory, expenseItemSummary.subCategory) &&
        Objects.equals(this.amount, expenseItemSummary.amount) &&
        Objects.equals(this.quantity, expenseItemSummary.quantity) &&
        Objects.equals(this.unit, expenseItemSummary.unit) &&
        Objects.equals(this.comments, expenseItemSummary.comments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemName, category, subCategory, amount, quantity, unit, comments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExpenseItemSummary {\n");
    sb.append("    itemName: ").append(toIndentedString(itemName)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    subCategory: ").append(toIndentedString(subCategory)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
    sb.append("    comments: ").append(toIndentedString(comments)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
