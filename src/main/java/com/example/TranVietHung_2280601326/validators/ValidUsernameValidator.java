package com.example.TranVietHung_2280601326.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.TranVietHung_2280601326.services.UserService;
import com.example.TranVietHung_2280601326.validators.annotations.ValidUsername;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {
    @Autowired
    private UserService userService;
    
    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (userService == null) {
            return true; // Skip validation if UserService not available
        }
        return userService.findByUsername(username).isEmpty();
    }
}
