package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.Order;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.VoucherRepository;
import poly.edu.service.AccountService;
import poly.edu.service.CartService;
import poly.edu.service.OrderCommandService;
import poly.edu.entity.Voucher;
import poly.edu.service.AddressService;
import poly.edu.dto.AddressDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.service.NotificationService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final NotificationService notificationService;
    private final AddressService addressService;

    /**
     * Display checkout page with cart summary.
     */
    @GetMapping("/checkout")
    public String checkout(@RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
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

        List<Cart> cartItems;
        Double cartTotal;

        if (isBuyNow) {
            cartItems = java.util.Collections.emptyList();
            cartTotal = 0.0;
        } else if (ids != null && !ids.isEmpty()) {
            cartItems = cartService.getCartItemsByIds(ids);
            cartTotal = cartService.getCartTotalByIds(ids);
        } else {
            cartItems = cartService.getCartItems(account.getId());
            cartTotal = cartService.getCartTotal(account.getId());
        }

        // Fetch addresses from DB
        List<AddressDTO> addresses = addressService.getAllAddresses(account.getId());
        model.addAttribute("addresses", addresses);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("pageTitle", "Thanh toán");
        model.addAttribute("checkoutMode", isBuyNow ? "BUY_NOW" : "CART");
        model.addAttribute("selectedCartItemIds", ids);
        model.addAttribute("appliedVoucherCode", voucherCode);
        return "user/checkout";
    }

    // ─── COUPON CONSTANTS ────────────────────────────────────────────────────
    private static final String COUPON_CODE = "OPENING";
    private static final int COUPON_PERCENT = 20; // 20% off

    /**
     * AJAX endpoint: validate coupon code.
     * Returns JSON: { valid, discountPercent, message }
     * Rules:
     * - Code must be "OPENING" (case-insensitive)
     * - Account must have NO previous orders (first order only)
     */
    @GetMapping("/checkout/validate-coupon")
    @ResponseBody
    public Map<String, Object> validateCoupon(
            @RequestParam("code") String code,
            Principal principal) {

        Map<String, Object> result = new HashMap<>();
        Account account = getAuthenticatedAccount(principal);

        if (account == null) {
            result.put("valid", false);
            result.put("message", "Bạn cần đăng nhập để dùng mã giảm giá.");
            return result;
        }

        if (!COUPON_CODE.equalsIgnoreCase(code == null ? "" : code.trim())) {
            result.put("valid", false);
            result.put("message", "Mã giảm giá không hợp lệ.");
            return result;
        }

        // Check first order: account must have 0 previous orders
        long orderCount = orderRepository.findByAccountId(account.getId()).size();
        if (orderCount > 0) {
            result.put("valid", false);
            result.put("message", "Mã OPENING chỉ áp dụng cho đơn hàng đầu tiên.");
            return result;
        }

        result.put("valid", true);
        result.put("discountPercent", COUPON_PERCENT);
        result.put("message", "Áp dụng thành công! Giảm " + COUPON_PERCENT + "% cho đơn hàng đầu tiên.");
        return result;
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
            @RequestParam(value = "cartItemIds", required = false) String cartItemIdsStr,
            @RequestParam(value = "payment", required = false, defaultValue = "COD") String paymentMethod,
            @RequestParam(value = "shippingFee", required = false, defaultValue = "30000") Long shippingFee,
            @RequestParam(value = "discountAmount", required = false, defaultValue = "0") Long discountAmount,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            Principal principal,
            RedirectAttributes redirectAttributes,
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
                List<Integer> cartItemIds = null;
                if (cartItemIdsStr != null && !cartItemIdsStr.isEmpty()) {
                    cartItemIds = java.util.Arrays.stream(cartItemIdsStr.split(","))
                            .map(Integer::parseInt)
                            .collect(java.util.stream.Collectors.toList());
                }

                List<Cart> cartItems;
                if (cartItemIds != null && !cartItemIds.isEmpty()) {
                    cartItems = cartService.getCartItemsByIds(cartItemIds);
                } else {
                    cartItems = cartService.getCartItems(account.getId());
                }

                if (cartItems == null || cartItems.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Lỗi: Không tìm thấy thông tin sản phẩm để thanh toán.");
                    return "redirect:/checkout";
                }
                order = orderCommandService.createOrder(account, cartItems, recipientName, phone, address);

                if (cartItemIds != null && !cartItemIds.isEmpty()) {
                    cartService.removeItemsFromCart(cartItemIds, account.getId());
                } else {
                    cartService.clearCart(account.getId());
                }
            }

            // Route by payment method
            if ("QR".equalsIgnoreCase(paymentMethod)) {
                order.setPaymentMethod("QR");
                order.setPaymentStatus("QR_PENDING");
            } else {
                order.setPaymentMethod("COD");
                order.setPaymentStatus("NOT_REQUIRED");
            }

            long safeFee = (shippingFee != null && shippingFee > 0) ? shippingFee : 30000L;
            long safeDiscount = (discountAmount != null && discountAmount > 0) ? discountAmount : 0L;

            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Voucher voucher = voucherRepository.findByCode(couponCode).orElse(null);
                if (voucher != null) {
                    if (voucher.getQuantity() > 0) {
                        order.setVoucher(voucher);
                        voucher.setQuantity(voucher.getQuantity() - 1);
                        voucherRepository.save(voucher);
                    } else if (safeDiscount > 0) {
                        throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng.");
                    }
                }
            }

            // Update the amounts on the order entity itself
            order.setShippingFee(BigDecimal.valueOf(safeFee));
            order.setDiscountAmount(BigDecimal.valueOf(safeDiscount));
            
            long finalAmt = Math.max(0, order.getTotalAmount().longValue() + safeFee - safeDiscount);
            order.setFinalAmount(BigDecimal.valueOf(finalAmt));
            
            orderRepository.save(order);
            notificationService.notifyNewOrder(order);

            if ("QR".equalsIgnoreCase(paymentMethod)) {
                return "redirect:/checkout/qr/" + order.getId();
            }

            String maskedPhone = maskPhoneNumber(phone);
            model.addAttribute("pageTitle", "Đặt hàng thành công");
            model.addAttribute("recipientName", recipientName != null ? recipientName : "Chưa chọn địa chỉ");
            model.addAttribute("phone", maskedPhone);
            model.addAttribute("fullAddress", address != null ? address : "");
            model.addAttribute("orderId", order.getId());
            return "user/order-success";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    // QR payment page has been moved to QrPaymentController.java

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
