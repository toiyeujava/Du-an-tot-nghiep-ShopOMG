package poly.edu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import poly.edu.entity.*;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductVariantRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderCommandService.
 * Verifies order state transitions and inventory management.
 */
@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private OrderCommandService orderCommandService;

    // ===== APPROVE ORDER =====

    @Test
    void approveOrder_pendingOrder_setsConfirmed() {
        Order order = createTestOrder(1, "PENDING");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderCommandService.approveOrder(1);

        assertEquals("CONFIRMED", result.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void approveOrder_nonPendingOrder_throwsException() {
        Order order = createTestOrder(1, "CONFIRMED");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderCommandService.approveOrder(1));
        assertTrue(ex.getMessage().contains("PENDING"));
    }

    @Test
    void approveOrder_notFound_throwsException() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderCommandService.approveOrder(999));
    }

    // ===== CANCEL ORDER =====

    @Test
    void cancelOrder_pendingOrder_restoresInventory() {
        Order order = createTestOrder(1, "PENDING");
        ProductVariant variant = createTestVariant(10);
        OrderDetail detail = createTestDetail(variant, 3);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderId(1)).thenReturn(List.of(detail));
        when(productVariantRepository.save(any())).thenReturn(variant);
        when(orderRepository.save(any())).thenReturn(order);

        Order result = orderCommandService.cancelOrder(1);

        assertEquals("CANCELLED", result.getStatus());
        assertEquals(13, variant.getQuantity()); // 10 + 3 restored
        verify(productVariantRepository).save(variant);
    }

    @Test
    void cancelOrder_shippingOrder_throwsException() {
        Order order = createTestOrder(1, "SHIPPING");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderCommandService.cancelOrder(1));
    }

    // ===== SHIP ORDER =====

    @Test
    void shipOrder_confirmedOrder_setsShipping() {
        Order order = createTestOrder(1, "CONFIRMED");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Order result = orderCommandService.shipOrder(1);

        assertEquals("SHIPPING", result.getStatus());
    }

    @Test
    void shipOrder_pendingOrder_throwsException() {
        Order order = createTestOrder(1, "PENDING");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderCommandService.shipOrder(1));
    }

    // ===== COMPLETE ORDER =====

    @Test
    void completeOrder_shippingOrder_setsCompleted() {
        Order order = createTestOrder(1, "SHIPPING");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Order result = orderCommandService.completeOrder(1);

        assertEquals("COMPLETED", result.getStatus());
    }

    // ===== HELPER METHODS =====

    private Order createTestOrder(int id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

    private ProductVariant createTestVariant(int quantity) {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setDiscount(0);

        ProductVariant variant = new ProductVariant();
        variant.setId(1);
        variant.setQuantity(quantity);
        variant.setProduct(product);
        return variant;
    }

    private OrderDetail createTestDetail(ProductVariant variant, int quantity) {
        OrderDetail detail = new OrderDetail();
        detail.setProductVariant(variant);
        detail.setQuantity(quantity);
        return detail;
    }
}
