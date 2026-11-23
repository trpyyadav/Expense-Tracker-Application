package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.request.TransactionRequestDTO;
import com.expensetracker.expense_tracker.dto.response.TransactionResponseDTO;
import com.expensetracker.expense_tracker.entity.Category;
import com.expensetracker.expense_tracker.entity.FinancialTransaction;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.enums.TransactionType;
import com.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.expensetracker.expense_tracker.mapper.TransactionMapper;
import com.expensetracker.expense_tracker.repository.CategoryRepository;
import com.expensetracker.expense_tracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.module.ResolutionException;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            CurrentUserService currentUserService
    ){
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    public TransactionResponseDTO createTransaction(TransactionRequestDTO request){

        User user = currentUserService.getCurrentUser();

        Category category = categoryRepository.findByIdAndUserId(
                request.getCategoryId(),
                user.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if(!category.getType().name().equals(request.getType().name())){
            throw new IllegalArgumentException("Category type must match transaction type");
        }

        FinancialTransaction transaction = new FinancialTransaction();

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());

        transaction.setUser(user);
        transaction.setCategory(category);

        FinancialTransaction savedTransaction = transactionRepository.save(transaction);

        return TransactionMapper.toDTO(savedTransaction);

    }

    public List<TransactionResponseDTO> getAllTransactions(){
        Long userId = currentUserService.getCurrentUser().getId();

        List<FinancialTransaction> transactions = transactionRepository.findByUserId(userId);

        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    @Transactional
    public TransactionResponseDTO getTransactionById(Long id) {

        Long userId = currentUserService.getCurrentUserId();

        FinancialTransaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                                    .orElseThrow(() ->
                                            new ResourceNotFoundException(
                                                    "Transaction not found"
                                            )
                                    );

        return TransactionMapper.toDTO(transaction);
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(
            Long id,
            TransactionRequestDTO requestDTO) {

        Long userId = currentUserService.getCurrentUserId();

        // Find transaction belonging to the logged-in user
        FinancialTransaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Transaction not found"));

        // Find category belonging to the same user
        Category category = categoryRepository.findByIdAndUserId(requestDTO.getCategoryId(), userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        // Validate category and transaction types
        if (!category.getType().name().equals(requestDTO.getType().name())) {
            throw new IllegalArgumentException("Category type must match transaction type");
        }

        // Update transaction fields
        transaction.setAmount(requestDTO.getAmount());
        transaction.setType(requestDTO.getType());
        transaction.setDescription(requestDTO.getDescription());
        transaction.setTransactionDate(requestDTO.getTransactionDate());
        transaction.setCategory(category);

        FinancialTransaction updatedTransaction = transactionRepository.save(transaction);

        return TransactionMapper.toDTO(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {

        Long userId = currentUserService.getCurrentUserId();

        FinancialTransaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found"
                                )
                        );

        transactionRepository.delete(transaction);
    }

    @Transactional
    public List<TransactionResponseDTO> getTransactionsByType(TransactionType type) {

        Long userId = currentUserService.getCurrentUserId();

        List<FinancialTransaction> transactions = transactionRepository.findByUserIdAndType(userId, type);

        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    @Transactional
    public List<TransactionResponseDTO> getTransactionsByCategory(Long categoryId) {

        Long userId = currentUserService.getCurrentUserId();

        // Verifying that the category belongs to the logged-in user
        categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        List<FinancialTransaction> transactions = transactionRepository.findByUserIdAndCategoryId(
                        userId,
                        categoryId
                );

        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    @Transactional
    public List<TransactionResponseDTO> getTransactionsByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        Long userId = currentUserService.getCurrentUserId();

        List<FinancialTransaction> transactions = transactionRepository
                        .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                                userId,
                                startDate,
                                endDate
                        );

        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    @Transactional
    public Page<TransactionResponseDTO> getTransactionsWithPagination(Pageable pageable) {

        Long userId = currentUserService.getCurrentUserId();

        Page<FinancialTransaction> transactions = transactionRepository.findByUserId(userId, pageable);

        return transactions.map(TransactionMapper::toDTO);
    }
}
