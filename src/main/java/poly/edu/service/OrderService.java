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
    private final poly.edu.repository.ProductVariantRepository productVariantRepository;

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
                productVariantRepository.save(variant);
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
     * Create a new order from cart items
     */
    @Transactional
    public Order createOrder(poly.edu.entity.Account account, List<poly.edu.entity.Cart> cartItems,
            String receiverName, String receiverPhone, String shippingAddress) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setAccount(account);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);

        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        for (poly.edu.entity.Cart cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProductVariant(cartItem.getProductVariant());
            detail.setProductName(cartItem.getProductVariant().getProduct().getName());
            detail.setQuantity(cartItem.getQuantity());

            double price = cartItem.getProductVariant().getProduct().getPrice();
            int discount = cartItem.getProductVariant().getProduct().getDiscount();
            double finalPrice = price * (1 - (double) discount / 100);

            detail.setPrice(java.math.BigDecimal.valueOf(finalPrice));
            detail.calculateTotal();

            totalAmount = totalAmount.add(detail.getTotal());
            orderDetailRepository.save(detail);

            // Update product variant inventory
            ProductVariant variant = cartItem.getProductVariant();
            int newQuantity = variant.getQuantity() - cartItem.getQuantity();
            if (newQuantity < 0) {
                throw new RuntimeException("Insufficient stock for product: " + variant.getProduct().getName());
            }
            variant.setQuantity(newQuantity);
            productVariantRepository.save(variant);
        }

        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setFinalAmount(totalAmount); // Assuming no additional fees/discounts for now
        savedOrder.setShippingFee(java.math.BigDecimal.ZERO);
        savedOrder.setDiscountAmount(java.math.BigDecimal.ZERO);

        return orderRepository.save(savedOrder);
    }

    /**
     * Create a new order for a single variant (Buy Now flow)
     */
    @Transactional
    public Order createOrderFromVariant(poly.edu.entity.Account account, Integer variantId, Integer quantity,
            String receiverName, String receiverPhone, String shippingAddress) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (variant.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + variant.getProduct().getName());
        }

        Order order = new Order();
        order.setAccount(account);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setProductVariant(variant);
        detail.setProductName(variant.getProduct().getName());
        detail.setQuantity(quantity);

        double price = variant.getProduct().getPrice();
        int discount = variant.getProduct().getDiscount();
        double finalPrice = price * (1 - (double) discount / 100);

        detail.setPrice(java.math.BigDecimal.valueOf(finalPrice));
        detail.calculateTotal();

        orderDetailRepository.save(detail);

        // Update product variant inventory
        variant.setQuantity(variant.getQuantity() - quantity);
        productVariantRepository.save(variant);

        savedOrder.setTotalAmount(detail.getTotal());
        savedOrder.setFinalAmount(detail.getTotal());
        savedOrder.setShippingFee(java.math.BigDecimal.ZERO);
        savedOrder.setDiscountAmount(java.math.BigDecimal.ZERO);

        return orderRepository.save(savedOrder);
    }

    /**
     * Get orders in date range
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }
}
