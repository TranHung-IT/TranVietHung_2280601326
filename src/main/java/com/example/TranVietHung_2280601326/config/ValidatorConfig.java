package com.example.TranVietHung_2280601326.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

@Configuration
public class ValidatorConfig {
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}
