package com.example.TranVietHung_2280601326.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.TranVietHung_2280601326.services.CartService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    
    @GetMapping
    public String showCart(HttpSession session, @NotNull Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        model.addAttribute("totalQuantity", cartService.getSumQuantity(session));
        return "book/cart";
    }
    
    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(HttpSession session, @PathVariable Long id) {
        var cart = cartService.getCart(session);
        cart.removeItems(id);
        return "redirect:/cart";
    }
    
    @GetMapping("/updateCart/{id}/{quantity}")
    public String updateCart(HttpSession session, @PathVariable Long id, @PathVariable int quantity) {
        var cart = cartService.getCart(session);
        cart.updateItems(id, quantity);
        return "redirect:/cart";
    }
    
    @GetMapping("/clearCart")
    public String clearCart(HttpSession session) {
        cartService.removeCart(session);
        return "redirect:/cart";
    }
    
    // Checkout - Thanh toan gio hang
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        var cart = cartService.getCart(session);
        
        // Kiem tra gio hang co trong khong
        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }
        
        // Luu gio hang thanh hoa don va xoa gio hang
        cartService.saveCart(session);
        
        // Chuyen den trang xac nhan thanh cong
        return "redirect:/cart?checkout=success";
    }
}
