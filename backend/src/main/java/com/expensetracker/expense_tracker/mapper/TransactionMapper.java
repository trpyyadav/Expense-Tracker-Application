package com.expensetracker.expense_tracker.mapper;

import com.expensetracker.expense_tracker.dto.response.TransactionResponseDTO;
import com.expensetracker.expense_tracker.entity.FinancialTransaction;

public class TransactionMapper {

    public static TransactionResponseDTO toDTO(FinancialTransaction transaction){
        TransactionResponseDTO dto = new TransactionResponseDTO();

        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setCategoryName(transaction.getCategory().getName());

        return dto;
    }
}
