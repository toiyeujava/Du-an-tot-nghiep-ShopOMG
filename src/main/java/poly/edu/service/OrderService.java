package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderService - Facade that delegates to OrderQueryService and
 * OrderCommandService.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why keep this class if Query and Command services exist?"
 *
 * 1. Backward compatibility: Multiple controllers depend on OrderService.
 * Removing it would require updating AdminOrderController, DashboardService,
 * etc.
 * 2. Facade pattern: Provides a unified API while internal implementation
 * follows CQRS-lite (Command-Query Responsibility Segregation).
 * 3. Easy to evolve: If we need to add cross-cutting concerns (logging,
 * metrics),
 * this is the perfect interception point.
 *
 * "Why not just find-and-replace all usages?"
 * - Risk of breaking changes in a graduation project is not worth it.
 * - The facade costs almost nothing in terms of performance or complexity.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderQueryService queryService;
    private final OrderCommandService commandService;

    // ===== QUERY OPERATIONS (delegated to OrderQueryService) =====

    public Page<Order> getAllOrders(Pageable pageable) {
        return queryService.getAllOrders(pageable);
    }

    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return queryService.getOrdersByStatus(status, pageable);
    }

    public Optional<Order> getOrderById(Integer id) {
        return queryService.getOrderById(id);
    }

    public Page<Order> getOrdersByAccountId(Integer accountId, Pageable pageable) {
        return queryService.getOrdersByAccountId(accountId, pageable);
    }

    public List<OrderDetail> getOrderDetails(Integer orderId) {
        return queryService.getOrderDetails(orderId);
    }

    public long countOrdersByStatus(String status) {
        return queryService.countOrdersByStatus(status);
    }

    public List<Order> getRecentOrders() {
        return queryService.getRecentOrders();
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return queryService.getOrdersByDateRange(startDate, endDate);
    }

    public long countQrPendingOrders() {
        return queryService.countQrPendingOrders();
    }

    public Page<Order> getQrPendingOrders(Pageable pageable) {
        return queryService.getQrPendingOrders(pageable);
    }

    // ===== COMMAND OPERATIONS (delegated to OrderCommandService) =====

    public Order approveOrder(Integer orderId) {
        return commandService.approveOrder(orderId);
    }

    public Order cancelOrder(Integer orderId) {
        return commandService.cancelOrder(orderId);
    }

    public Order completeOrder(Integer orderId) {
        return commandService.completeOrder(orderId);
    }

    public Order shipOrder(Integer orderId) {
        return commandService.shipOrder(orderId);
    }

    public Order createOrder(Account account, List<Cart> cartItems,
            String receiverName, String receiverPhone, String shippingAddress) {
        return commandService.createOrder(account, cartItems, receiverName, receiverPhone, shippingAddress);
    }

    public Order createOrderFromVariant(Account account, Integer variantId, Integer quantity,
            String receiverName, String receiverPhone, String shippingAddress) {
        return commandService.createOrderFromVariant(account, variantId, quantity,
                receiverName, receiverPhone, shippingAddress);
    }

    public Order confirmQrPayment(Integer orderId, String confirmedByUsername) {
        return commandService.confirmQrPayment(orderId, confirmedByUsername);
    }

    public Order rejectQrPayment(Integer orderId, String rejectedByUsername) {
        return commandService.rejectQrPayment(orderId, rejectedByUsername);
    }
}
