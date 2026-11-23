package com.expensetracker.expense_tracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is Required")
    private String password;
}
