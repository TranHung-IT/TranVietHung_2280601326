package com.example.TranVietHung_2280601326.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.TranVietHung_2280601326.services.BookService;
import com.example.TranVietHung_2280601326.services.CategoryService;
import com.example.TranVietHung_2280601326.services.InvoiceService;
import com.example.TranVietHung_2280601326.services.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ReportController {
    
    private final BookService bookService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final InvoiceService invoiceService;
    
    @GetMapping
    public String dashboard(Model model) {
        // Thống kê tổng quan
        long totalBooks = bookService.countAllBooks();
        long totalUsers = userService.countAllUsers();
        long totalCategories = categoryService.countAllCategories();
        long totalOrders = invoiceService.countAllInvoices();
        
        // Thống kê theo ngày
        LocalDateTime today = LocalDateTime.now();
        long todayOrders = invoiceService.countInvoicesByDate(today.toLocalDate());
        
        // Thống kê doanh thu
        double totalRevenue = invoiceService.getTotalRevenue();
        double todayRevenue = invoiceService.getRevenueByDate(today.toLocalDate());
        
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("todayOrders", todayOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("todayRevenue", todayRevenue);
        
        // Top selling books
        model.addAttribute("topBooks", bookService.getTopSellingBooks(5));
        
        // Books by category
        model.addAttribute("booksByCategory", bookService.countBooksByCategory());
        
        return "admin/reports/dashboard";
    }
    
    @GetMapping("/sales")
    public String salesReport(
            Model model,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        Map<String, Object> salesData = invoiceService.getSalesReport(start, end);
        double totalRevenue = invoiceService.getRevenueByDateRange(start, end);
        long totalOrders = invoiceService.countInvoicesByDateRange(start, end);
        
        model.addAttribute("salesData", salesData);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        
        return "admin/reports/sales";
    }
    
    @GetMapping("/books")
    public String booksReport(Model model) {
        model.addAttribute("booksByCategory", bookService.countBooksByCategory());
        model.addAttribute("topExpensiveBooks", bookService.getTopExpensiveBooks(10));
        model.addAttribute("topCheapBooks", bookService.getTopCheapBooks(10));
        model.addAttribute("booksWithoutImages", bookService.countBooksWithoutImages());
        
        return "admin/reports/books";
    }
    
    @GetMapping("/users")
    public String usersReport(Model model) {
        long totalUsers = userService.countAllUsers();
        long activeUsers = userService.countActiveUsers();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("userRegistrationStats", userService.getUserRegistrationStats());
        
        return "admin/reports/users";
    }
    
    @GetMapping("/api/sales-chart")
    @ResponseBody
    public Map<String, Object> getSalesChartData(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> chartData = new HashMap<>();
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        Map<LocalDate, Double> dailySales = invoiceService.getDailySales(startDate, endDate);
        
        chartData.put("labels", dailySales.keySet().stream().map(LocalDate::toString).toArray());
        chartData.put("data", dailySales.values().toArray());
        
        return chartData;
    }
}
