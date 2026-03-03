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

    /** Find existing review by product + account (for update) */
    Optional<ProductReview> findByProductIdAndAccountId(Integer productId, Integer accountId);

    /** Average rating for a product */
    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0) FROM ProductReview r WHERE r.product.id = :productId")
    Double getAverageRating(@Param("productId") Integer productId);

    /** Count reviews for a product */
    long countByProductId(Integer productId);
}
