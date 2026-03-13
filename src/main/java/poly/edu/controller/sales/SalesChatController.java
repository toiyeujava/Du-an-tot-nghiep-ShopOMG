package poly.edu.controller.sales;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.OrderRepository;

@Controller
@RequestMapping("/sales")
@PreAuthorize("hasAnyRole('SALES', 'ADMIN')")
@RequiredArgsConstructor
public class SalesChatController {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/chat")
    public String openSalesChat(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("pageTitle", "Hỗ trợ khách hàng");

        // Cần thiết để badge đơn hàng trên sidebar không bị mất
        model.addAttribute("pendingCount", orderRepository.countByStatus("PENDING"));

        // Thông tin nhân viên hiện tại
        if (userDetails != null) {
            accountRepository.findByEmail(userDetails.getUsername())
                .ifPresent(acc -> model.addAttribute("currentAccount", acc));
        }

        return "sales/sales_chat";
    }
}