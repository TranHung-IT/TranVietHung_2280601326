package com.example.TranVietHung_2280601326.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.ItemInvoice;

@Repository
public interface IItemInvoiceRepository extends JpaRepository<ItemInvoice, Long> {
}
