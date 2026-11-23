package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.request.BudgetRequestDTO;
import com.expensetracker.expense_tracker.dto.response.BudgetResponseDTO;
import com.expensetracker.expense_tracker.dto.response.BudgetUsageResponseDTO;
import com.expensetracker.expense_tracker.entity.Budget;
import com.expensetracker.expense_tracker.entity.Category;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.enums.CategoryType;
import com.expensetracker.expense_tracker.enums.TransactionType;
import com.expensetracker.expense_tracker.exception.DuplicateResourceException;
import com.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.expensetracker.expense_tracker.mapper.BudgetMapper;
import com.expensetracker.expense_tracker.repository.BudgetRepository;
import com.expensetracker.expense_tracker.repository.CategoryRepository;
import com.expensetracker.expense_tracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final TransactionRepository transactionRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            CurrentUserService currentUserService,
            TransactionRepository transactionRepository
    ){
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
        this.transactionRepository = transactionRepository;
    }

    public BudgetResponseDTO createBudget(BudgetRequestDTO requestDTO){

        User user = currentUserService.getCurrentUser();

        Category category = categoryRepository
                .findByIdAndUserId(
                        requestDTO.getCategoryId(),
                        user.getId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Category not found..."));

        if(category.getType() != CategoryType.EXPENSE){
            throw new IllegalArgumentException("Budget can only be created for expenses");
        }

        boolean budgetExists = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(),
                requestDTO.getCategoryId(),
                requestDTO.getMonth(),
                requestDTO.getYear()
        ).isPresent();

        if (budgetExists) {
            throw new DuplicateResourceException("Budget already exists for this category and month");
        }

        Budget budget = new Budget();

        budget.setAmount(requestDTO.getAmount());
        budget.setMonth(requestDTO.getMonth());
        budget.setYear(requestDTO.getYear());
        budget.setUser(user);
        budget.setCategory(category);

        Budget savedBudget = budgetRepository.save(budget);

        return BudgetMapper.toDTO(savedBudget);
    }

    @Transactional
    public List<BudgetResponseDTO> getAllBudgets() {

        Long userId = currentUserService.getCurrentUserId();

        List<Budget> budgets = budgetRepository.findByUserId(userId);

        return budgets.stream()
                .map(BudgetMapper::toDTO)
                .toList();
    }

    @Transactional
    public BudgetResponseDTO getBudgetById(Long budgetId) {

        Long userId = currentUserService.getCurrentUserId();

        Budget budget = budgetRepository
                .findByIdAndUserId(budgetId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Budget not found"));

        return BudgetMapper.toDTO(budget);
    }

    @Transactional
    public BudgetResponseDTO updateBudget(
            Long budgetId,
            BudgetRequestDTO requestDTO) {

        Long userId = currentUserService.getCurrentUserId();

        Budget budget = budgetRepository
                .findByIdAndUserId(budgetId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Budget not found"));

        Category category = categoryRepository
                .findByIdAndUserId(requestDTO.getCategoryId(), userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(
                    "Budget can only be created for expense categories");
        }

        Optional<Budget> existingBudget =
                budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                        userId,
                        category.getId(),
                        requestDTO.getMonth(),
                        requestDTO.getYear()
                );

        if (existingBudget.isPresent()
                && !existingBudget.get().getId().equals(budgetId)) {

            throw new DuplicateResourceException(
                    "Budget already exists for this category and month");
        }

        budget.setAmount(requestDTO.getAmount());
        budget.setMonth(requestDTO.getMonth());
        budget.setYear(requestDTO.getYear());
        budget.setCategory(category);

        Budget updatedBudget = budgetRepository.save(budget);

        return BudgetMapper.toDTO(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long budgetId) {

        Long userId = currentUserService.getCurrentUserId();

        Budget budget = budgetRepository
                .findByIdAndUserId(budgetId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
    }

    @Transactional
    public List<BudgetResponseDTO> getBudgetsByMonthAndYear(
            Integer month,
            Integer year) {

        Long userId = currentUserService.getCurrentUserId();

        List<Budget> budgets =
                budgetRepository.findByUserIdAndMonthAndYear(
                        userId, month, year
                );

        return budgets.stream()
                .map(BudgetMapper::toDTO)
                .toList();
    }

    @Transactional
    public List<BudgetUsageResponseDTO> getBudgetUsage(
            Integer month,
            Integer year) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        if (year < 2000) {
            throw new IllegalArgumentException("Invalid year");
        }

        Long userId = currentUserService.getCurrentUserId();

        // Get first and last dates of the selected month
        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all budgets for the selected month
        List<Budget> budgets =
                budgetRepository.findByUserIdAndMonthAndYear(
                        userId, month, year
                );

        // Calculate usage for each budget
        return budgets.stream()
                .map(budget -> {

                    BigDecimal totalSpent =
                            transactionRepository.calculateTotalSpent(
                                    userId,
                                    budget.getCategory().getId(),
                                    TransactionType.EXPENSE,
                                    startDate,
                                    endDate
                            );

                    BigDecimal remainingAmount =
                            budget.getAmount().subtract(totalSpent);

                    BigDecimal percentageUsed =
                            totalSpent.multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            budget.getAmount(),
                                            2,
                                            RoundingMode.HALF_UP
                                    );

                    return new BudgetUsageResponseDTO(
                            budget.getId(),
                            budget.getCategory().getName(),
                            budget.getAmount(),
                            totalSpent,
                            remainingAmount,
                            percentageUsed,
                            totalSpent.compareTo(budget.getAmount()) > 0
                    );
                })
                .toList();
    }
}
