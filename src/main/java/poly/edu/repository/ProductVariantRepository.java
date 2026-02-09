package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.ProductVariant;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    // Find variants by product ID
    List<ProductVariant> findByProductId(Integer productId);

    // Find variant by SKU
    Optional<ProductVariant> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);

    // Check if size/color combination exists for product
    @Query("SELECT COUNT(pv) > 0 FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.size = :size AND pv.color = :color")
    boolean existsByProductIdAndSizeAndColor(@Param("productId") Integer productId,
            @Param("size") String size,
            @Param("color") String color);

    // Find variant by product ID, color, and size
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.color = :color AND pv.size = :size")
    Optional<ProductVariant> findByProductIdAndColorAndSize(@Param("productId") Integer productId,
            @Param("color") String color,
            @Param("size") String size);

    // Check if variant has active orders (PENDING, CONFIRMED, SHIPPING)
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od WHERE od.productVariant.id = :variantId " +
            "AND od.order.status IN ('PENDING', 'CONFIRMED', 'SHIPPING')")
    boolean hasActiveOrders(@Param("variantId") Integer variantId);
}
