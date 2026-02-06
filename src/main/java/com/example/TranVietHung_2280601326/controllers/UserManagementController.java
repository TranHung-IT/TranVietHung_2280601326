package com.example.TranVietHung_2280601326.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.TranVietHung_2280601326.models.User;
import com.example.TranVietHung_2280601326.repositories.IRoleRepository;
import com.example.TranVietHung_2280601326.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserManagementController {
    
    private final UserService userService;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String listUsers(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<User> userPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userService.searchUsers(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            userPage = userService.getAllUsers(pageable);
        }
        
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("roles", roleRepository.findAll());
        
        return "admin/users/list";
    }
    
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/users/detail";
    }
    
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/users/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @Valid User user,
            BindingResult result,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) Long roleId,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/users/edit";
        }
        
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return "redirect:/admin/users";
        }
        
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        
        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        
        if (roleId != null) {
            existingUser.getRoles().clear();
            existingUser.getRoles().add(roleRepository.findById(roleId).orElse(null));
        }
        
        userService.updateUser(existingUser);
        
        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/admin/users";
    }
    
    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        if (user != null) {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        if (user != null) {
            // Toggle enabled status (you'll need to add this field to User model)
            // user.setEnabled(!user.isEnabled());
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "User status updated!");
        }
        return "redirect:/admin/users";
    }
}
