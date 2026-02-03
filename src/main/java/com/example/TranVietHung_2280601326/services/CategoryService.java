package com.example.TranVietHung_2280601326.services;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.models.Category;
import com.example.TranVietHung_2280601326.repositories.ICategoryRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class CategoryService {
    private final ICategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public void updateCategory(@NotNull Category category) { 
        Category existingCategory = categoryRepository 
        .findById(category.getId()) .orElse(null); 
        Objects.requireNonNull(existingCategory) 
        .setName(category.getName()); 
        categoryRepository.save(existingCategory); }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }
}
