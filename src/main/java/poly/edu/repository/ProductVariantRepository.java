package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.ProductVariant;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    // Find variants by product ID
    java.util.List<ProductVariant> findByProductId(Integer productId);

    // Find variant by SKU
    java.util.Optional<ProductVariant> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);
    
    // Find variant by product ID, color, and size
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.color = :color AND pv.size = :size")
    Optional<ProductVariant> findByProductIdAndColorAndSize(@Param("productId") Integer productId, 
                                                              @Param("color") String color, 
                                                              @Param("size") String size);
}
