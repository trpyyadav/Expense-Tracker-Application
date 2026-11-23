package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.response.*;
import com.expensetracker.expense_tracker.entity.FinancialTransaction;
import com.expensetracker.expense_tracker.enums.TransactionType;
import com.expensetracker.expense_tracker.mapper.TransactionMapper;
import com.expensetracker.expense_tracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public AnalyticsService(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ){
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public FinancialOverviewResponseDTO getFinancialOverview(){

        Long userId = currentUserService.getCurrentUserId();

        BigDecimal totalIncome = transactionRepository.calculateTotalByUserIdAndType(
                userId,
                TransactionType.INCOME
        );

        BigDecimal totalExpenses = transactionRepository.calculateTotalByUserIdAndType(
                userId,
                TransactionType.EXPENSE
        );

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        FinancialOverviewResponseDTO financialOverviewResponseDTO = new FinancialOverviewResponseDTO();

        financialOverviewResponseDTO.setTotalIncome(totalIncome);
        financialOverviewResponseDTO.setTotalExpenses(totalExpenses);
        financialOverviewResponseDTO.setNetSavings(netSavings);

        return financialOverviewResponseDTO;

    }

    public MonthlySummaryResponseDTO getMonthlySummary(
            Integer month,
            Integer year
    ){
        if(month < 1 || month > 12){
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if(year < 2000){
            throw new IllegalArgumentException("Invalid year");
        }

        Long userId = currentUserService.getCurrentUserId();

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

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

        return new MonthlySummaryResponseDTO(
                month,
                year,
                totalIncome,
                totalExpenses,
                netSavings
        );
    }

    public List<CategoryExpenseResponseDTO> getCategoryWiseExpense(
            Integer month,
            Integer year
    ){
        if(month < 1 || month > 12){
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if(year < 2000){
            throw new IllegalArgumentException("Invalid year");
        }

        Long userId = currentUserService.getCurrentUserId();

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Object[]> results = transactionRepository.findCategoryWiseExpenses(
                userId,
                TransactionType.EXPENSE,
                startDate,
                endDate
        );

        BigDecimal totalExpenses = results.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryExpenseResponseDTO> categoryExpenses =  results.stream()
                .map(row ->{
                            Long categoryId = (Long) row[0];
                            String categoryName = (String) row[1];
                            BigDecimal totalSpent = (BigDecimal) row[2];

                            BigDecimal percentage = totalSpent.multiply(BigDecimal.valueOf(100)
                                                    .divide(totalExpenses, 2, RoundingMode.HALF_DOWN));

                            return new CategoryExpenseResponseDTO(
                                    categoryId,
                                    categoryName,
                                    totalSpent,
                                    percentage
                            );
                }).toList();

        return categoryExpenses;
    }

    @Transactional
    public List<MonthlyTrendResponseDTO> getMonthlyTrends(int months) {

        if (months < 1 || months > 12) {
            throw new IllegalArgumentException(
                    "Months must be between 1 and 12"
            );
        }

        Long userId = currentUserService.getCurrentUserId();

        // Calculate the date range
        YearMonth currentMonth = YearMonth.now();

        YearMonth startMonth = currentMonth.minusMonths(months - 1);

        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        // Fetch grouped monthly results
        List<Object[]> results =
                transactionRepository.findMonthlyTrends(
                        userId,
                        startDate,
                        endDate
                );

        // Store results month-wise
        Map<YearMonth, MonthlyTrendResponseDTO> monthlyData = new LinkedHashMap<>();

        // Initialize every month with zero values
        for (int i = 0; i < months; i++) {

            YearMonth month = startMonth.plusMonths(i);

            monthlyData.put(
                    month,
                    new MonthlyTrendResponseDTO(
                            month.getMonthValue(),
                            month.getYear(),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    )
            );
        }

        // Populate income and expense totals
        for (Object[] row : results) {

            Integer year = ((Number) row[0]).intValue();
            Integer month = ((Number) row[1]).intValue();

            TransactionType type = (TransactionType) row[2];

            BigDecimal amount = (BigDecimal) row[3];

            YearMonth yearMonth = YearMonth.of(year, month);

            MonthlyTrendResponseDTO dto = monthlyData.get(yearMonth);

            if (type == TransactionType.INCOME) {
                dto.setTotalIncome(amount);
            } else if (type == TransactionType.EXPENSE) {
                dto.setTotalExpenses(amount);
            }
        }

        // Calculate net savings for every month
        for (MonthlyTrendResponseDTO dto : monthlyData.values()) {

            BigDecimal netSavings = dto.getTotalIncome()
                    .subtract(dto.getTotalExpenses());

            dto.setNetSavings(netSavings);
        }

        return new ArrayList<>(monthlyData.values());
    }

    @Transactional
    public List<TransactionResponseDTO> getRecentTransactions(int limit) {

        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 50"
            );
        }

        Long userId = currentUserService.getCurrentUserId();

        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "transactionDate")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<FinancialTransaction> transactionPage = transactionRepository.findByUserId(userId, pageable);

        return transactionPage.getContent()
                .stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    @Transactional
    public DashboardResponseDTO getDashboardSummary() {

        YearMonth currentMonth = YearMonth.now();

        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();

        FinancialOverviewResponseDTO overview = getFinancialOverview();

        MonthlySummaryResponseDTO monthlySummary = getMonthlySummary(month, year);

        List<CategoryExpenseResponseDTO> categoryExpenses = getCategoryWiseExpense(month, year);

        List<MonthlyTrendResponseDTO> monthlyTrends = getMonthlyTrends(6);

        List<TransactionResponseDTO> recentTransactions = getRecentTransactions(5);

        return new DashboardResponseDTO(
                overview,
                monthlySummary,
                categoryExpenses,
                monthlyTrends,
                recentTransactions
        );
    }


}
