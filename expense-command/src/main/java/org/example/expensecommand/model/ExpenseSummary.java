package org.example.expensecommand.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.*;
import jakarta.annotation.Generated;

/**
 * ExpenseSummary
 */

@Schema(name = "ExpenseSummary", description = "")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class ExpenseSummary {

  private LocalDate transactionDate;

  private LocalDate purchaseDate;

  private String store;

  private String paymentType;

  @Valid
  private java.util.List<@Valid ExpenseItemSummary> items = new java.util.ArrayList<>();

  public ExpenseSummary() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ExpenseSummary(LocalDate transactionDate, LocalDate purchaseDate, String store, String paymentType, java.util.List<@Valid ExpenseItemSummary> items) {
    this.transactionDate = transactionDate;
    this.purchaseDate = purchaseDate;
    this.store = store;
    this.paymentType = paymentType;
    this.items = items;
  }

  public ExpenseSummary transactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
    return this;
  }

  /**
   * Get transactionDate
   * @return transactionDate
  */
  @Valid
  @Schema(name = "transactionDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transactionDate")
  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public ExpenseSummary purchaseDate(LocalDate purchaseDate) {
    this.purchaseDate = purchaseDate;
    return this;
  }

  /**
   * Get purchaseDate
   * @return purchaseDate
  */
  @Valid
  @Schema(name = "purchaseDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("purchaseDate")
  public LocalDate getPurchaseDate() {
    return purchaseDate;
  }

  public void setPurchaseDate(LocalDate purchaseDate) {
    this.purchaseDate = purchaseDate;
  }

  public ExpenseSummary store(String store) {
    this.store = store;
    return this;
  }

  /**
   * Get store
   * @return store
  */

  @Schema(name = "store", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("store")
  public String getStore() {
    return store;
  }

  public void setStore(String store) {
    this.store = store;
  }

  public ExpenseSummary paymentType(String paymentType) {
    this.paymentType = paymentType;
    return this;
  }

  /**
   * Get paymentType
   * @return paymentType
  */

  @Schema(name = "paymentType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("paymentType")
  public String getPaymentType() {
    return paymentType;
  }

  public void setPaymentType(String paymentType) {
    this.paymentType = paymentType;
  }

  public ExpenseSummary items(java.util.List<@Valid ExpenseItemSummary> items) {
    this.items = items;
    return this;
  }

  public ExpenseSummary addItemsItem(ExpenseItemSummary itemsItem) {
    if (this.items == null) {
      this.items = new java.util.ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
  */
  @Valid
  @Schema(name = "items", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("items")
  public java.util.List<@Valid ExpenseItemSummary> getItems() {
    return items;
  }

  public void setItems(java.util.List<@Valid ExpenseItemSummary> items) {
    this.items = items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExpenseSummary expenseSummary = (ExpenseSummary) o;
    return Objects.equals(this.transactionDate, expenseSummary.transactionDate) &&
        Objects.equals(this.purchaseDate, expenseSummary.purchaseDate) &&
        Objects.equals(this.store, expenseSummary.store) &&
        Objects.equals(this.paymentType, expenseSummary.paymentType) &&
        Objects.equals(this.items, expenseSummary.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionDate, purchaseDate, store, paymentType, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExpenseSummary {\n");
    sb.append("    transactionDate: ").append(toIndentedString(transactionDate)).append("\n");
    sb.append("    purchaseDate: ").append(toIndentedString(purchaseDate)).append("\n");
    sb.append("    store: ").append(toIndentedString(store)).append("\n");
    sb.append("    paymentType: ").append(toIndentedString(paymentType)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
