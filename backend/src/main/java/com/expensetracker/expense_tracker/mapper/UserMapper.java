package com.expensetracker.expense_tracker.mapper;

import com.expensetracker.expense_tracker.dto.response.UserResponseDTO;
import com.expensetracker.expense_tracker.entity.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user){
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return dto;
    }
}
