package com.example.TranVietHung_2280601326.viewmodels;

import com.example.TranVietHung_2280601326.models.Book;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BookPostVm(
    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    String title,
    
    @NotBlank(message = "Author must not be blank")
    @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
    String author,
    
    @NotNull(message = "Price must not be blank")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    Double price,
    
    @NotNull(message = "Category ID must not be null")
    Long categoryId
) {
    public static BookPostVm from(@NotNull Book book) {
        return new BookPostVm(
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            book.getCategory() != null ? book.getCategory().getId() : null
        );
    }
}
