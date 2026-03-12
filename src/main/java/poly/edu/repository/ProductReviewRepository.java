package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.ProductReview;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {

    /** All reviews submitted by a specific account, newest first */
    List<ProductReview> findByAccountIdOrderByReviewDateDesc(Integer accountId);

    /** Reviews for a specific product */
    List<ProductReview> findByProductIdOrderByReviewDateDesc(Integer productId);

    /** Check if account has already reviewed a product */
    boolean existsByProductIdAndAccountId(Integer productId, Integer accountId);

    /** Find existing review by product + account (for update — legacy fallback) */
    Optional<ProductReview> findByProductIdAndAccountId(Integer productId, Integer accountId);

    /** Safe fallback when multiple reviews exist for same product+account (different orders) */
    Optional<ProductReview> findFirstByProductIdAndAccountId(Integer productId, Integer accountId);

    /** Find existing review by order + product + account (for per-order upsert) */
    Optional<ProductReview> findByOrderIdAndProductIdAndAccountId(Integer orderId, Integer productId, Integer accountId);

    /** Average rating for a product */
    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0) FROM ProductReview r WHERE r.product.id = :productId")
    Double getAverageRating(@Param("productId") Integer productId);

    /** Count reviews for a product */
    long countByProductId(Integer productId);
    /**
     * Kiểm tra account đã mua sản phẩm này chưa (qua OrderDetails).
     * Điều kiện: đơn hàng có status = 'COMPLETED'.
     */
    @Query("""
        SELECT COUNT(od) > 0
        FROM OrderDetail od
        JOIN od.order o
        JOIN od.productVariant pv
        WHERE o.account.id = :accountId
          AND pv.product.id = :productId
          AND o.status = 'COMPLETED'
        """)
    boolean hasPurchasedProduct(@Param("accountId") Integer accountId,
                                @Param("productId") Integer productId);

    /**
     * Check if this specific order+product combination has been reviewed.
     * Used to determine per-order review button state.
     */
    boolean existsByOrderIdAndProductIdAndAccountId(Integer orderId, Integer productId, Integer accountId);

    /**
     * Returns a list of "orderId_productId" strings for all reviews submitted
     * by the given account that have an associated order.
     * Used to build the per-order reviewed key set in the order history page.
     */
    @Query("""
        SELECT CONCAT(CAST(r.order.id AS string), '_', CAST(r.product.id AS string))
        FROM ProductReview r
        WHERE r.account.id = :accountId AND r.order IS NOT NULL
        """)
    List<String> findReviewedOrderProductKeys(@Param("accountId") Integer accountId);
}
