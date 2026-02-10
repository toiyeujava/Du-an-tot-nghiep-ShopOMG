package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.ProductVariantRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderCommandService - Handles write/mutation operations for orders.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why separate command operations?"
 *
 * 1. Single Responsibility: Each method modifies state (DB writes)
 * 2. Transaction boundaries are clearer - every public method is @Transactional
 * 3. Easier to add audit logging, event publishing, etc.
 * 4. Testing: Mock only what you need (no query methods cluttering the mock
 * setup)
 *
 * State Machine Pattern for Order Status:
 * ┌─────────┐ ┌───────────┐ ┌──────────┐ ┌───────────┐
 * │ PENDING │───→│ CONFIRMED │───→│ SHIPPING │───→│ COMPLETED │
 * └────┬────┘ └─────┬─────┘ └──────────┘ └───────────┘
 * │ │
 * └───────┬───────┘
 * ▼
 * ┌───────────┐
 * │ CANCELLED │
 * └───────────┘
 *
 * "Why not use a State Machine library (e.g., Spring Statemachine)?"
 * - Only 5 states → simple if/else is more readable
 * - No parallel or nested states needed
 * - Adding a library would be over-engineering for this use case
 */
@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Approve order: PENDING → CONFIRMED.
     * Time Complexity: O(1) single update.
     */
    @Transactional
    public Order approveOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only approve orders with PENDING status. Current status: " + order.getStatus());
        }

        order.setStatus("CONFIRMED");
        return orderRepository.save(order);
    }

    /**
     * Cancel order with inventory restoration.
     *
     * Algorithm:
     * 1. Validate status (only PENDING or CONFIRMED can be cancelled)
     * 2. For each order detail: restore variant quantity
     * 3. Set status to CANCELLED
     *
     * Time Complexity: O(n) where n = number of order items.
     * Transaction: ACID ensures atomicity - either all quantities restore or none.
     */
    @Transactional
    public Order cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only cancel orders with PENDING or CONFIRMED status. Current status: " + order.getStatus());
        }

        // Restore inventory for each order item
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : orderDetails) {
            ProductVariant variant = detail.getProductVariant();
            if (variant != null) {
                int restoredQuantity = variant.getQuantity() + detail.getQuantity();
                variant.setQuantity(restoredQuantity);
                productVariantRepository.save(variant);
            }
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    /**
     * Ship order: CONFIRMED → SHIPPING.
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
     * Complete order: SHIPPING → COMPLETED.
     */
    @Transactional
    public Order completeOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!"SHIPPING".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Can only complete orders with SHIPPING status. Current status: " + order.getStatus());
        }

        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    /**
     * Create order from cart items.
     *
     * Algorithm:
     * 1. Create Order entity with receiver info
     * 2. For each cart item: create OrderDetail, deduct inventory
     * 3. Calculate totals and save
     *
     * "Why check stock inside the loop?"
     * - Race condition: between adding to cart and checkout, stock could change
     * - @Transactional ensures rollback if any item is out of stock
     */
    @Transactional
    public Order createOrder(Account account, List<Cart> cartItems,
            String receiverName, String receiverPhone, String shippingAddress) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = buildOrder(account, receiverName, receiverPhone, shippingAddress);
        Order savedOrder = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cartItem : cartItems) {
            OrderDetail detail = buildOrderDetail(savedOrder, cartItem.getProductVariant(), cartItem.getQuantity());
            totalAmount = totalAmount.add(detail.getTotal());
            orderDetailRepository.save(detail);

            // Deduct inventory
            deductInventory(cartItem.getProductVariant(), cartItem.getQuantity());
        }

        return finalizeOrder(savedOrder, totalAmount);
    }

    /**
     * Create order for single variant (Buy Now flow).
     */
    @Transactional
    public Order createOrderFromVariant(Account account, Integer variantId, Integer quantity,
            String receiverName, String receiverPhone, String shippingAddress) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (variant.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + variant.getProduct().getName());
        }

        Order order = buildOrder(account, receiverName, receiverPhone, shippingAddress);
        Order savedOrder = orderRepository.save(order);

        OrderDetail detail = buildOrderDetail(savedOrder, variant, quantity);
        orderDetailRepository.save(detail);

        // Deduct inventory
        deductInventory(variant, quantity);

        return finalizeOrder(savedOrder, detail.getTotal());
    }

    // ===== PRIVATE HELPER METHODS =====

    private Order buildOrder(Account account, String receiverName, String receiverPhone, String shippingAddress) {
        Order order = new Order();
        order.setAccount(account);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setShippingAddress(shippingAddress);
        return order;
    }

    private OrderDetail buildOrderDetail(Order order, ProductVariant variant, int quantity) {
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProductVariant(variant);
        detail.setProductName(variant.getProduct().getName());
        detail.setQuantity(quantity);

        double price = variant.getProduct().getPrice();
        int discount = variant.getProduct().getDiscount();
        double finalPrice = price * (1 - (double) discount / 100);

        detail.setPrice(BigDecimal.valueOf(finalPrice));
        detail.calculateTotal();
        return detail;
    }

    private void deductInventory(ProductVariant variant, int quantity) {
        int newQuantity = variant.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock for product: " + variant.getProduct().getName());
        }
        variant.setQuantity(newQuantity);
        productVariantRepository.save(variant);
    }

    private Order finalizeOrder(Order order, BigDecimal totalAmount) {
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        return orderRepository.save(order);
    }
}
