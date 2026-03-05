package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Account;
import poly.edu.entity.ProductReview;
import poly.edu.repository.AccountRepository;
import poly.edu.service.ProductReviewService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * ProductReviewController
 *
 * CHỈ xử lý 1 việc:
 *   GET /api/reviews/{productId} → trả JSON cho product-detail.html (AJAX)
 *
 * Dùng Principal (giống AccountProfileController) để tương thích cả
 * form login lẫn OAuth2 login.
 */
@RestController
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;
    private final AccountRepository    accountRepository;

    @GetMapping("/api/reviews/{productId}")
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @PathVariable Integer productId,
            Principal principal) {

        List<ProductReview> reviews = reviewService.getReviewsByProduct(productId);
        Double avg = reviewService.getAverageRating(productId);

        // Đếm từng sao (index 1-5)
        long[] sc = new long[6];
        for (ProductReview r : reviews) sc[r.getRating()]++;

        // Build danh sách JSON-safe (tránh circular reference từ JPA entity)
        List<Map<String, Object>> reviewList = reviews.stream().map(r -> {
            Account acc = r.getAccount();
            String avatar = (acc.getAvatar() != null && !acc.getAvatar().isBlank())
                    ? acc.getAvatar()
                    : "https://ui-avatars.com/api/?name="
                        + acc.getFullName().replace(" ", "+")
                        + "&background=8B0000&color=fff";
            return Map.<String, Object>of(
                    "id",         r.getId(),
                    "username",   acc.getUsername(),
                    "fullName",   acc.getFullName(),
                    "avatar",     avatar,
                    "rating",     r.getRating(),
                    "comment",    r.getComment() != null ? r.getComment() : "",
                    "reviewDate", r.getReviewDate() != null
                                    ? r.getReviewDate().toLocalDate().toString() : ""
            );
        }).toList();

        // Kiểm tra quyền đánh giá — an toàn khi chưa đăng nhập
        boolean canReview   = false;
        boolean hasReviewed = false;

        if (principal != null) {
            Account account = resolveAccount(principal);
            if (account != null) {
                canReview   = reviewService.hasPurchased(account.getId(), productId);
                hasReviewed = reviewService.hasReviewed(account.getId(), productId);
            }
        }

        return ResponseEntity.ok(Map.of(
                "reviews",     reviewList,
                "average",     avg,
                "total",       reviews.size(),
                "starCounts",  new long[]{ 0, sc[1], sc[2], sc[3], sc[4], sc[5] },
                "canReview",   canReview,
                "hasReviewed", hasReviewed
        ));
    }

    /**
     * Resolve Account từ Principal — hỗ trợ cả form login và OAuth2.
     * Copy logic từ AccountProfileController để nhất quán.
     */
    private Account resolveAccount(Principal principal) {
        try {
            String identifier;
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
                identifier = token.getPrincipal().getAttribute("email");
                if (identifier == null) identifier = token.getPrincipal().getName();
            } else {
                identifier = principal.getName();
            }

            // Thử tìm theo email trước, rồi username
            Account acc = accountRepository.findByEmail(identifier).orElse(null);
            if (acc == null) {
                acc = accountRepository.findByUsername(identifier).orElse(null);
            }
            return acc;
        } catch (Exception e) {
            return null;
        }
    }
}