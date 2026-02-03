package com.example.TranVietHung_2280601326.services;

import java.util.List;

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
}
