package com.example.TranVietHung_2280601326.viewmodels;

import java.util.List;
import java.util.stream.Collectors;

import com.example.TranVietHung_2280601326.daos.Cart;
import com.example.TranVietHung_2280601326.daos.Item;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CartVm(
    List<CartItemVm> items,
    Integer totalQuantity,
    Double totalPrice
) {
    public static CartVm from(@NotNull Cart cart) {
        List<CartItemVm> items = cart.getCartItems().stream()
            .map(CartItemVm::from)
            .collect(Collectors.toList());
            
        int totalQuantity = cart.getCartItems().stream()
            .mapToInt(Item::getQuantity)
            .sum();
            
        double totalPrice = cart.getCartItems().stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
            
        return new CartVm(items, totalQuantity, totalPrice);
    }
    
    @Builder
    public record CartItemVm(
        Long bookId,
        String bookName,
        Integer quantity,
        Double price,
        Double subtotal
    ) {
        public static CartItemVm from(@NotNull Item item) {
            return new CartItemVm(
                item.getBookId(),
                item.getBookName(),
                item.getQuantity(),
                item.getPrice(),
                item.getPrice() * item.getQuantity()
            );
        }
    }
}
