package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    // Find variants by product ID
    java.util.List<ProductVariant> findByProductId(Integer productId);

    // Find variant by SKU
    java.util.Optional<ProductVariant> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);
}
