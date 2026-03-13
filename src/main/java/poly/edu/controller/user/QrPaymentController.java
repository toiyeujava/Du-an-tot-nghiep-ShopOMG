package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.entity.Order;
import poly.edu.repository.OrderRepository;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * QrPaymentController - Displays QR payment page for customers.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why extract this from CheckoutController?"
 *
 * 1. CheckoutController was 312 lines → violates the 250-line rule
 * 2. QR payment display is a distinct concern from checkout processing:
 * - Checkout = cart → order creation (write-heavy, transactional)
 * - QR display = show a QR image for an existing order (read-only)
 * 3. Future extensibility: if we add MoMo, ZaloPay, VNPay, each can be
 * its own controller without bloating CheckoutController
 *
 * "Why not put this in AdminOrderController?"
 * - This is a CUSTOMER-facing page (the user sees the QR to pay)
 * - Admin confirmation is a separate action in AdminOrderController
 */
@Controller
@RequiredArgsConstructor
public class QrPaymentController {

    // VietQR constants
    private static final String VIETQR_BASE = "https://img.vietqr.io/image/MB-0961342609-compact.png";

    private final OrderRepository orderRepository;

    /**
     * Display QR payment page for a given order.
     * Generates a VietQR URL dynamically with the order amount and code.
     *
     * Algorithm:
     * 1. Verify user is authenticated
     * 2. Load order by ID
     * 3. Calculate final QR amount = productTotal + shipping - discount
     * 4. Build VietQR URL with amount and order code as description
     * 5. Return view with QR data
     *
     * Time Complexity: O(1) - single DB lookup by primary key.
     */
    @GetMapping("/checkout/qr/{orderId}")
    public String qrPaymentPage(
            @PathVariable Integer orderId,
            Model model, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "redirect:/account/orders";
        }

        String orderCode = "OMG" + orderId;
        BigDecimal finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
        long amountLong = Math.max(0, finalAmount.longValue());

        // Build VietQR URL with dynamic amount and description
        String encodedDesc;
        try {
            encodedDesc = URLEncoder.encode(orderCode, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            encodedDesc = orderCode;
        }
        String qrUrl = VIETQR_BASE + "?amount=" + amountLong + "&addInfo=" + encodedDesc;

        model.addAttribute("pageTitle", "Thanh toán QR - " + orderCode);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("amount", amountLong);
        model.addAttribute("qrUrl", qrUrl);
        return "user/qr-payment";
    }
}
