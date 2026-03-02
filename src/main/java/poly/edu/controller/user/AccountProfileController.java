package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.dto.ProfileForm;
import poly.edu.entity.Account;
import poly.edu.entity.Order;
import poly.edu.entity.Product;
import poly.edu.entity.ProductReview;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.ProductReviewRepository;
import poly.edu.service.AccountService;
import poly.edu.service.FileService;
import poly.edu.service.OrderQueryService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AccountProfileController - Manages user profile, orders, reviews, addresses.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why split from AccountController?"
 *
 * The original AccountController (242 lines) mixed:
 * 1. Profile management (view, edit) ← THIS CONTROLLER
 * 2. Order history (list, cancel) ← THIS CONTROLLER
 * 3. Registration (sign-up) → AccountAuthController
 *
 * Profile/orders are for authenticated users managing their "My Account"
 * section.
 * Registration is a public, unauthenticated flow with different dependencies
 * (EmailVerificationService, SignUpForm validation).
 *
 * "Why keep orders here and not in a separate OrderController?"
 * - These aren't order management endpoints (that's admin's job)
 * - These are "My Orders" - part of the account/profile experience
 * - Same authentication pattern as profile
 */
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountProfileController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final FileService fileService;
    private final OrderQueryService orderQueryService;
    private final OrderRepository orderRepository;
    private final poly.edu.service.OrderCommandService orderCommandService;
    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;

    /**
     * Display user profile page (GET).
     */
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        Account acc = getAuthenticatedAccount(principal);
        if (acc == null)
            return "redirect:/login?error=true";

        ProfileForm form = new ProfileForm();
        form.setUsername(acc.getUsername());
        form.setFullName(acc.getFullName());
        form.setPhone(acc.getPhone());
        form.setEmail(acc.getEmail());
        form.setAvatarUrl(acc.getAvatar());
        form.setBirthDate(acc.getBirthDate());
        form.setGender(acc.getGender());

        model.addAttribute("profileForm", form);
        model.addAttribute("activePage", "profile");
        return "user/account-profile";
    }

    /**
     * Update user profile (POST).
     */
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm form,
            BindingResult binding,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Principal principal,
            RedirectAttributes ra) {

        if (binding.hasErrors())
            return "user/account-profile";

        Account acc = getAuthenticatedAccount(principal);
        if (acc == null)
            return "redirect:/login?error=true";

        acc.setFullName(form.getFullName());
        acc.setPhone(form.getPhone());
        acc.setBirthDate(form.getBirthDate());
        acc.setGender(form.getGender());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarPath = fileService.save(avatarFile);
                acc.setAvatar(avatarPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        accountService.save(acc);
        ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        return "redirect:/account/profile";
    }

    /**
     * Cancel user's own order.
     */
    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam("orderId") Integer orderId,
            Principal principal, RedirectAttributes ra) {
        Account acc = getAuthenticatedAccount(principal);
        if (acc == null)
            return "redirect:/login";

        Optional<Order> orderOpt = orderQueryService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getAccount().getId().equals(acc.getId())) {
                try {
                    orderCommandService.cancelOrder(orderId);
                    ra.addFlashAttribute("success", "Hủy đơn hàng thành công!");
                } catch (Exception e) {
                    ra.addFlashAttribute("error", "Không thể hủy đơn hàng: " + e.getMessage());
                }
            } else {
                ra.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này!");
            }
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
        }
        return "redirect:/account/orders";
    }

    /**
     * Display user's order history.
     */
    @GetMapping("/orders")
    public String orders(Model model, Principal principal) {
        Account acc = getAuthenticatedAccount(principal);
        if (acc == null)
            return "redirect:/login?error=true";

        List<Order> orders = orderRepository.findByAccountIdOrderByOrderDateDesc(
                acc.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();

        model.addAttribute("orders", orders);
        model.addAttribute("activePage", "orders");
        return "user/account-orders";
    }

    /**
     * API endpoint for polling: returns [{orderId, status}] for current user's
     * orders.
     * Used by JavaScript polling to auto-refresh order statuses without page
     * reload.
     */
    @GetMapping("/orders/statuses")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOrderStatuses(Principal principal) {
        Account acc = getAuthenticatedAccount(principal);
        if (acc == null) {
            return ResponseEntity.status(401).build();
        }

        List<Order> orders = orderRepository
                .findByAccountIdOrderByOrderDateDesc(acc.getId(),
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        List<Map<String, Object>> result = orders.stream()
                .map(o -> Map.<String, Object>of(
                        "orderId", o.getId(),
                        "status", o.getStatus()))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/reviews")
    public String reviews(Model model, Principal principal) {
        Account acc = getAuthenticatedAccount(principal);
        if (acc == null)
            return "redirect:/login?error=true";

        List<ProductReview> myReviews = productReviewRepository
                .findByAccountIdOrderByReviewDateDesc(acc.getId());

        model.addAttribute("myReviews", myReviews);
        model.addAttribute("activePage", "reviews");
        return "user/account-reviews";
    }

    /**
     * Submit a product review (AJAX POST from review modal).
     * Body: { productId, rating, comment }
     * Returns 200 OK on success, 400/401 on error.
     */
    @PostMapping("/reviews/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitReview(
            @RequestBody Map<String, Object> body,
            Principal principal) {

        Account acc = getAuthenticatedAccount(principal);
        if (acc == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            Integer productId = Integer.valueOf(body.get("productId").toString());
            Integer rating = Integer.valueOf(body.get("rating").toString());
            String comment = body.containsKey("comment") ? body.get("comment").toString() : "";

            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số sao không hợp lệ"));
            }

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sản phẩm không tồn tại"));
            }

            // Upsert: if already reviewed this product, update; otherwise insert
            ProductReview review = productReviewRepository
                    .findByProductIdAndAccountId(productId, acc.getId())
                    .orElse(new ProductReview());

            review.setProduct(productOpt.get());
            review.setAccount(acc);
            review.setRating(rating);
            review.setComment(comment.isBlank() ? null : comment);

            productReviewRepository.save(review);

            return ResponseEntity.ok(Map.of("success", true, "message", "Đánh giá đã được gửi thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/addresses")
    public String addresses(Model model) {
        model.addAttribute("activePage", "addresses");
        return "user/account-addresses";
    }

    // ===== PRIVATE HELPER =====

    /**
     * Resolve authenticated account from Principal (supports both form and OAuth2
     * login).
     */
    private Account getAuthenticatedAccount(Principal principal) {
        if (principal == null)
            return null;

        String identifier = "";
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            identifier = token.getPrincipal().getAttribute("email");
            if (identifier == null)
                identifier = token.getPrincipal().getName();
        } else {
            identifier = principal.getName();
        }

        Account acc = accountService.findByEmail(identifier);
        if (acc == null) {
            acc = accountRepository.findByUsername(identifier).orElse(null);
        }
        return acc;
    }
}
