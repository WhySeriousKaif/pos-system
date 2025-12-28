package com.molla.service;

import com.molla.exceptions.UserException;
import com.molla.model.User;
import com.molla.payload.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto, User user) throws UserException;

    CategoryDto updateCategory(Long id, CategoryDto categoryDto, User user) throws UserException;

    void deleteCategory(Long id, User user) throws UserException;

    List<CategoryDto> getCategoriesByStoreId(Long storeId) throws UserException;
    
    CategoryDto moderateCategory(Long id, CategoryDto categoryDto, User user) throws UserException;
}
