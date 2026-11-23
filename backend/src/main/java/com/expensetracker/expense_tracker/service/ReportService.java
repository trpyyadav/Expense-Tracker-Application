package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.response.*;
import com.expensetracker.expense_tracker.enums.TransactionType;
import com.expensetracker.expense_tracker.mapper.TransactionMapper;
import com.expensetracker.expense_tracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final BudgetService budgetService;

    public ReportService(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService,
            BudgetService budgetService
    ){
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
        this.budgetService = budgetService;
    }

    @Transactional
    public FinancialReportResponseDTO generateFinancialReport(
            LocalDate startDate,
            LocalDate endDate
    ){
        if(startDate == null || endDate == null){
            throw new IllegalArgumentException("Start Date or end Date cannot be null...");
        }

        if(startDate.isAfter(endDate)){
            throw new IllegalArgumentException("Start date cannot be after end date...");
        }

        Long userId = currentUserService.getCurrentUserId();

        //Calculate Financial Overview
        BigDecimal totalIncome = transactionRepository.calculateTotalSpent(
                userId,
                null,
                TransactionType.INCOME,
                startDate,
                endDate
        );

        BigDecimal totalExpenses = transactionRepository.calculateTotalSpent(
                userId,
                null,
                TransactionType.EXPENSE,
                startDate,
                endDate
        );
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        FinancialOverviewResponseDTO financialOverview = new FinancialOverviewResponseDTO(
                totalIncome,
                totalExpenses,
                netSavings
        );

        // Calculate Category wise expenses

        List<Object[]> results = transactionRepository.findCategoryWiseExpenses(
                userId,
                TransactionType.EXPENSE,
                startDate,
                endDate
        );

        BigDecimal categoryTotal = results.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryExpenseResponseDTO> categoryExpense = results.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    String categoryName = (String) row[1];
                    BigDecimal totalSpent = (BigDecimal) row[2];

                    BigDecimal percentage = (BigDecimal) totalSpent.multiply(BigDecimal.valueOf(100))
                            .divide(categoryTotal, 2, RoundingMode.HALF_DOWN);

                    return new CategoryExpenseResponseDTO(
                            categoryId,
                            categoryName,
                            totalSpent,
                            percentage
                    );

                }).toList();


        List<TransactionResponseDTO> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                userId,
                startDate,
                endDate
        ).stream()
                .map(TransactionMapper::toDTO)
                .toList();


        return new FinancialReportResponseDTO(
                startDate,
                endDate,
                financialOverview,
                categoryExpense,
                transactions
        );
    }

    @Transactional
    public MonthlyReportResponseDTO getMonthlySummary(
            Integer month,
            Integer year
    ){
        if(month == null || year == null){
            throw new IllegalArgumentException("Month and year are required...");
        }

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Long userId = currentUserService.getCurrentUserId();

        // Financial Overview

        BigDecimal totalIncome = transactionRepository.calculateTotalSpent(
                userId,
                null,
                TransactionType.INCOME,
                startDate,
                endDate
        );

        BigDecimal totalExpenses = transactionRepository.calculateTotalSpent(
                userId,
                null,
                TransactionType.EXPENSE,
                startDate,
                endDate
        );

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        FinancialOverviewResponseDTO financialOverview = new FinancialOverviewResponseDTO(
                totalIncome,
                totalExpenses,
                netSavings
        );

        List<Object[]> results = transactionRepository.findCategoryWiseExpenses(
                userId,
                TransactionType.EXPENSE,
                startDate,
                endDate
        );

        BigDecimal categoryTotal = results.stream()
                .map(row -> (BigDecimal )row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryExpenseResponseDTO> categoryExpenses = results.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    String categoryName = (String) row[1];
                    BigDecimal totalSpent = (BigDecimal) row[2];

                    BigDecimal percentage = totalSpent.multiply(BigDecimal.valueOf(100))
                            .divide(categoryTotal, 2, RoundingMode.HALF_DOWN);

                    return new CategoryExpenseResponseDTO(
                            categoryId,
                            categoryName,
                            totalSpent,
                            percentage
                    );
                }).toList();

        List<TransactionResponseDTO> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                userId,
                startDate,
                endDate
        ).stream()
                .map(TransactionMapper::toDTO)
                .toList();

        List<BudgetUsageResponseDTO> budgetUsage = budgetService.getBudgetUsage(month, year);

        return new MonthlyReportResponseDTO(
                month,
                year,
                financialOverview,
                categoryExpenses,
                transactions,
                budgetUsage
        );
    }
}