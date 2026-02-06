package com.example.TranVietHung_2280601326.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.Invoice;
import com.example.TranVietHung_2280601326.models.User;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUserOrderByInvoiceDateDesc(User user);
    List<Invoice> findAllByOrderByInvoiceDateDesc();
}
