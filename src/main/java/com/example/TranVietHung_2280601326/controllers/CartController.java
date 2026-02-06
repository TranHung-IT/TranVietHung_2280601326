package com.example.TranVietHung_2280601326.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.TranVietHung_2280601326.models.User;
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
    
    // Checkout - Hien thi trang thanh toan
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        var cart = cartService.getCart(session);
        
        // Kiem tra gio hang co trong khong
        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }
        
        // Lay thong tin user neu da dang nhap
        User user = null;
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // User not logged in
        }
        
        // Truyen thong tin sang view
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        model.addAttribute("totalQuantity", cartService.getSumQuantity(session));
        model.addAttribute("user", user);
        
        return "book/checkout";
    }
    
    // Xu ly thanh toan
    @PostMapping("/process-checkout")
    public String processCheckout(
            HttpSession session,
            @RequestParam String customerName,
            @RequestParam String customerPhone,
            @RequestParam String customerEmail,
            @RequestParam String customerAddress,
            @RequestParam String paymentMethod,
            RedirectAttributes redirectAttributes) {
        
        var cart = cartService.getCart(session);
        
        // Kiem tra gio hang co trong khong
        if (cart.getCartItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/cart";
        }
        
        // Luu thong tin khach hang va phuong thuc thanh toan
        var invoice = cartService.saveCartWithCustomerInfo(
            session, 
            customerName, 
            customerPhone, 
            customerEmail, 
            customerAddress, 
            paymentMethod
        );
        
        // Thong bao thanh cong
        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
        
        // Chuyen den trang chi tiet don hang
        return "redirect:/cart/order/" + invoice.getId();
    }
    
    // Xem chi tiet don hang
    @GetMapping("/order/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        var invoice = cartService.getInvoiceById(id);
        
        if (invoice == null) {
            return "redirect:/cart/orders";
        }
        
        // Lay user hien tai
        User user = null;
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        // Kiem tra quyen truy cap (chi user so huu hoac admin moi xem duoc)
        if (user != null && invoice.getUser() != null && 
            !invoice.getUser().getId().equals(user.getId()) && 
            user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ADMIN"))) {
            return "redirect:/cart/orders";
        }
        
        model.addAttribute("invoice", invoice);
        return "book/order-detail";
    }
    
    // Xem lich su don hang
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        // Lay user hien tai
        User user = null;
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                user = (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // User not logged in
        }
        
        if (user == null) {
            return "redirect:/login";
        }
        
        var invoices = cartService.getInvoicesByUser(user);
        model.addAttribute("invoices", invoices);
        return "book/order-history";
    }
}
