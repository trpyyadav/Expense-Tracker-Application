package com.expensetracker.expense_tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FinancialReportResponseDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private FinancialOverviewResponseDTO financialOverview;
    private List<CategoryExpenseResponseDTO> categoryExpense;
    private List<TransactionResponseDTO> transactions;

}
