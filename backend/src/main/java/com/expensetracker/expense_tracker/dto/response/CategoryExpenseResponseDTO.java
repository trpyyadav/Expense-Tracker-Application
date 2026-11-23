package com.expensetracker.expense_tracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryExpenseResponseDTO {

    private long categoryId;
    private String categoryName;
    private BigDecimal totalSpent;
    private BigDecimal percentage;
}
