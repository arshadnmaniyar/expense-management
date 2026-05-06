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
 * ExpenseSearchResponse
 */

@Schema(name = "ExpenseSearchResponse", description = "")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class ExpenseSearchResponse {

  @Valid
  private java.util.List<@Valid ExpenseSummary> expenses = new java.util.ArrayList<>();

  private String nextCursor;

  public ExpenseSearchResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ExpenseSearchResponse(java.util.List<@Valid ExpenseSummary> expenses) {
    this.expenses = expenses;
  }

  public ExpenseSearchResponse expenses(java.util.List<@Valid ExpenseSummary> expenses) {
    this.expenses = expenses;
    return this;
  }

  public ExpenseSearchResponse addExpensesItem(ExpenseSummary expensesItem) {
    if (this.expenses == null) {
      this.expenses = new java.util.ArrayList<>();
    }
    this.expenses.add(expensesItem);
    return this;
  }

  /**
   * Get expenses
   * @return expenses
  */
  @Valid
  @Schema(name = "expenses", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expenses")
  public java.util.List<@Valid ExpenseSummary> getExpenses() {
    return expenses;
  }

  public void setExpenses(java.util.List<@Valid ExpenseSummary> expenses) {
    this.expenses = expenses;
  }

  public ExpenseSearchResponse nextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
    return this;
  }

  /**
   * Next cursor for pagination, null if no more
   * @return nextCursor
  */

  @Schema(name = "nextCursor", description = "Next cursor for pagination, null if no more", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nextCursor")
  public String getNextCursor() {
    return nextCursor;
  }

  public void setNextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExpenseSearchResponse expenseSearchResponse = (ExpenseSearchResponse) o;
    return Objects.equals(this.expenses, expenseSearchResponse.expenses) &&
        Objects.equals(this.nextCursor, expenseSearchResponse.nextCursor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expenses, nextCursor);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExpenseSearchResponse {\n");
    sb.append("    expenses: ").append(toIndentedString(expenses)).append("\n");
    sb.append("    nextCursor: ").append(toIndentedString(nextCursor)).append("\n");
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
