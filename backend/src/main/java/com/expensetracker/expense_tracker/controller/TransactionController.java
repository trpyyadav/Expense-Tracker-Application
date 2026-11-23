package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.request.TransactionRequestDTO;
import com.expensetracker.expense_tracker.dto.response.TransactionResponseDTO;
import com.expensetracker.expense_tracker.enums.TransactionType;
import com.expensetracker.expense_tracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO requestDTO){

        TransactionResponseDTO responseDTO = transactionService.createTransaction(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {

        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable Long id) {

        TransactionResponseDTO response = transactionService.getTransactionById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO requestDTO) {

        TransactionResponseDTO response =
                transactionService.updateTransaction(id, requestDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByType(@RequestParam TransactionType type) {

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByType(type);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/filter/category")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByCategory(@RequestParam Long categoryId) {

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByCategory(categoryId);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/filter/date-range")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByDateRange(
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactionsWithPagination(
            @PageableDefault(
                    size = 10,
                    sort = "transactionDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<TransactionResponseDTO> response = transactionService.getTransactionsWithPagination(pageable);

        return ResponseEntity.ok(response);
    }
}
