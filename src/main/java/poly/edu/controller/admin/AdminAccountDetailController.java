package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.entity.Account;
import poly.edu.entity.AuditLog;
import poly.edu.entity.Order;
import poly.edu.service.AdminAccountService;
import poly.edu.service.AuditLogService;

import java.util.List;

/**
 * AdminAccountDetailController — single-account detail page.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why load audit logs server-side instead of a separate AJAX call?"
 *
 * Per-account activity logs are typically small (<100 entries), so a second
 * HTTP round-trip and extra JS fetch adds complexity for no measurable gain.
 * Server-side rendering via Thymeleaf keeps the stack uniform and the page
 * fully functional even without JavaScript.
 *
 * "Why add the `tab` param?"
 *
 * After a POST action (lock, reset password, etc.) the controller redirects
 * back to this page. Passing `?tab=logs` in the redirect URL opens the
 * activity-log tab immediately, so the admin sees the newly written log entry
 * without having to switch tabs manually.
 */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountDetailController {

    private final AdminAccountService adminAccountService;
    private final AuditLogService auditLogService;

    @GetMapping("/{id}")
    public String accountDetail(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "orders") String tab,
            Model model) {

        Account account = adminAccountService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        model.addAttribute("pageTitle", "Chi tiết tài khoản: " + account.getUsername());
        model.addAttribute("account", account);
        model.addAttribute("activeTab", tab);

        // Orders tab (paginated)
        Pageable pageable = PageRequest.of(page, 10);
        Page<Order> orders = adminAccountService.getUserOrders(id, pageable);
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        // Activity log tab — all logs for this account (small enough to skip pagination)
        List<AuditLog> activityLogs = auditLogService.getLogsByEntity("Account", id);
        model.addAttribute("activityLogs", activityLogs);

        return "admin/account-detail";
    }
}
