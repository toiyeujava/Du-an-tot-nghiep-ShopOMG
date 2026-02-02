package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Cart;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    /**
     * Tìm tất cả sản phẩm trong giỏ hàng của một tài khoản
     */
    @Query("SELECT c FROM Cart c WHERE c.account.id = :accountId ORDER BY c.createdAt DESC")
    List<Cart> findByAccountId(@Param("accountId") Integer accountId);
    
    /**
     * Tìm sản phẩm cụ thể trong giỏ hàng (để kiểm tra trùng lặp)
     */
    @Query("SELECT c FROM Cart c WHERE c.account.id = :accountId AND c.productVariant.id = :variantId")
    Optional<Cart> findByAccountIdAndVariantId(@Param("accountId") Integer accountId, 
                                                 @Param("variantId") Integer variantId);
    
    /**
     * Đếm số lượng sản phẩm trong giỏ hàng
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.account.id = :accountId")
    Long countByAccountId(@Param("accountId") Integer accountId);
    
    /**
     * Xóa tất cả sản phẩm trong giỏ hàng của một tài khoản
     */
    void deleteByAccountId(Integer accountId);
    
    /**
     * Xóa một sản phẩm cụ thể trong giỏ hàng
     */
    @Query("DELETE FROM Cart c WHERE c.id = :cartId AND c.account.id = :accountId")
    void deleteByIdAndAccountId(@Param("cartId") Integer cartId, @Param("accountId") Integer accountId);
}
