package com.example.TranVietHung_2280601326.viewmodels;

import com.example.TranVietHung_2280601326.models.Category;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CategoryVm(
    Long id,
    String name
) {
    public static CategoryVm from(@NotNull Category category) {
        return new CategoryVm(
            category.getId(),
            category.getName()
        );
    }
}
