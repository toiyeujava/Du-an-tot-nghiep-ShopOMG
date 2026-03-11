package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.OrderDetail;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Find order details by order ID
    List<OrderDetail> findByOrderId(Integer orderId);

    // Get top selling products for a given month/year
    @Query("SELECT od.productVariant.product.id as productId, " +
            "od.productVariant.product.name as productName, " +
            "od.productVariant.product.image as productImage, " +
            "SUM(od.quantity) as totalSold, " +
            "SUM(od.total) as totalRevenue " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND MONTH(o.orderDate) = :month " +
            "AND YEAR(o.orderDate) = :year " +
            "GROUP BY od.productVariant.product.id, od.productVariant.product.name, od.productVariant.product.image " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> getTopSellingProducts(@Param("month") int month,
            @Param("year") int year,
            @Param("limit") int limit);

    // Get top products sold TODAY (within a date range) for any active status
    @Query("SELECT od.productVariant.product.name as productName, " +
            "SUM(od.quantity) as totalSold " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status != 'CANCELLED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY od.productVariant.product.id, od.productVariant.product.name " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> getTopProductsSoldToday(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get completed/paid products sold TODAY (within a date range)
    @Query("SELECT od.productVariant.product.name as productName, " +
            "SUM(od.quantity) as totalSold, " +
            "SUM(od.total) as totalRevenue " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status != 'CANCELLED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY od.productVariant.product.id, od.productVariant.product.name " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> getCompletedProductsSoldToday(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get daily units sold for chart
    @Query("SELECT CAST(o.orderDate AS date) as orderDate, SUM(od.quantity) as unitsSold " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status != 'CANCELLED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(o.orderDate AS date) " +
            "ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getDailyUnitsSold(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Get completed/paid products sold on a SPECIFIC DATE (within a date range)
    @Query("SELECT od.productVariant.product.name as productName, " +
            "SUM(od.quantity) as totalSold, " +
            "SUM(od.total) as totalRevenue, " +
            "od.productVariant.product.image as productImage " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status != 'CANCELLED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY od.productVariant.product.id, od.productVariant.product.name, od.productVariant.product.image " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> getCompletedProductsSoldOnDate(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Count total units sold today
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.status != 'CANCELLED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countUnitsSoldToday(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}
