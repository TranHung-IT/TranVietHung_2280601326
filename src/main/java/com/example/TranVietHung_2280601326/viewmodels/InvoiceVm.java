package com.example.TranVietHung_2280601326.viewmodels;

import com.example.TranVietHung_2280601326.models.Invoice;
import com.example.TranVietHung_2280601326.models.ItemInvoice;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record InvoiceVm(
    Long id,
    Date invoiceDate,
    Double total,
    String customerName,
    String customerPhone,
    String customerEmail,
    String customerAddress,
    String paymentMethod,
    String status,
    List<InvoiceItemVm> items
) {
    public static InvoiceVm from(@NotNull Invoice invoice) {
        List<InvoiceItemVm> items = invoice.getItemInvoices() != null 
            ? invoice.getItemInvoices().stream()
                .map(InvoiceItemVm::from)
                .collect(Collectors.toList())
            : List.of();
            
        return new InvoiceVm(
            invoice.getId(),
            invoice.getInvoiceDate(),
            invoice.getTotal(),
            invoice.getCustomerName(),
            invoice.getCustomerPhone(),
            invoice.getCustomerEmail(),
            invoice.getCustomerAddress(),
            invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : null,
            invoice.getStatus() != null ? invoice.getStatus().toString() : null,
            items
        );
    }
    
    @Builder
    public record InvoiceItemVm(
        Long id,
        Integer quantity,
        BookInInvoiceVm book
    ) {
        public static InvoiceItemVm from(@NotNull ItemInvoice itemInvoice) {
            return new InvoiceItemVm(
                itemInvoice.getId(),
                itemInvoice.getQuantity(),
                itemInvoice.getBook() != null ? BookInInvoiceVm.from(itemInvoice.getBook()) : null
            );
        }
    }
    
    @Builder
    public record BookInInvoiceVm(
        Long id,
        String title,
        String author,
        Double price
    ) {
        public static BookInInvoiceVm from(@NotNull com.example.TranVietHung_2280601326.models.Book book) {
            return new BookInInvoiceVm(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice()
            );
        }
    }
}
