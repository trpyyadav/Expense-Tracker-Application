package com.expensetracker.expense_tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportResponseDTO {

    private Integer month;
    private Integer year;

    private FinancialOverviewResponseDTO financialOverview;

    private List<CategoryExpenseResponseDTO> categoryExpenses;

    private List<TransactionResponseDTO> transactions;

    private List<BudgetUsageResponseDTO> budgetUsage;
}

