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
public class DashboardResponseDTO {

    private FinancialOverviewResponseDTO financialOverview;

    private MonthlySummaryResponseDTO monthlySummary;

    private List<CategoryExpenseResponseDTO> categoryExpenses;

    private List<MonthlyTrendResponseDTO> monthlyTrends;

    private List<TransactionResponseDTO> recentTransactions;
}
