package com.example.TranVietHung_2280601326.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.TranVietHung_2280601326.models.Book;

@Service
public class BookService {
    private final List<Book> books;

    public BookService(List<Book> books) {
        this.books = books;
    }

    public List<Book> getAllBooks() {
        return books;
    }

    // Them sach moi
    public void addBook(Book book) {
        books.add(book);
    }

    // Xoa sach theo ID
    public void deleteBookById(Long id) {
        books.removeIf(book -> book.getId().equals(id));
    }

    // Sua thong tin sach
    public void updateBook(Book updatedBook) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(updatedBook.getId())) {
                books.set(i, updatedBook);
                return;
            }
        }
    }

    // Hien thi chi tiet sach theo ID
    public Book getBookById(Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
