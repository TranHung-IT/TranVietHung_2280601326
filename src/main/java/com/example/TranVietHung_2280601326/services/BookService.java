package com.example.TranVietHung_2280601326.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.repositories.IBookRepository;
import com.example.TranVietHung_2280601326.repositories.IItemInvoiceRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class BookService {
    private final IBookRepository bookRepository;
    private final IItemInvoiceRepository itemInvoiceRepository;

    public List<Book> getAllBooks(Integer pageNumber,
                                Integer pageSize,
                                String sortBy){
        return StreamSupport.stream(bookRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    // Them sach moi
    public Book addBook(Book book) {
        // Kiểm tra xem đã có sách với tên này chưa
        Book existingBook = bookRepository.findByTitleIgnoreCase(book.getTitle());
        
        if (existingBook != null) {
            // Nếu đã có sách trùng tên, cộng dồn số lượng
            int currentQuantity = existingBook.getQuantity() != null ? existingBook.getQuantity() : 0;
            int addQuantity = book.getQuantity() != null ? book.getQuantity() : 1;
            existingBook.setQuantity(currentQuantity + addQuantity);
            
            // Cập nhật thông tin khác nếu cần (giữ nguyên hoặc update)
            // Ở đây tôi sẽ giữ nguyên thông tin cũ, chỉ cộng quantity
            return bookRepository.save(existingBook);
        } else {
            // Nếu chưa có, đảm bảo quantity có giá trị
            if (book.getQuantity() == null || book.getQuantity() == 0) {
                book.setQuantity(1);
            }
            return bookRepository.save(book);
        }
    }

    // Xoa sach theo ID
    public void deleteBookById(Long id) {
        // Set book_id = null trong ItemInvoice để giữ lại lịch sử đơn hàng
        // Snapshot bookName và bookPrice đã được lưu khi tạo order
        itemInvoiceRepository.setBookToNullByBookId(id);
        
        // Xóa book - lúc này không còn reference từ ItemInvoice
        bookRepository.deleteById(id);
    }

    // Sua thong tin sach
    public Book updateBook(@NonNull Book book) {
        return bookRepository.save(book);
    }

    // Hien thi chi tiet sach theo ID
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    // Tim kiem sach theo tu khoa
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword);
    }
    
    // Admin reports methods
    public long countAllBooks() {
        return bookRepository.count();
    }
    
    public List<Object[]> getTopSellingBooks(int limit) {
        // Return empty list since we don't have sales tracking yet
        // In the future, join with invoice items to get actual sales data
        return List.of();
    }
    
    public Map<String, Long> countBooksByCategory() {
        List<Book> allBooks = bookRepository.findAll();
        Map<String, Long> categoryCount = new HashMap<>();
        
        for (Book book : allBooks) {
            if (book.getCategory() != null) {
                String categoryName = book.getCategory().getName();
                categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0L) + 1);
            } else {
                categoryCount.put("Uncategorized", categoryCount.getOrDefault("Uncategorized", 0L) + 1);
            }
        }
        
        return categoryCount;
    }
    
    public List<Book> getTopExpensiveBooks(int limit) {
        return bookRepository.findAll().stream()
            .sorted((b1, b2) -> Double.compare(b2.getPrice(), b1.getPrice()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<Book> getTopCheapBooks(int limit) {
        return bookRepository.findAll().stream()
            .sorted((b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public long countBooksWithoutImages() {
        return bookRepository.findAll().stream()
            .filter(book -> book.getImages() == null || book.getImages().isEmpty())
            .count();
    }
}
