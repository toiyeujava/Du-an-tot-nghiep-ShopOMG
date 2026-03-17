package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.entity.Order;
import poly.edu.repository.OrderRepository;

import java.time.format.DateTimeFormatter;

/**
 * TransactionDetailController - Displays a filtered transaction detail page.
 *
 * Shows: order code, amount, transfer content, payment status, transaction time.
 * Hides: bank account info, code thanh toán, xác thực tự động, webhook/chat/app logs.
 */
@Controller
@RequiredArgsConstructor
public class TransactionDetailController {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    private final OrderRepository orderRepository;

    @GetMapping("/transaction-detail")
    public String transactionDetail(
            @RequestParam(value = "orderId", required = false) Integer orderId,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        if (orderId == null) {
            return "redirect:/account/orders";
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "redirect:/account/orders";
        }

        String orderCode = "OMG" + orderId;
        String confirmedAt = order.getPaymentConfirmedAt() != null
                ? order.getPaymentConfirmedAt().format(DISPLAY_FMT)
                : "—";

        String realContent = order.getTransferContent() != null
                ? order.getTransferContent()
                : "OMG" + orderId;
        String realRefCode = order.getReferenceCode() != null
                ? order.getReferenceCode()
                : "—";

        model.addAttribute("orderCode",       orderCode);
        model.addAttribute("referenceCode",   realRefCode);
        model.addAttribute("amount",          order.getFinalAmount());
        model.addAttribute("transferContent", realContent);
        model.addAttribute("paymentStatus",   order.getPaymentStatus());
        model.addAttribute("orderStatus",     order.getStatus());
        model.addAttribute("transactionDate", confirmedAt);
        model.addAttribute("orderId",         orderId);

        return "user/transaction-detail";
    }
}
