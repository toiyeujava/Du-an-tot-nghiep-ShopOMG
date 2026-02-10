package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CartController - Handles shopping cart CRUD operations.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why does CartController directly use repositories (AccountRepository,
 * ProductVariantRepository)?"
 *
 * These are used only in private helper methods:
 * - getCurrentAccountId(): resolves the logged-in user's ID from
 * SecurityContext
 * - findVariantId(): resolves a variant from product+color+size
 *
 * These are lightweight lookups, not business logic, so keeping them here
 * avoids over-engineering. If the auth resolution becomes complex, we could
 * extract a shared AuthenticationHelper service.
 *
 * "Why updateQuantity() returns JSON (ResponseEntity) while others redirect?"
 * - updateQuantity is called via AJAX (JavaScript fetch)
 * - The cart page updates quantities without full page reload
 * - Other operations (add, remove, clear) still use traditional form POST +
 * redirect
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AccountRepository accountRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Display cart page.
     */
    @GetMapping
    public String viewCart(Model model) {
        Integer accountId = getCurrentAccountId();
        if (accountId == null)
            return "redirect:/login";

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
     * Add product to cart.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Integer productId,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Integer accountId = getCurrentAccountId();

        if (accountId == null) {
            Map<String, Object> pendingAction = new HashMap<>();
            pendingAction.put("productId", productId);
            pendingAction.put("color", color);
            pendingAction.put("size", size);
            pendingAction.put("variantId", variantId);
            pendingAction.put("quantity", quantity);
            session.setAttribute("pendingCartAction", pendingAction);
            session.setAttribute("redirectAfterLogin", "/product/" + productId);

            redirectAttributes.addFlashAttribute("info",
                    "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng và trải nghiệm mua sắm tuyệt vời! " +
                            "Đăng ký ngay để nhận giảm 20% cho đơn hàng đầu tiên.");
            return "redirect:/login";
        }

        try {
            Integer finalVariantId = null;
            if (variantId != null) {
                finalVariantId = variantId;
            } else if (color != null && size != null) {
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
     * Update cart item quantity (AJAX endpoint).
     */
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable("id") Integer cartId,
            @RequestParam("quantity") Integer quantity) {

        Integer accountId = getCurrentAccountId();
        Map<String, Object> response = new HashMap<>();

        if (accountId == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Cart cart = cartService.updateQuantity(cartId, accountId, quantity);

            double price = cart.getProductVariant().getProduct().getPrice();
            int discount = cart.getProductVariant().getProduct().getDiscount() != null
                    ? cart.getProductVariant().getProduct().getDiscount()
                    : 0;
            double finalPrice = price * (100 - discount) / 100.0;
            double itemTotal = finalPrice * cart.getQuantity();

            response.put("success", true);
            response.put("newQuantity", cart.getQuantity());
            response.put("newItemTotal", itemTotal);
            response.put("cartItemCount", cartService.getCartItemCount(accountId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Remove item from cart.
     */
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") Integer cartId,
            RedirectAttributes redirectAttributes) {
        Integer accountId = getCurrentAccountId();
        if (accountId == null)
            return "redirect:/login";

        try {
            cartService.removeFromCart(cartId, accountId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    /**
     * Clear entire cart.
     */
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        Integer accountId = getCurrentAccountId();
        if (accountId == null)
            return "redirect:/login";

        try {
            cartService.clearCart(accountId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    // ===== PRIVATE HELPERS =====

    private Integer getCurrentAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String principal = auth.getName();
        Account account = accountRepository.findByEmail(principal).orElse(null);
        if (account == null) {
            account = accountRepository.findByUsername(principal).orElse(null);
        }
        return account != null ? account.getId() : null;
    }

    private Integer findVariantId(Integer productId, String color, String size) {
        Optional<ProductVariant> variant = productVariantRepository
                .findByProductIdAndColorAndSize(productId, color, size);
        return variant.map(ProductVariant::getId).orElse(null);
    }
}
