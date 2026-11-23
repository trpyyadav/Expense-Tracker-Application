package com.expensetracker.expense_tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialOverviewResponseDTO {

    private BigDecimal totalIncome;

    private BigDecimal totalExpenses;

    private BigDecimal netSavings;
}
