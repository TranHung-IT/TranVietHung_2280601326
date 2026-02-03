package com.example.TranVietHung_2280601326.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.repositories.IBookRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class BookService {
    private final IBookRepository bookRepository;

    public List<Book> getAllBooks(Integer pageNumber,
                                Integer pageSize,
                                String sortBy){
        return StreamSupport.stream(bookRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    // Them sach moi
    public void addBook(Book book) {
        bookRepository.save(book);
    }

    // Xoa sach theo ID
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    // Sua thong tin sach
    public void updateBook(@NonNull Book book) {
        Book existingBook = bookRepository.findById(book.getId())
            .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + book.getId()));
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        bookRepository.save(existingBook);
    }

    // Hien thi chi tiet sach theo ID
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    // Tim kiem sach theo tu khoa
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword);
    }
}
