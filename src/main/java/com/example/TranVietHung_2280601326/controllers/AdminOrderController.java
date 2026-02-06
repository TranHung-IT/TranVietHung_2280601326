package com.example.TranVietHung_2280601326.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.TranVietHung_2280601326.services.CartService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminOrderController {
    private final CartService cartService;
    
    // Hien thi danh sach tat ca don hang
    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String payment,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model) {
        
        var allInvoices = cartService.getAllInvoices();
        
        // Loc theo cac tieu chi
        var invoices = allInvoices.stream()
            .filter(invoice -> {
                // Loc theo tim kiem (ten hoac sdt)
                if (search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    boolean matchName = invoice.getCustomerName() != null && 
                                       invoice.getCustomerName().toLowerCase().contains(searchLower);
                    boolean matchPhone = invoice.getCustomerPhone() != null && 
                                        invoice.getCustomerPhone().contains(search);
                    if (!matchName && !matchPhone) {
                        return false;
                    }
                }
                
                // Loc theo phuong thuc thanh toan
                if (payment != null && !payment.isEmpty()) {
                    if (!payment.equals(invoice.getPaymentMethod())) {
                        return false;
                    }
                }
                
                // Loc theo ngay (can implement neu muon)
                // TODO: Add date filtering if needed
                
                return true;
            })
            .toList();
        
        model.addAttribute("invoices", invoices);
        model.addAttribute("searchParam", search);
        model.addAttribute("paymentParam", payment);
        model.addAttribute("fromDateParam", fromDate);
        model.addAttribute("toDateParam", toDate);
        
        // Tinh toan thong ke dua tren danh sach da loc
        double totalRevenue = invoices.stream()
            .mapToDouble(invoice -> invoice.getTotal())
            .sum();
        
        long uniqueCustomers = invoices.stream()
            .map(invoice -> invoice.getCustomerPhone())
            .distinct()
            .count();
        
        int totalProducts = invoices.stream()
            .flatMap(invoice -> invoice.getItemInvoices().stream())
            .mapToInt(item -> item.getQuantity())
            .sum();
        
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("uniqueCustomers", uniqueCustomers);
        model.addAttribute("totalProducts", totalProducts);
        
        return "admin/orders/list";
    }
    
    // Xem chi tiet don hang
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        var invoice = cartService.getInvoiceById(id);
        if (invoice == null) {
            return "redirect:/admin/orders";
        }
        model.addAttribute("invoice", invoice);
        return "admin/orders/detail";
    }
    
    // Cap nhat trang thai don hang
    @PostMapping("/{id}/update-status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        
        try {
            cartService.updateInvoiceStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }
}
