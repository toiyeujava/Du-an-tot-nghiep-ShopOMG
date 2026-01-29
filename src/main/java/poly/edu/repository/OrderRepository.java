package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Find orders by status
    List<Order> findByStatus(String status);

    Page<Order> findByStatus(String status, Pageable pageable);

    // Find orders by account
    List<Order> findByAccountId(Integer accountId);

    Page<Order> findByAccountIdOrderByOrderDateDesc(Integer accountId, Pageable pageable);

    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Count orders by status
    Long countByStatus(String status);

    // Get monthly revenue
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND MONTH(o.orderDate) = :month " +
            "AND YEAR(o.orderDate) = :year")
    BigDecimal getMonthlyRevenue(@Param("month") int month, @Param("year") int year);

    // Get daily revenue for chart
    @Query("SELECT CAST(o.orderDate AS date) as orderDate, SUM(o.finalAmount) as revenue " +
            "FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(o.orderDate AS date) " +
            "ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get recent orders
    List<Order> findTop10ByOrderByOrderDateDesc();

    // Check if product variant has orders
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od " +
            "WHERE od.productVariant.product.id = :productId " +
            "AND od.order.status IN ('PENDING', 'CONFIRMED', 'SHIPPING')")
    boolean hasActiveOrdersForProduct(@Param("productId") Integer productId);
}
