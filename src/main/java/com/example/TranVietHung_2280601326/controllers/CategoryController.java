package com.example.TranVietHung_2280601326.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.TranVietHung_2280601326.models.Category;
import com.example.TranVietHung_2280601326.services.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * GET /admin/categories - Hiển thị danh sách categories
     */
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories/list";
    }
    
    /**
     * GET /admin/categories/add - Hiển thị form thêm category
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/add";
    }
    
    /**
     * POST /admin/categories/add - Xử lý thêm category
     */
    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute("category") Category category,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            return "admin/categories/add";
        }
        
        try {
            categoryService.addCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category added successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", "Error adding category: " + e.getMessage());
            return "admin/categories/add";
        }
    }
    
    /**
     * GET /admin/categories/edit/{id} - Hiển thị form sửa category
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Category not found!");
            return "redirect:/admin/categories";
        }
        
        model.addAttribute("category", category);
        return "admin/categories/edit";
    }
    
    /**
     * POST /admin/categories/edit - Xử lý cập nhật category
     */
    @PostMapping("/edit")
    public String updateCategory(@Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            return "admin/categories/edit";
        }
        
        try {
            categoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating category: " + e.getMessage());
            return "admin/categories/edit";
        }
    }
    
    /**
     * GET /admin/categories/delete/{id} - Xóa category
     */
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Category not found!");
                return "redirect:/admin/categories";
            }
            
            categoryService.deleteCategoryById(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting category: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
}
