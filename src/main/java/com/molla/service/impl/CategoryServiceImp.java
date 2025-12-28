package com.molla.service.impl;

import com.molla.domain.UserRole;
import com.molla.exceptions.UserException;
import com.molla.mapper.CategoryMapper;
import com.molla.model.Category;
import com.molla.model.Store;
import com.molla.model.User;
import com.molla.payload.dto.CategoryDto;
import com.molla.repository.CategoryRepository;
import com.molla.repository.StoreRepository;
import com.molla.service.CategoryService;
import com.molla.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final StoreRepository storeRepository;
    @Override
    public CategoryDto createCategory(CategoryDto categoryDto, User user) throws UserException {
        User currentUser = userService.getCurrentUser();
        Store store = storeRepository.findById(categoryDto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        checkAuthority(currentUser, store);
        
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setStore(store);
       
        return CategoryMapper.toDto(categoryRepository.save(category));
    }
    @Override
    public List<CategoryDto> getCategoriesByStoreId(Long storeId) throws UserException {
        List<Category> categories = categoryRepository.findByStoreId(storeId);
        return categories.stream().map(CategoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto, User user) throws UserException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        User currentUser = userService.getCurrentUser();

        category.setName(categoryDto.getName());
        checkAuthority(currentUser, category.getStore()); 

        return CategoryMapper.toDto(categoryRepository.save(category));
    }
    
    @Override
    public CategoryDto moderateCategory(Long id, CategoryDto categoryDto, User user) throws UserException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        User currentUser = userService.getCurrentUser();
        checkAuthority(currentUser, category.getStore());
        
        category.setName(categoryDto.getName());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }
    
    @Override
    public void deleteCategory(Long id, User user) throws UserException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        User currentUser = userService.getCurrentUser();
        checkAuthority(currentUser, category.getStore()); 
        categoryRepository.delete(category);
    }

    private void checkAuthority(User user, Store store) throws UserException {
        boolean isAdmin = user.getRole().equals(UserRole.ROLE_STORE_ADMIN);
        boolean isManager = user.getRole().equals(UserRole.ROLE_STORE_MANAGER);
        boolean isSameStore = user.getId().equals(store.getStoreAdmin().getId());

        if(!(isAdmin || isManager) && !isSameStore) {
            throw new UserException("You don't have permission to access this category");
        }
    }

    
    
}
