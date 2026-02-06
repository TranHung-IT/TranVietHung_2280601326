package com.example.TranVietHung_2280601326.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.BookImage;

@Repository
public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBookIdOrderByDisplayOrderAsc(Long bookId);
    void deleteByBookId(Long bookId);
}
