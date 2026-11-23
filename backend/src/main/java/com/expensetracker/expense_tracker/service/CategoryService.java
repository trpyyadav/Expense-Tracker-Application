package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.request.CategoryRequestDTO;
import com.expensetracker.expense_tracker.dto.response.CategoryResponseDTO;
import com.expensetracker.expense_tracker.entity.Category;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.exception.CategoryInUseException;
import com.expensetracker.expense_tracker.exception.DuplicateResourceException;
import com.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.expensetracker.expense_tracker.mapper.CategoryMapper;
import com.expensetracker.expense_tracker.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository, CurrentUserService currentUserService){
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO){
        User user = currentUserService.getCurrentUser();

        boolean exists = categoryRepository.existsByNameAndTypeAndUserId(
               requestDTO.getName(),
               requestDTO.getType(),
                user.getId()
        );

        if(exists){
            throw new DuplicateResourceException("Category already exists");
        }

        Category category = new Category();

        category.setName(requestDTO.getName());
        category.setType(requestDTO.getType());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toDTO(savedCategory);

    }

    @Transactional
    public List<CategoryResponseDTO> getAllCategories(){

        Long userId = currentUserService.getCurrentUserId();

        return categoryRepository.findByUserId(userId)
                .stream()
                .map(CategoryMapper::toDTO)
                .toList();

    }

    @Transactional
    public CategoryResponseDTO getCategoryById(Long categoryId) {

        Long userId = currentUserService.getCurrentUserId();

        Category category = categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        return CategoryMapper.toDTO(category);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(
            Long categoryId,
            CategoryRequestDTO requestDTO) {

        Long userId = currentUserService.getCurrentUserId();

        Category category = categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        boolean exists = categoryRepository
                .existsByNameAndTypeAndUserId(
                        requestDTO.getName(),
                        requestDTO.getType(),
                        userId
                );

        if (exists &&
                (!category.getName().equals(requestDTO.getName()) ||
                        category.getType() != requestDTO.getType())) {

            throw new DuplicateResourceException("Category already exists");
        }

        category.setName(requestDTO.getName());
        category.setType(requestDTO.getType());

        Category updatedCategory = categoryRepository.save(category);

        return CategoryMapper.toDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {

        Long userId = currentUserService.getCurrentUserId();

        Category category = categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();

        } catch (DataIntegrityViolationException ex) {

            throw new CategoryInUseException("Cannot delete category because it is being used " + "by a transaction or budget");
        }
    }








}