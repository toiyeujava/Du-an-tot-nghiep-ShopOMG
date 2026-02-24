package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.Order;
import poly.edu.repository.AccountRepository;
import poly.edu.service.AccountService;
import poly.edu.service.CartService;
import poly.edu.service.OrderCommandService;

import java.security.Principal;
import java.util.List;

/**
 * CheckoutController - Handles the checkout flow.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why separate checkout from HomeController?"
 *
 * Checkout is a write-heavy, transactional operation that requires:
 * - Authentication (user must be logged in)
 * - Cart data (read cart → create order → clear cart)
 * - Order creation (complex: stock check, payment, address)
 *
 * This is fundamentally different from the read-only homepage/shop pages.
 * Mixing them in one controller violates SRP and makes testing hard.
 *
 * "Why does this depend on both CartService and OrderCommandService?"
 * - The checkout flow bridges two domains:
 * Cart (what the user wants) → Order (what the user committed to)
 * - This controller is the orchestrator between the two
 */
@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderCommandService orderCommandService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    /**
     * Display checkout page with cart summary.
     */
    @GetMapping("/checkout")
    public String checkout(@RequestParam(value = "source", required = false) String source,
            Model model, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        Account account = getAuthenticatedAccount(auth);

        if (account == null) {
            return "redirect:/login";
        }

        // "buynow" source = Buy Now flow (single product from sessionStorage).
        // Anything else = Cart checkout flow.
        boolean isBuyNow = "buynow".equals(source);

        List<Cart> cartItems = isBuyNow ? java.util.Collections.emptyList()
                : cartService.getCartItems(account.getId());
        Double cartTotal = isBuyNow ? 0.0 : cartService.getCartTotal(account.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("pageTitle", "Thanh toán");
        model.addAttribute("checkoutMode", isBuyNow ? "BUY_NOW" : "CART");
        return "user/checkout";
    }

    /**
     * Process checkout - create order from cart or buy-now.
     *
     * Algorithm:
     * 1. Authenticate user
     * 2. Determine flow: Buy Now (single variant) or Cart Checkout (multiple items)
     * 3. Create order via OrderCommandService
     * 4. Clear cart (only for cart checkout)
     * 5. Redirect to success page
     *
     * "Why two checkout paths?"
     * - Buy Now: skips cart, instant purchase (better UX for impulse buys)
     * - Cart: traditional multi-item checkout
     * - Both create the same Order entity, just different input sources
     */
    @PostMapping("/checkout/process")
    public String processCheckout(
            @RequestParam(value = "selectedRecipientName", required = false) String recipientName,
            @RequestParam(value = "selectedPhone", required = false) String phone,
            @RequestParam(value = "selectedAddress", required = false) String address,
            @RequestParam(value = "buyNowVariantId", required = false) Integer buyNowVariantId,
            @RequestParam(value = "buyNowQuantity", required = false) Integer buyNowQuantity,
            Principal principal,
            Model model) {

        Account account = getAuthenticatedAccount(principal);
        if (account == null) {
            return "redirect:/login";
        }

        try {
            Order order;
            if (buyNowVariantId != null && buyNowQuantity != null) {
                order = orderCommandService.createOrderFromVariant(account, buyNowVariantId, buyNowQuantity,
                        recipientName, phone, address);
            } else {
                List<Cart> cartItems = cartService.getCartItems(account.getId());
                if (cartItems == null || cartItems.isEmpty()) {
                    return "redirect:/checkout?error=empty";
                }
                order = orderCommandService.createOrder(account, cartItems, recipientName, phone, address);
                cartService.clearCart(account.getId());
            }

            String maskedPhone = maskPhoneNumber(phone);
            model.addAttribute("pageTitle", "Đặt hàng thành công");
            model.addAttribute("recipientName", recipientName != null ? recipientName : "Chưa chọn địa chỉ");
            model.addAttribute("phone", maskedPhone);
            model.addAttribute("fullAddress", address != null ? address : "");
            model.addAttribute("orderId", order.getId());
            return "user/order-success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/checkout?error=stock";
        }
    }

    // ===== PRIVATE HELPERS =====

    private Account getAuthenticatedAccount(Principal principal) {
        if (principal == null)
            return null;
        String identifier = principal.getName();
        Account acc = accountRepository.findByUsername(identifier).orElse(null);
        if (acc == null) {
            acc = accountService.findByEmail(identifier);
        }
        return acc;
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty())
            return "";
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() < 4)
            return phone;
        String first2 = digitsOnly.substring(0, 2);
        String last2 = digitsOnly.substring(digitsOnly.length() - 2);
        int asteriskCount = digitsOnly.length() - 4;
        String asterisks = "*".repeat(Math.max(0, asteriskCount));
        return "(+84)" + first2 + asterisks + last2;
    }
}
