package com.example.TranVietHung_2280601326.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.daos.Cart;
import com.example.TranVietHung_2280601326.daos.Item;
import com.example.TranVietHung_2280601326.models.Invoice;
import com.example.TranVietHung_2280601326.models.ItemInvoice;
import com.example.TranVietHung_2280601326.models.User;
import com.example.TranVietHung_2280601326.repositories.IBookRepository;
import com.example.TranVietHung_2280601326.repositories.IInvoiceRepository;
import com.example.TranVietHung_2280601326.repositories.IItemInvoiceRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class CartService {
    private static final String CART_SESSION_KEY = "cart";
    
    private final IInvoiceRepository invoiceRepository;
    private final IItemInvoiceRepository itemInvoiceRepository;
    private final IBookRepository bookRepository;
    
    public Cart getCart(@NotNull HttpSession session) {
        return Optional.ofNullable((Cart) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return cart;
                });
    }
    
    public void updateCart(@NotNull HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }
    
    public void removeCart(@NotNull HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
    
    public int getSumQuantity(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }
    
    public double getSumPrice(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    // Luu gio hang thanh hoa don va tra ve invoice
    public Invoice saveCart(@NotNull HttpSession session) {
        var cart = getCart(session);
        if (cart.getCartItems().isEmpty()) return null;
        
        var invoice = new Invoice();
        invoice.setInvoiceDate(new Date(new Date().getTime()));
        invoice.setTotal(getSumPrice(session));
        invoiceRepository.save(invoice);
        
        cart.getCartItems().forEach(item -> {
            var items = new ItemInvoice();
            items.setInvoice(invoice);
            items.setQuantity(item.getQuantity());
            items.setBook(bookRepository.findById(item.getBookId()).orElseThrow());
            itemInvoiceRepository.save(items);
            invoice.getItemInvoices().add(items); // Thêm vào collection để hiển thị
        });
        
        removeCart(session);
        return invoice;
    }
    
    // Luu gio hang voi thong tin khach hang
    public Invoice saveCartWithCustomerInfo(@NotNull HttpSession session, 
                                           String customerName,
                                           String customerPhone,
                                           String customerEmail,
                                           String customerAddress,
                                           String paymentMethod) {
        var cart = getCart(session);
        if (cart.getCartItems().isEmpty()) return null;
        
        System.out.println("=== DEBUG: Cart has " + cart.getCartItems().size() + " items ===");
        cart.getCartItems().forEach(item -> {
            System.out.println("Item - BookID: " + item.getBookId() + ", Quantity: " + item.getQuantity());
        });
        
        var invoice = new Invoice();
        invoice.setInvoiceDate(new Date());
        invoice.setTotal(getSumPrice(session));
        invoice.setCustomerName(customerName);
        invoice.setCustomerPhone(customerPhone);
        invoice.setCustomerEmail(customerEmail);
        invoice.setCustomerAddress(customerAddress);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus("PENDING");
        
        // Lay user tu security context neu co
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                invoice.setUser((User) authentication.getPrincipal());
            }
        } catch (Exception e) {
            // Ignore if user not found
        }
        
        // Save invoice first to get ID
        invoice = invoiceRepository.save(invoice);
        System.out.println("=== DEBUG: Invoice saved with ID: " + invoice.getId() + " ===");
        
        // Now save all items
        int savedCount = 0;
        for (Item item : cart.getCartItems()) {
            var itemInvoice = new ItemInvoice();
            itemInvoice.setInvoice(invoice);
            itemInvoice.setQuantity(item.getQuantity());
            itemInvoice.setBook(bookRepository.findById(item.getBookId()).orElseThrow());
            itemInvoiceRepository.save(itemInvoice);
            savedCount++;
            System.out.println("=== DEBUG: Saved ItemInvoice #" + savedCount + " for BookID: " + item.getBookId() + " ===");
        }
        
        System.out.println("=== DEBUG: Total ItemInvoices saved: " + savedCount + " ===");
        
        removeCart(session);
        return invoice;
    }
    
    // Lay invoice theo ID
    public Invoice getInvoiceById(Long id) {
        var invoice = invoiceRepository.findById(id).orElse(null);
        if (invoice != null) {
            // Force load itemInvoices and books to avoid lazy loading issues
            invoice.getItemInvoices().size();
            invoice.getItemInvoices().forEach(item -> {
                if (item.getBook() != null) {
                    item.getBook().getTitle(); // Touch to load
                }
            });
        }
        return invoice;
    }
    
    // Lay danh sach invoice cua user
    public List<Invoice> getInvoicesByUser(User user) {
        return invoiceRepository.findByUserOrderByInvoiceDateDesc(user);
    }
    
    // Lay tat ca invoice (cho admin)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByInvoiceDateDesc();
    }
    
    // Cap nhat trang thai don hang
    public void updateInvoiceStatus(Long id, String status) {
        var invoice = invoiceRepository.findById(id).orElseThrow();
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }
}
