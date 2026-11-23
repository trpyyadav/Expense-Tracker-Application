package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.Category;
import com.expensetracker.expense_tracker.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndTypeAndUserId(
            String email,
            CategoryType type,
            Long userId
    );


}
