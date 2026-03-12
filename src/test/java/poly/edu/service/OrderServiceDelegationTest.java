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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the refactored OrderService Facade.
 * Verifies that all methods correctly delegate to the appropriate service.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why test delegation? Isn't it trivial?"
 *
 * 1. Ensures the facade wiring is correct after refactoring
 * 2. Catches accidental direct implementations (should be delegation only)
 * 3. Documents the contract: OrderService must delegate, not implement
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceDelegationTest {

    @Mock
    private OrderQueryService queryService;

    @Mock
    private OrderCommandService commandService;

    @InjectMocks
    private OrderService orderService;

    // ===== QUERY DELEGATION =====

    @Test
    void getAllOrders_delegatesToQueryService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> expected = new PageImpl<>(List.of());
        when(queryService.getAllOrders(pageable)).thenReturn(expected);

        Page<Order> result = orderService.getAllOrders(pageable);

        assertSame(expected, result);
        verify(queryService).getAllOrders(pageable);
    }

    @Test
    void getOrderById_delegatesToQueryService() {
        Optional<Order> expected = Optional.of(new Order());
        when(queryService.getOrderById(1)).thenReturn(expected);

        Optional<Order> result = orderService.getOrderById(1);

        assertSame(expected, result);
        verify(queryService).getOrderById(1);
    }

    @Test
    void countQrPendingOrders_delegatesToQueryService() {
        when(queryService.countQrPendingOrders()).thenReturn(5L);

        long result = orderService.countQrPendingOrders();

        assertEquals(5L, result);
        verify(queryService).countQrPendingOrders();
    }

    // ===== COMMAND DELEGATION =====

    @Test
    void approveOrder_delegatesToCommandService() {
        Order expected = new Order();
        when(commandService.approveOrder(1)).thenReturn(expected);

        Order result = orderService.approveOrder(1);

        assertSame(expected, result);
        verify(commandService).approveOrder(1);
    }

    @Test
    void confirmQrPayment_delegatesToCommandService() {
        Order expected = new Order();
        when(commandService.confirmQrPayment(1, "admin")).thenReturn(expected);

        Order result = orderService.confirmQrPayment(1, "admin");

        assertSame(expected, result);
        verify(commandService).confirmQrPayment(1, "admin");
    }

    @Test
    void rejectQrPayment_delegatesToCommandService() {
        Order expected = new Order();
        when(commandService.rejectQrPayment(1, "admin")).thenReturn(expected);

        Order result = orderService.rejectQrPayment(1, "admin");

        assertSame(expected, result);
        verify(commandService).rejectQrPayment(1, "admin");
    }

    @Test
    void cancelOrder_delegatesToCommandService() {
        Order expected = new Order();
        when(commandService.cancelOrder(1)).thenReturn(expected);

        Order result = orderService.cancelOrder(1);

        assertSame(expected, result);
        verify(commandService).cancelOrder(1);
    }
}
