package com.example.TranVietHung_2280601326.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.BookVideo;

@Repository
public interface BookVideoRepository extends JpaRepository<BookVideo, Long> {
    Optional<BookVideo> findByBookId(Long bookId);
    void deleteByBookId(Long bookId);
}
