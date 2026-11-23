package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.request.BudgetRequestDTO;
import com.expensetracker.expense_tracker.dto.response.BudgetResponseDTO;
import com.expensetracker.expense_tracker.dto.response.BudgetUsageResponseDTO;
import com.expensetracker.expense_tracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService){
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(@Valid @RequestBody BudgetRequestDTO requestDTO){

        BudgetResponseDTO responseDTO = budgetService.createBudget(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets() {

        List<BudgetResponseDTO> budgets = budgetService.getAllBudgets();

        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(@PathVariable Long id) {

        BudgetResponseDTO response = budgetService.getBudgetById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDTO requestDTO) {

        BudgetResponseDTO response = budgetService.updateBudget(id, requestDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {

        budgetService.deleteBudget(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByMonthAndYear(
            @RequestParam Integer month,
            @RequestParam Integer year) {

        List<BudgetResponseDTO> budgets = budgetService.getBudgetsByMonthAndYear(month, year);

        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/usage")
    public ResponseEntity<List<BudgetUsageResponseDTO>> getBudgetUsage(
            @RequestParam Integer month,
            @RequestParam Integer year) {

        List<BudgetUsageResponseDTO> response =
                budgetService.getBudgetUsage(month, year);

        return ResponseEntity.ok(response);
    }
}
