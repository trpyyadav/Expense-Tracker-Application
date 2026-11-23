package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.FinancialTransaction;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findByUserId(Long userId);

    List<FinancialTransaction> findByUserIdAndType(
            Long userId,
            com.expensetracker.expense_tracker.enums.TransactionType type
    );

    Optional<FinancialTransaction> findByIdAndUserId(Long id, Long userId);

    List<FinancialTransaction> findByUserIdAndCategoryId(
            Long userId,
            Long categoryId
    );

    List<FinancialTransaction>
    findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    Page<FinancialTransaction> findByUserId(
            Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM FinancialTransaction t
    WHERE t.user.id = :userId
    AND (:categoryId IS NULL OR t.category.id = :categoryId)
    AND t.type = :type
    AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal calculateTotalSpent(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM FinancialTransaction t
    where t.user.id = :userId
    AND t.type = :type
    """)
    BigDecimal calculateTotalByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") TransactionType type
    );

    @Query("""
    SELECT t.category.id, t.category.name, SUM(t.amount)
    FROM FinancialTransaction t
    WHERE t.user.id = :userId
    AND t.type = :type
    AND t.transactionDate BETWEEN :startDate AND :endDate
    GROUP BY t.category.id, t.category.name
    ORDER BY SUM(t.amount) desc
    """)
    List<Object[]> findCategoryWiseExpenses(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT YEAR(t.transactionDate),
           MONTH(t.transactionDate),
           t.type,
           SUM(t.amount)
    FROM FinancialTransaction t
    WHERE t.user.id = :userId
    AND t.transactionDate BETWEEN :startDate AND :endDate
    GROUP BY YEAR(t.transactionDate),
             MONTH(t.transactionDate),
             t.type
    ORDER BY YEAR(t.transactionDate),
             MONTH(t.transactionDate)
    """)
    List<Object[]> findMonthlyTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
