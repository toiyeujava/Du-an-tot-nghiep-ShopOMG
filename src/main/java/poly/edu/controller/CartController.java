package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.ProductVariantRepository;
import poly.edu.service.CartService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    /**
     * Hiển thị trang giỏ hàng
     */
    @GetMapping
    public String viewCart(Model model) {
        Integer accountId = getCurrentAccountId();
        
        if (accountId == null) {
            return "redirect:/login";
        }

        List<Cart> cartItems = cartService.getCartItems(accountId);
        Double cartTotal = cartService.getCartTotal(accountId);
        Long itemCount = cartService.getCartItemCount(accountId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("pageTitle", "Giỏ hàng");

        return "user/cart";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Integer productId,
                           @RequestParam(value = "color", required = false) String color,
                           @RequestParam(value = "size", required = false) String size,
                           @RequestParam(value = "variantId", required = false) Integer variantId,
                           @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                           RedirectAttributes redirectAttributes) {
        
        Integer accountId = getCurrentAccountId();
        
        if (accountId == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/login";
        }

        try {
            Integer finalVariantId = null;

            // Case 1: Direct Variant ID provided
            if (variantId != null) {
                finalVariantId = variantId;
            } 
            // Case 2: Color and Size provided
            else if (color != null && size != null) {
                finalVariantId = findVariantId(productId, color, size);
            }
            
            if (finalVariantId == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn màu sắc và kích thước hợp lệ");
                return "redirect:/product/" + productId;
            }

            cartService.addToCart(accountId, finalVariantId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/" + productId;
        }

        return "redirect:/cart";
    }

    /**
     * Cập nhật số lượng sản phẩm
     */
    /**
     * Cập nhật số lượng sản phẩm (AJAX)
     */
    @PostMapping("/update/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> updateQuantity(@PathVariable("id") Integer cartId,
                                 @RequestParam("quantity") Integer quantity) {
        
        Integer accountId = getCurrentAccountId();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (accountId == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return org.springframework.http.ResponseEntity.status(401).body(response);
        }

        try {
            Cart cart = cartService.updateQuantity(cartId, accountId, quantity);
            
            // Calculate new item total price (with discount)
            double price = cart.getProductVariant().getProduct().getPrice();
            int discount = cart.getProductVariant().getProduct().getDiscount() != null ? cart.getProductVariant().getProduct().getDiscount() : 0;
            double finalPrice = price * (100 - discount) / 100.0;
            double itemTotal = finalPrice * cart.getQuantity();

            response.put("success", true);
            response.put("newQuantity", cart.getQuantity());
            response.put("newItemTotal", itemTotal);
            response.put("cartItemCount", cartService.getCartItemCount(accountId));
            
            return org.springframework.http.ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Integer cartId,
                                 RedirectAttributes redirectAttributes) {
        
        Integer accountId = getCurrentAccountId();
        
        if (accountId == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeFromCart(cartId, accountId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        Integer accountId = getCurrentAccountId();
        
        if (accountId == null) {
            return "redirect:/login";
        }

        try {
            cartService.clearCart(accountId);
            // Removed success message as per user request
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    // ===== HELPER METHODS =====

    /**
     * Lấy ID tài khoản hiện tại từ Security Context
     */
    private Integer getCurrentAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            System.out.println("DEBUG: User not authenticated");
            return null;
        }

        String principal = auth.getName();
        System.out.println("DEBUG: Principal = " + principal);
        
        // Try to find by email first (since login uses email)
        Account account = accountRepository.findByEmail(principal).orElse(null);
        
        // If not found, try username
        if (account == null) {
            account = accountRepository.findByUsername(principal).orElse(null);
        }
        
        if (account != null) {
            System.out.println("DEBUG: Found account ID = " + account.getId());
            return account.getId();
        } else {
            System.out.println("DEBUG: Account not found for principal: " + principal);
            return null;
        }
    }

    /**
     * Tìm variant ID dựa trên productId, color, size
     */
    private Integer findVariantId(Integer productId, String color, String size) {
        Optional<ProductVariant> variant = productVariantRepository
                .findByProductIdAndColorAndSize(productId, color, size);
        return variant.map(ProductVariant::getId).orElse(null);
    }
}
