package com.example.TranVietHung_2280601326.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.TranVietHung_2280601326.models.Invoice;
import com.example.TranVietHung_2280601326.repositories.IInvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class InvoiceService {
    private final IInvoiceRepository invoiceRepository;
    
    // Lay tat ca hoa don
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
    // Lay hoa don theo ID
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }
    
    // Dem tong so hoa don
    public long countAllInvoices() {
        return invoiceRepository.count();
    }
    
    // Tinh tong doanh thu
    public double getTotalRevenue() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .mapToDouble(inv -> inv.getTotal())
                .sum();
    }
    
    // Tinh doanh thu theo ngay
    public double getRevenueByDate(LocalDate date) {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                        inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(date))
                .mapToDouble(inv -> inv.getTotal())
                .sum();
    }
    
    // Dem so hoa don theo ngay
    public long countInvoicesByDate(LocalDate date) {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                        inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(date))
                .count();
    }
    
    // Bao cao ban hang theo khoang thoi gian
    public Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Invoice> filteredInvoices = invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(startDate) &&
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(endDate))
                .toList();
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalOrders", filteredInvoices.size());
        report.put("totalRevenue", filteredInvoices.stream()
                .mapToDouble(inv -> inv.getTotal()).sum());
        return report;
    }
    
    // Tinh doanh thu theo khoang thoi gian
    public double getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(startDate) &&
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(endDate))
                .mapToDouble(inv -> inv.getTotal())
                .sum();
    }
    
    // Dem so hoa don theo khoang thoi gian
    public long countInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(startDate) &&
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).   toLocalDate().isAfter(endDate))
                .count();
    }
    
    // Lay du lieu ban hang theo ngay
    public Map<LocalDate, Double> getDailySales(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        Map<LocalDate, Double> dailySales = new HashMap<>();
        
        invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && 
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(startDate) &&
                        !inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(endDate))
                .forEach(inv -> {
                    LocalDate date = inv.getInvoiceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    dailySales.merge(date, inv.getTotal(), Double::sum);
                });
        
        return dailySales;
    }
}
