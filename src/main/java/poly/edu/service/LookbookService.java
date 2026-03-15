package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.entity.*;
import poly.edu.repository.LookbookPostRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LookbookService {

    private final LookbookPostRepository lookbookRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final FileService fileService;

    /**
     * Create a new Lookbook post.
     * Validates that the user owns the order and hasn't already posted for it.
     */
    @Transactional
    public LookbookPost createPost(Account account, Integer orderId, Integer productId,
                                    String caption, MultipartFile imageFile) throws IOException {
        // Validate order ownership
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!order.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("Bạn không có quyền đăng bài cho đơn hàng này");
        }

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể đăng bài cho đơn hàng đã hoàn thành");
        }

        // Prevent duplicate
        if (lookbookRepo.existsByOrderIdAndAccountId(orderId, account.getId())) {
            throw new RuntimeException("Bạn đã đăng lookbook cho đơn hàng này rồi");
        }

        // Validate product exists
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Save image
        String imagePath = fileService.save(imageFile);
        if (imagePath == null || imagePath.isBlank()) {
            throw new RuntimeException("Vui lòng chọn ảnh để đăng");
        }

        // Create post
        LookbookPost post = new LookbookPost();
        post.setAccount(account);
        post.setProduct(product);
        post.setOrder(order);
        post.setImagePath(imagePath);
        post.setCaption(caption != null ? caption.trim() : "");
        post.setStatus("PENDING");

        return lookbookRepo.save(post);
    }

    /** Public feed — approved posts only */
    public List<LookbookPost> getApprovedPosts() {
        return lookbookRepo.findByStatusOrderByCreatedAtDesc("APPROVED");
    }

    /** User's own posts (all statuses) */
    public List<LookbookPost> getPostsByAccount(Integer accountId) {
        return lookbookRepo.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    /** Admin — all posts */
    public List<LookbookPost> getAllPosts() {
        return lookbookRepo.findAllByOrderByCreatedAtDesc();
    }

    /** Check if user already posted for this order */
    public boolean hasPosted(Integer orderId, Integer accountId) {
        return lookbookRepo.existsByOrderIdAndAccountId(orderId, accountId);
    }

    /** Admin approve */
    @Transactional
    public void approve(Integer postId) {
        LookbookPost post = lookbookRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        post.setStatus("APPROVED");
        lookbookRepo.save(post);
    }

    /** Admin reject */
    @Transactional
    public void reject(Integer postId) {
        LookbookPost post = lookbookRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        post.setStatus("REJECTED");
        lookbookRepo.save(post);
    }

    /** Admin delete */
    @Transactional
    public void deletePost(Integer postId) {
        lookbookRepo.deleteById(postId);
    }
}
