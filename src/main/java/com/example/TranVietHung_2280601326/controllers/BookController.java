package com.example.TranVietHung_2280601326.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.services.BookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor

public class BookController {
    private final BookService bookService;

    @GetMapping
    public String showAllBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "book/list";
    }

    // Them sach moi - Hien thi form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        return "book/add";
    }

    // Them sach moi - Xu ly form
    @PostMapping("/add")
    public String addBook(Book book) {
        bookService.addBook(book);
        return "redirect:/books";
    }

    // Xoa sach theo ID
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return "redirect:/books";
    }

    // Sua thong tin sach - Hien thi form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book != null) {
            model.addAttribute("book", book);
            return "book/edit";
        }
        return "redirect:/books";
    }

    // Sua thong tin sach - Xu ly form
    @PostMapping("/edit")
    public String updateBook(Book book) {
        bookService.updateBook(book);
        return "redirect:/books";
    }

    // Hien thi chi tiet sach theo ID
    @GetMapping("/{id}")
    public String viewBookDetails(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book != null) {
            model.addAttribute("book", book);
            return "book/details";
        }
        return "redirect:/books";
    }
}
