package com.example.TranVietHung_2280601326.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.TranVietHung_2280601326.models.ItemInvoice;

@Repository
public interface IItemInvoiceRepository extends JpaRepository<ItemInvoice, Long> {
    
    @Modifying
    @Query("UPDATE ItemInvoice i SET i.book = null WHERE i.book.id = :bookId")
    void setBookToNullByBookId(@Param("bookId") Long bookId);
}
