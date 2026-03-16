package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.LookbookPost;

import java.util.List;

@Repository
public interface LookbookPostRepository extends JpaRepository<LookbookPost, Integer> {

    /** Public feed: approved posts, newest first */
    List<LookbookPost> findByStatusOrderByCreatedAtDesc(String status);

    /** User's own posts */
    List<LookbookPost> findByAccountIdOrderByCreatedAtDesc(Integer accountId);

    /** Admin: all posts, newest first */
    List<LookbookPost> findAllByOrderByCreatedAtDesc();

    /** Prevent duplicate: check if user already posted for this order */
    boolean existsByOrderIdAndAccountId(Integer orderId, Integer accountId);
}
