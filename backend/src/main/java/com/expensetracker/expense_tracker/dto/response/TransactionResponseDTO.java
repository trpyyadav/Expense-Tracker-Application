package com.expensetracker.expense_tracker.dto.response;

import com.expensetracker.expense_tracker.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransactionResponseDTO {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDate transactionDate;

    private Long categoryId;
    private String categoryName;
}
