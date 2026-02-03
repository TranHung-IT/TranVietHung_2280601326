package com.example.TranVietHung_2280601326.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.TranVietHung_2280601326.daos.Item;
import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.services.BookService;
import com.example.TranVietHung_2280601326.services.CartService;
import com.example.TranVietHung_2280601326.services.CategoryService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
@Validated
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;
    private final CartService cartService;

    @GetMapping
    public String showAllBooks(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        model.addAttribute("books", bookService.getAllBooks(pageNo, pageSize, sortBy));
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalPages", bookService.getAllBooks(pageNo, pageSize, sortBy).size() / pageSize);
        return "book/list";
    }

    // Them sach moi - Hien thi form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    // Them sach moi - Xu ly form
    @PostMapping("/add")
    public String addBook(@Valid Book book, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }
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
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        return "redirect:/books";
    }

    // Sua thong tin sach - Xu ly form
    @PostMapping("/edit")
    public String updateBook(@Valid Book book, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
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
    
    // Them sach vao gio hang
    @PostMapping("/add-to-cart")
    public String addToCart(HttpSession session, 
                           @RequestParam long id, 
                           @RequestParam String name, 
                           @RequestParam double price, 
                           @RequestParam(defaultValue = "1") int quantity) {
        var cart = cartService.getCart(session);
        cart.addItems(new Item(id, name, price, quantity));
        cartService.updateCart(session, cart);
        return "redirect:/books";
    }
    
    // Tim kiem sach theo tu khoa
    @GetMapping("/search")
    public String searchBooks(@RequestParam("keyword") String keyword, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/books";
        }
        model.addAttribute("books", bookService.searchBooks(keyword));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        return "book/list";
    }
}