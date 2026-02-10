package poly.edu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.OrderRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderQueryService.
 * Verifies all read-only operations work correctly.
 */
@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Test
    void getAllOrders_returnsPageOfOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(new Order(), new Order());
        Page<Order> page = new PageImpl<>(orders, pageable, 2);

        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<Order> result = orderQueryService.getAllOrders(pageable);

        assertEquals(2, result.getTotalElements());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getOrderById_found_returnsOrder() {
        Order order = new Order();
        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        Optional<Order> result = orderQueryService.getOrderById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void getOrderById_notFound_returnsEmpty() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Order> result = orderQueryService.getOrderById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void getOrderDetails_returnsDetailsList() {
        List<OrderDetail> details = Arrays.asList(new OrderDetail(), new OrderDetail());
        when(orderDetailRepository.findByOrderId(1)).thenReturn(details);

        List<OrderDetail> result = orderQueryService.getOrderDetails(1);

        assertEquals(2, result.size());
        verify(orderDetailRepository).findByOrderId(1);
    }

    @Test
    void countOrdersByStatus_returnsCount() {
        when(orderRepository.countByStatus("PENDING")).thenReturn(5L);

        long result = orderQueryService.countOrdersByStatus("PENDING");

        assertEquals(5L, result);
    }
}
