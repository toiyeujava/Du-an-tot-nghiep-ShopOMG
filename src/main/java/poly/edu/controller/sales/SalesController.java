package poly.edu.controller.sales;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.service.EmailService; // 4.9

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/sales")
@PreAuthorize("hasAnyRole('SALES', 'ADMIN')")
@RequiredArgsConstructor
public class SalesController {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService; // 4.9

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("pageTitle", "Dashboard - Bán hàng");

        // Lấy thông tin tài khoản đang đăng nhập
        if (userDetails != null) {
            accountRepository.findByEmail(userDetails.getUsername())
                .ifPresent(acc -> model.addAttribute("currentAccount", acc));
        }

        // Đếm đơn theo trạng thái
        Map<String, Long> orderStatsByStatus = new HashMap<>();
        orderStatsByStatus.put("PENDING",   orderRepository.countByStatus("PENDING"));
        orderStatsByStatus.put("CONFIRMED", orderRepository.countByStatus("CONFIRMED"));
        orderStatsByStatus.put("SHIPPING",  orderRepository.countByStatus("SHIPPING"));
        orderStatsByStatus.put("COMPLETED", orderRepository.countByStatus("COMPLETED"));
        orderStatsByStatus.put("CANCELLED", orderRepository.countByStatus("CANCELLED"));
        model.addAttribute("orderStatsByStatus", orderStatsByStatus);

        // Stats cards - dùng findByDateRange có sẵn trong OrderRepository
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().plusDays(1).atStartOfDay();

        var todayOrders = orderRepository.findByDateRange(startOfDay, endOfDay);

        model.addAttribute("pendingCount",        orderStatsByStatus.get("PENDING"));
        model.addAttribute("todayCount",          todayOrders.size());
        model.addAttribute("completedTodayCount", todayOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus())).count());

        // 10 đơn hàng mới nhất
        model.addAttribute("recentOrders", orderRepository.findTop10ByOrderByOrderDateDesc());

        return "sales/dashboard";
    }

    //Danh sách đơn hàng + lọc + tìm kiếm
    @GetMapping("/orders")
    public String orders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        if (userDetails != null) {
            accountRepository.findByEmail(userDetails.getUsername())
                .ifPresent(acc -> model.addAttribute("currentAccount", acc));
        }

        String kw  = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String sts = (status  != null && !status.trim().isEmpty())  ? status.trim()  : null;

        Pageable pageable = PageRequest.of(page, 15);
        Page<poly.edu.entity.Order> orders = orderRepository.searchOrders(sts, kw, pageable);

        model.addAttribute("pendingCount",   orderRepository.countByStatus("PENDING"));
        model.addAttribute("orders",         orders);
        model.addAttribute("currentStatus",  sts);
        model.addAttribute("keyword",        kw != null ? kw : "");
        model.addAttribute("currentPage",    page);

        return "sales/orders";
    }
    
    // Chi tiết đơn hàng
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {

        poly.edu.entity.Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + id));

        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        model.addAttribute("order", order);
        model.addAttribute("pendingCount", orderRepository.countByStatus("PENDING"));

        if (userDetails != null) {
            accountRepository.findByEmail(userDetails.getUsername())
                .ifPresent(acc -> model.addAttribute("currentAccount", acc));
        }

        return "sales/order-detail";
    }
    
 // 4.7: Cập nhật trạng thái đơn hàng
    @PutMapping("/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Integer id,
                                          @RequestBody Map<String, String> body) {
        try {
            poly.edu.entity.Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn #" + id));
            order.setStatus(body.get("status"));
            orderRepository.save(order);
            emailService.sendOrderStatusEmail(order); // 4.9: gửi email thông báo trạng thái
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 4.8: Hủy đơn hàng
    @PutMapping("/orders/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id,
                                         @RequestBody Map<String, String> body) {
        try {
            poly.edu.entity.Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn #" + id));
            if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("success", false,
                        "message", "Không thể hủy đơn ở trạng thái này!"));
            }
            String reason = body.get("reason");
            order.setStatus("CANCELLED");
            order.setNote(reason != null ? "[Hủy bởi NV] " + reason : "[Hủy bởi nhân viên]");
            orderRepository.save(order);
            emailService.sendOrderCancelledEmail(order, reason); // 4.9: gửi email hủy đơn kèm lý do
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã hủy đơn hàng!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}