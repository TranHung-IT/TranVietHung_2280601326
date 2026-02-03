package com.example.TranVietHung_2280601326.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.Book;

@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {
    
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchBooks(@Param("keyword") String keyword);
}