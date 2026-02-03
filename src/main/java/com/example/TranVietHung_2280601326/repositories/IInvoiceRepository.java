package com.example.TranVietHung_2280601326.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.Invoice;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {
}
