package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Account;
import poly.edu.entity.Product;
import poly.edu.entity.ProductReview;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.ProductReviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    /** Lấy tất cả đánh giá của một sản phẩm (mới nhất trước) */
    public List<ProductReview> getReviewsByProduct(Integer productId) {
        return reviewRepository.findByProductIdOrderByReviewDateDesc(productId);
    }

    /** Lấy tất cả đánh giá của một tài khoản */
    public List<ProductReview> getReviewsByAccount(Integer accountId) {
        return reviewRepository.findByAccountIdOrderByReviewDateDesc(accountId);
    }

    /** Điểm trung bình của sản phẩm */
    public Double getAverageRating(Integer productId) {
        return reviewRepository.getAverageRating(productId);
    }

    /** Số lượng đánh giá theo từng sao (1-5) */
    public long countByStar(Integer productId, int star) {
        return reviewRepository.findByProductIdOrderByReviewDateDesc(productId)
                .stream().filter(r -> r.getRating() == star).count();
    }

    /** Kiểm tra user đã mua sản phẩm chưa (qua OrderDetails → ProductVariant → Product) */
    public boolean hasPurchased(Integer accountId, Integer productId) {
        return reviewRepository.hasPurchasedProduct(accountId, productId);
    }

    /** Kiểm tra user đã đánh giá sản phẩm chưa */
    public boolean hasReviewed(Integer accountId, Integer productId) {
        return reviewRepository.existsByProductIdAndAccountId(productId, accountId);
    }

    /**
     * Tạo mới hoặc cập nhật đánh giá (upsert).
     * Nếu đã có review → cập nhật rating + comment.
     * Nếu chưa có  → tạo mới.
     */
    @Transactional
    public ProductReview saveOrUpdate(Account account, Integer productId, int rating, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + productId));

        ProductReview review = reviewRepository
                .findByProductIdAndAccountId(productId, account.getId())
                .orElse(new ProductReview());

        review.setProduct(product);
        review.setAccount(account);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        return reviewRepository.save(review);
    }
}