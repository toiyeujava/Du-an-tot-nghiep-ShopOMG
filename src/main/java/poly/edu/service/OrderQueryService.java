package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderDetailRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderQueryService - Handles read-only order operations.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why split OrderService into Query and Command?"
 *
 * CQRS Lite (Command-Query Responsibility Segregation):
 * - Queries (reads) have different performance characteristics than commands
 * (writes)
 * - Reads can be cached, scaled horizontally, use read replicas
 * - Writes need strict consistency, transaction management
 * - Separating them makes each class focused and testable
 *
 * "Why not a full CQRS with separate databases?"
 * - Overkill for this project size
 * - Same database is fine, just separate the code responsibility
 * - Easy to evolve into full CQRS later if needed
 *
 * Time Complexity:
 * - getAllOrders(): O(n) where n = page size
 * - getOrderById(): O(1) primary key lookup
 * - countOrdersByStatus(): O(n) where n = total orders (DB-optimized with
 * index)
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Get all orders with pagination.
     * Algorithm: Delegates to Spring Data paginated query.
     */
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Get orders filtered by status.
     * Algorithm: WHERE clause filter with pagination.
     */
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    /**
     * Get single order by primary key.
     * Time Complexity: O(1) - indexed lookup.
     */
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    /**
     * Get orders for a specific user account.
     */
    public Page<Order> getOrdersByAccountId(Integer accountId, Pageable pageable) {
        return orderRepository.findByAccountIdOrderByOrderDateDesc(accountId, pageable);
    }

    /**
     * Get order line items.
     */
    public List<OrderDetail> getOrderDetails(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    /**
     * Count orders by status (for dashboard stats).
     */
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Get most recent 10 orders (for dashboard).
     */
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByOrderDateDesc();
    }

    /**
     * Get orders within a date range (for reports/charts).
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }
}
