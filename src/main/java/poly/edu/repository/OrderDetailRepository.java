package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.OrderDetail;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // Find order details by order ID
    List<OrderDetail> findByOrderId(Integer orderId);

    // Get top selling products
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
}
