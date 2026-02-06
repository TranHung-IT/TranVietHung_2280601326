package com.example.TranVietHung_2280601326.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.TranVietHung_2280601326.daos.Cart;
import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.models.Category;
import com.example.TranVietHung_2280601326.models.Invoice;
import com.example.TranVietHung_2280601326.services.BookService;
import com.example.TranVietHung_2280601326.services.CartService;
import com.example.TranVietHung_2280601326.services.CategoryService;
import com.example.TranVietHung_2280601326.viewmodels.BookGetVm;
import com.example.TranVietHung_2280601326.viewmodels.BookPostVm;
import com.example.TranVietHung_2280601326.viewmodels.CartVm;
import com.example.TranVietHung_2280601326.viewmodels.CategoryVm;
import com.example.TranVietHung_2280601326.viewmodels.InvoiceVm;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Validated
public class ApiController {
    
    private final BookService bookService;
    private final CategoryService categoryService;
    private final CartService cartService;
    
    /**
     * GET /api/v1/books - Lấy danh sách tất cả sách
     */
    @GetMapping("/books")
    public ResponseEntity<List<BookGetVm>> getAllBooks(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy) {
        
        List<Book> books = bookService.getAllBooks(pageNumber, pageSize, sortBy);
        List<BookGetVm> bookVms = books.stream()
                .map(BookGetVm::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookVms);
    }
    
    /**
     * GET /api/v1/books/{id} - Lấy chi tiết sách theo ID
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<BookGetVm> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(BookGetVm.from(book));
    }
    
    /**
     * GET /api/v1/books/search - Tìm kiếm sách theo từ khóa
     */
    @GetMapping("/books/search")
    public ResponseEntity<List<BookGetVm>> searchBooks(@RequestParam String keyword) {
        List<Book> books = bookService.searchBooks(keyword);
        List<BookGetVm> bookVms = books.stream()
                .map(BookGetVm::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookVms);
    }
    
    /**
     * POST /api/v1/books - Tạo mới sách
     */
    @PostMapping("/books")
    public ResponseEntity<?> createBook(@Valid @RequestBody BookPostVm bookPostVm) {
        try {
            // Validate category exists
            Category category = categoryService.getCategoryById(bookPostVm.categoryId());
            if (category == null) {
                return ResponseEntity.badRequest()
                        .body("Category with ID " + bookPostVm.categoryId() + " not found");
            }
            
            // Create new book
            Book book = new Book();
            book.setTitle(bookPostVm.title());
            book.setAuthor(bookPostVm.author());
            book.setPrice(bookPostVm.price());
            book.setCategory(category);
            
            Book savedBook = bookService.addBook(book);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(BookGetVm.from(savedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating book: " + e.getMessage());
        }
    }
    
    /**
     * PUT /api/v1/books/{id} - Cập nhật sách
     */
    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookPostVm bookPostVm) {
        try {
            // Check if book exists
            Book existingBook = bookService.getBookById(id);
            if (existingBook == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Validate category exists
            Category category = categoryService.getCategoryById(bookPostVm.categoryId());
            if (category == null) {
                return ResponseEntity.badRequest()
                        .body("Category with ID " + bookPostVm.categoryId() + " not found");
            }
            
            // Update book fields
            existingBook.setTitle(bookPostVm.title());
            existingBook.setAuthor(bookPostVm.author());
            existingBook.setPrice(bookPostVm.price());
            existingBook.setCategory(category);
            
            Book updatedBook = bookService.updateBook(existingBook);
            
            return ResponseEntity.ok(BookGetVm.from(updatedBook));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating book: " + e.getMessage());
        }
    }
    
    /**
     * DELETE /api/v1/books/{id} - Xóa sách theo ID
     */
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            
            bookService.deleteBookById(id);
            return ResponseEntity.ok().body("Book deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting book: " + e.getMessage());
        }
    }
    
    // ==================== CATEGORY ENDPOINTS ====================
    
    /**
     * GET /api/v1/categories - Lấy danh sách tất cả categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryVm>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryVm> categoryVms = categories.stream()
                .map(CategoryVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryVms);
    }
    
    /**
     * GET /api/v1/categories/{id} - Lấy chi tiết category theo ID
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryVm> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(CategoryVm.from(category));
    }
    
    // ==================== CART ENDPOINTS ====================
    
    /**
     * GET /api/v1/cart - Lấy thông tin giỏ hàng hiện tại
     */
    @GetMapping("/cart")
    public ResponseEntity<CartVm> getCart(HttpSession session) {
        Cart cart = cartService.getCart(session);
        return ResponseEntity.ok(CartVm.from(cart));
    }
    
    /**
     * GET /api/v1/cart/summary - Lấy thống kê giỏ hàng (số lượng, tổng giá)
     */
    @GetMapping("/cart/summary")
    public ResponseEntity<?> getCartSummary(HttpSession session) {
        int totalQuantity = cartService.getSumQuantity(session);
        double totalPrice = cartService.getSumPrice(session);
        
        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("totalQuantity", totalQuantity);
            put("totalPrice", totalPrice);
        }});
    }
    
    // ==================== ORDER/INVOICE ENDPOINTS ====================
    
    /**
     * GET /api/v1/orders - Lấy danh sách tất cả đơn hàng (Admin)
     */
    @GetMapping("/orders")
    public ResponseEntity<List<InvoiceVm>> getAllOrders() {
        List<Invoice> invoices = cartService.getAllInvoices();
        List<InvoiceVm> invoiceVms = invoices.stream()
                .map(InvoiceVm::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceVms);
    }
    
    /**
     * GET /api/v1/orders/{id} - Lấy chi tiết đơn hàng theo ID
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<InvoiceVm> getOrderById(@PathVariable Long id) {
        Invoice invoice = cartService.getInvoiceById(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(InvoiceVm.from(invoice));
    }
    
    /**
     * GET /api/v1/orders/stats - Lấy thống kê đơn hàng (Admin)
     */
    @GetMapping("/orders/stats")
    public ResponseEntity<?> getOrderStats() {
        List<Invoice> invoices = cartService.getAllInvoices();
        
        double totalRevenue = invoices.stream()
                .mapToDouble(Invoice::getTotal)
                .sum();
        
        long uniqueCustomers = invoices.stream()
                .filter(inv -> inv.getCustomerEmail() != null)
                .map(Invoice::getCustomerEmail)
                .distinct()
                .count();
        
        int totalProducts = invoices.stream()
                .flatMap(inv -> inv.getItemInvoices().stream())
                .mapToInt(item -> item.getQuantity())
                .sum();
        
        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("totalOrders", invoices.size());
            put("totalRevenue", totalRevenue);
            put("uniqueCustomers", uniqueCustomers);
            put("totalProducts", totalProducts);
        }});
    }
}
