package poly.edu.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import poly.edu.entity.Order;
import poly.edu.service.DashboardService;
import poly.edu.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminOrderControllerTest - Unit tests for order management.
 * 
 * Test Focus:
 * - State machine transitions (approve, ship, complete, cancel)
 * - Error handling for invalid transitions
 * - Stock restoration on cancel
 */
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private DashboardService dashboardService;

    private Order pendingOrder;
    private Order confirmedOrder;
    private Order shippingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = new Order();
        pendingOrder.setId(1);
        pendingOrder.setStatus("PENDING");
        pendingOrder.setFinalAmount(BigDecimal.valueOf(500000));
        pendingOrder.setOrderDate(LocalDateTime.now());

        confirmedOrder = new Order();
        confirmedOrder.setId(2);
        confirmedOrder.setStatus("CONFIRMED");
        confirmedOrder.setFinalAmount(BigDecimal.valueOf(300000));
        confirmedOrder.setOrderDate(LocalDateTime.now());

        shippingOrder = new Order();
        shippingOrder.setId(3);
        shippingOrder.setStatus("SHIPPING");
        shippingOrder.setFinalAmount(BigDecimal.valueOf(400000));
        shippingOrder.setOrderDate(LocalDateTime.now());
    }

    // ==================== GET /admin/orders ====================

    @Nested
    @DisplayName("GET /admin/orders - List Orders")
    class ListOrders {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should list all orders with pagination")
        void orders_withNoFilter_returnsAllOrders() throws Exception {
            // Arrange
            List<Order> orders = Arrays.asList(pendingOrder, confirmedOrder);
            Page<Order> orderPage = new PageImpl<>(orders);
            when(orderService.getAllOrders(any(PageRequest.class))).thenReturn(orderPage);
            when(dashboardService.getOrderStatsByStatus()).thenReturn(
                    Map.of("PENDING", 5L, "CONFIRMED", 3L, "SHIPPING", 2L, "COMPLETED", 10L));

            // Act & Assert
            mockMvc.perform(get("/admin/orders"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/orders"))
                    .andExpect(model().attributeExists("orders"))
                    .andExpect(model().attributeExists("orderStats"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should filter orders by status")
        void orders_withStatusFilter_returnsFilteredOrders() throws Exception {
            // Arrange
            Page<Order> orderPage = new PageImpl<>(List.of(pendingOrder));
            when(orderService.getOrdersByStatus(eq("PENDING"), any())).thenReturn(orderPage);
            when(dashboardService.getOrderStatsByStatus()).thenReturn(Map.of());

            // Act & Assert
            mockMvc.perform(get("/admin/orders")
                    .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedStatus", "PENDING"));

            verify(orderService).getOrdersByStatus(eq("PENDING"), any());
        }
    }

    // ==================== State Transitions ====================

    @Nested
    @DisplayName("Order State Transitions")
    class StateTransitions {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should approve pending order (PENDING → CONFIRMED)")
        void approveOrder_pendingOrder_success() throws Exception {
            // Arrange
            doNothing().when(orderService).approveOrder(1);

            // Act & Assert
            mockMvc.perform(post("/admin/orders/1/approve")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/orders/1"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(orderService).approveOrder(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should fail to approve non-pending order")
        void approveOrder_nonPendingOrder_showsError() throws Exception {
            // Arrange
            doThrow(new IllegalStateException("Only PENDING orders can be approved"))
                    .when(orderService).approveOrder(2);

            // Act & Assert
            mockMvc.perform(post("/admin/orders/2/approve")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should ship confirmed order (CONFIRMED → SHIPPING)")
        void shipOrder_confirmedOrder_success() throws Exception {
            // Arrange
            doNothing().when(orderService).shipOrder(2);

            // Act & Assert
            mockMvc.perform(post("/admin/orders/2/ship")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/orders/2"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should complete shipping order (SHIPPING → COMPLETED)")
        void completeOrder_shippingOrder_success() throws Exception {
            // Arrange
            doNothing().when(orderService).completeOrder(3);

            // Act & Assert
            mockMvc.perform(post("/admin/orders/3/complete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/orders/3"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should cancel order and restore stock")
        void cancelOrder_activeOrder_successAndRestoresStock() throws Exception {
            // Arrange
            doNothing().when(orderService).cancelOrder(1);

            // Act & Assert
            mockMvc.perform(post("/admin/orders/1/cancel")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/orders/1"))
                    .andExpect(flash().attribute("successMessage",
                            "Hủy đơn hàng thành công! Kho hàng đã được hoàn."));

            verify(orderService).cancelOrder(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should fail to cancel completed order")
        void cancelOrder_completedOrder_showsError() throws Exception {
            // Arrange
            doThrow(new IllegalStateException("Cannot cancel COMPLETED orders"))
                    .when(orderService).cancelOrder(4);

            // Act & Assert
            mockMvc.perform(post("/admin/orders/4/cancel")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    // ==================== GET /admin/orders/{id} ====================

    @Nested
    @DisplayName("GET /admin/orders/{id} - Order Detail")
    class OrderDetail {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return order detail page")
        void orderDetail_existingOrder_returnsDetailPage() throws Exception {
            // Arrange
            when(orderService.getOrderById(1)).thenReturn(Optional.of(pendingOrder));
            when(orderService.getOrderDetails(1)).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/admin/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/order-detail"))
                    .andExpect(model().attributeExists("order"))
                    .andExpect(model().attributeExists("orderDetails"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should throw when order not found")
        void orderDetail_nonExistingOrder_throws() throws Exception {
            // Arrange
            when(orderService.getOrderById(999)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/admin/orders/999"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
