package com.expensetracker.expense_tracker.dto.request;

import com.expensetracker.expense_tracker.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDTO {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater thn zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @Size(max = 500, message = "Description cannot exceed 500 charcaters")
    private String description;

    @NotNull(message = "Amount is required")
    private LocalDate transactionDate;

    @NotNull(message = "Amount is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;


}
