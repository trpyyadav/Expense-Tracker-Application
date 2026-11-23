package com.expensetracker.expense_tracker.mapper;

import com.expensetracker.expense_tracker.dto.response.CategoryResponseDTO;
import com.expensetracker.expense_tracker.entity.Category;

public class CategoryMapper {

    public static CategoryResponseDTO toDTO(Category category){
        CategoryResponseDTO dto = new CategoryResponseDTO();

        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());

        return dto;
    }
}
