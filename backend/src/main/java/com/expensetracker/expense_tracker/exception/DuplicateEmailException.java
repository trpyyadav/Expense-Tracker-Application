package com.expensetracker.expense_tracker.exception;

public class DuplicateEmailException  extends RuntimeException{

    public DuplicateEmailException(String message){
        super(message);
    }
}
