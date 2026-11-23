package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.response.*;
import com.expensetracker.expense_tracker.service.AnalyticsService;
import org.hibernate.validator.internal.metadata.aggregated.rule.ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<FinancialOverviewResponseDTO> getFinancialOverview(){

        FinancialOverviewResponseDTO financialOverviewResponseDTO = analyticsService.getFinancialOverview();

        return ResponseEntity.ok(financialOverviewResponseDTO);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponseDTO> getMonthlySummary(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        MonthlySummaryResponseDTO monthlySummaryResponseDTO = analyticsService.getMonthlySummary(month, year);

        return ResponseEntity.ok(monthlySummaryResponseDTO);
    }

    @GetMapping("/category-expenses")
    public ResponseEntity<List<CategoryExpenseResponseDTO>> getCategoryWiseExpenses(
            @RequestParam Integer month,
            @RequestParam Integer year) {

        List<CategoryExpenseResponseDTO> response = analyticsService.getCategoryWiseExpense(month, year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<List<MonthlyTrendResponseDTO>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months) {

        List<MonthlyTrendResponseDTO> response = analyticsService.getMonthlyTrends(months);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getRecentTransactions(
            @RequestParam(defaultValue = "5") int limit) {

        List<TransactionResponseDTO> response = analyticsService.getRecentTransactions(limit);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDTO> getDashboardSummary() {

        DashboardResponseDTO response = analyticsService.getDashboardSummary();

        return ResponseEntity.ok(response);
    }
}

