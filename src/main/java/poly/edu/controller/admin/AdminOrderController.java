package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Order;
import poly.edu.service.DashboardService;
import poly.edu.service.OrderService;

/**
 * AdminOrderController - Handles order management with State Machine pattern.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why use State Machine pattern for orders?"
 * 
 * Order status transitions must follow business rules:
 * 
 * PENDING ──approve──→ CONFIRMED ──ship──→ SHIPPING ──complete──→ COMPLETED
 * │ │ │
 * └──────────cancel──────┴──────cancel──────┘
 * ↓
 * CANCELLED
 * 
 * Benefits:
 * 1. Prevents invalid transitions (e.g., COMPLETED → PENDING)
 * 2. Each transition can trigger side effects (e.g., cancel restores stock)
 * 3. Easy to add new states without breaking existing logic
 * 4. Clear for testing - test each transition independently
 * 
 * "Why not use a State Machine library?"
 * 
 * For this e-commerce project, the state machine is simple enough:
 * - Only 5 states
 * - Linear transitions (no complex branching)
 * - Using Spring Statemachine would be overkill
 * 
 * Time Complexity:
 * - approveOrder(): O(1) - single update
 * - cancelOrder(): O(n) - n = order items (need to restore stock)
 * - completeOrder(): O(1) - single update
 */
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final DashboardService dashboardService;

    /**
     * List orders with optional status filter.
     * 
     * Algorithm:
     * 1. Check if status filter is provided
     * 2. If yes → filter by status
     * 3. If no → get all orders
     * 4. Always sort by orderDate descending (newest first)
     */
    @GetMapping
    public String orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý đơn hàng");

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<Order> orderPage;

        if (status != null && !status.isEmpty()) {
            orderPage = orderService.getOrdersByStatus(status, pageable);
            model.addAttribute("selectedStatus", status);
        } else {
            orderPage = orderService.getAllOrders(pageable);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        // Order statistics for status tabs
        model.addAttribute("orderStats", dashboardService.getOrderStatsByStatus());

        return "admin/orders";
    }

    /**
     * View order details.
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderService.getOrderDetails(id));

        return "admin/order-detail";
    }

    /**
     * Approve order (PENDING → CONFIRMED).
     * 
     * Business Rule:
     * - Only PENDING orders can be approved
     * - Approving means admin has reviewed and accepted the order
     */
    @PostMapping("/{id}/approve")
    public String approveOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.approveOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Duyệt đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Ship order (CONFIRMED → SHIPPING).
     * 
     * Business Rule:
     * - Only CONFIRMED orders can be shipped
     * - This means the order is now with the delivery service
     */
    @PostMapping("/{id}/ship")
    public String shipOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.shipOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã chuyển sang trạng thái Đang giao!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Complete order (SHIPPING → COMPLETED).
     * 
     * Business Rule:
     * - Only SHIPPING orders can be completed
     * - Customer has received the order
     */
    @PostMapping("/{id}/complete")
    public String completeOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.completeOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã hoàn thành!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Cancel order (Any non-COMPLETED → CANCELLED).
     * 
     * Algorithm:
     * 1. Load order with orderDetails (eager fetch)
     * 2. For each orderDetail:
     * a. Get productVariant
     * b. Restore quantity: variant.quantity += orderDetail.quantity
     * c. Save variant
     * 3. Set order.status = 'CANCELLED'
     * 4. Save order
     * 
     * Time Complexity: O(n) where n = number of order items
     * 
     * Why restore stock?
     * - When order was placed, stock was decreased
     * - Cancellation means items are available again
     * - Transaction ensures atomicity
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hủy đơn hàng thành công! Kho hàng đã được hoàn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}
