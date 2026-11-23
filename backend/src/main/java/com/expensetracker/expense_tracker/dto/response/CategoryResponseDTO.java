package com.expensetracker.expense_tracker.dto.response;

import com.expensetracker.expense_tracker.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private CategoryType type;
}
