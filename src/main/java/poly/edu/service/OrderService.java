package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderDetailRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Get all orders with pagination
     * Algorithm: Simple pagination query
     * Time Complexity: O(n) where n = page size
     */
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Get orders by status
     * Algorithm: Filtered query with pagination
     * Time Complexity: O(n) where n = matching orders
     */
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    /**
     * Get order by ID
     * Algorithm: Direct lookup by primary key
     * Time Complexity: O(1)
     */
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    /**
     * Get orders by account ID
     */
    public Page<Order> getOrdersByAccountId(Integer accountId, Pageable pageable) {
        return orderRepository.findByAccountIdOrderByOrderDateDesc(accountId, pageable);
    }

    /**
     * Approve order (change status from PENDING to CONFIRMED)
     * Algorithm: Status transition validation + Update
     * Time Complexity: O(1)
     * 
     * Data Structure: State Machine
     * States: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED
     */
    @Transactional
    public Order approveOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Validate current status
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only approve orders with PENDING status. Current status: " + order.getStatus());
        }

        // Change status to CONFIRMED
        order.setStatus("CONFIRMED");
        return orderRepository.save(order);
    }

    /**
     * Cancel order with inventory restoration
     * Algorithm:
     * 1. Load order with orderDetails (eager fetch)
     * 2. For each orderDetail:
     * a. Get productVariant
     * b. Restore quantity: variant.quantity += orderDetail.quantity
     * c. Save variant
     * 3. Update order status to CANCELLED
     * 4. Save order
     * 
     * Time Complexity: O(n) where n = number of order items
     * Space Complexity: O(1) - no additional data structures
     * 
     * Transaction: ACID properties ensure atomicity
     */
    @Transactional
    public Order cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Validate current status - can only cancel PENDING or CONFIRMED orders
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only cancel orders with PENDING or CONFIRMED status. Current status: " + order.getStatus());
        }

        // Restore inventory for each order item
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : orderDetails) {
            ProductVariant variant = detail.getProductVariant();
            if (variant != null) {
                // Restore quantity
                int restoredQuantity = variant.getQuantity() + detail.getQuantity();
                variant.setQuantity(restoredQuantity);
                // Note: ProductVariant would need a repository to save
                // For now, this assumes cascade save through Product
            }
        }

        // Update order status
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    /**
     * Complete order (change status to COMPLETED)
     * Algorithm: Status transition validation + Update
     * Time Complexity: O(1)
     */
    @Transactional
    public Order completeOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Validate current status
        if (!"SHIPPING".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only complete orders with SHIPPING status. Current status: " + order.getStatus());
        }

        // Change status to COMPLETED
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    /**
     * Ship order (change status from CONFIRMED to SHIPPING)
     */
    @Transactional
    public Order shipOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only ship orders with CONFIRMED status. Current status: " + order.getStatus());
        }

        order.setStatus("SHIPPING");
        return orderRepository.save(order);
    }

    /**
     * Get order details
     */
    public List<OrderDetail> getOrderDetails(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    /**
     * Count orders by status
     */
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Get recent orders (last 10)
     */
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByOrderDateDesc();
    }

    /**
     * Get orders in date range
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }
}
