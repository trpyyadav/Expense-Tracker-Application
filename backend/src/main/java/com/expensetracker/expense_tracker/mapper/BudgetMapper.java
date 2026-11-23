package com.expensetracker.expense_tracker.mapper;

import com.expensetracker.expense_tracker.dto.response.BudgetResponseDTO;
import com.expensetracker.expense_tracker.entity.Budget;

public class BudgetMapper {

    public static BudgetResponseDTO toDTO(Budget budget){
        BudgetResponseDTO dto = new BudgetResponseDTO();

        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setCategoryId(budget.getCategory().getId());
        dto.setCategoryName(budget.getCategory().getName());

        return dto;

    }
}
