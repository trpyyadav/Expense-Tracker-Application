package com.expensetracker.expense_tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BudgetUsageResponseDTO {

    private Long budgetId;

    private String categoryName;

    private BigDecimal budgetAmount;

    private BigDecimal totalSpent;

    private BigDecimal remainingAmount;

    private BigDecimal percentageUsed;

    private boolean exceeded;
}
