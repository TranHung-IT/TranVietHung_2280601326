package com.example.TranVietHung_2280601326;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.models.Category;

@Configuration
public class AppConfig {
    @Bean 
    public List<Category> getCategories() {
        Category category1 = Category.builder()
            .id(1L)
            .name("Công nghệ thông tin")
            .build();
        
        return List.of(category1);
    }
    
    @Bean 
    public List<Book> getBooks() { 
        Category category = Category.builder()
            .id(1L)
            .name("Công nghệ thông tin")
            .build();
        
        var books = new ArrayList<Book>(); 

        books.add(Book.builder().id(1L).title("Lập trình Web Spring Framework").author("Ánh Nguyễn").price(29.99).category(category).build()); 
        books.add(Book.builder().id(2L).title("Lập trình ứng dụng Java").author("Huy Cường").price(45.63).category(category).build()); 
        books.add(Book.builder().id(3L).title("Lập trình Web Spring Boot").author("Xuân Nhân").price(12.0).category(category).build()); 
        books.add(Book.builder().id(4L).title("Lập trình Web Spring MVC").author("Ánh Nguyễn").price(0.12).category(category).build()); 
        
        return books; 
    }
}
