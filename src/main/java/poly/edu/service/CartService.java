package poly.edu.service;

import poly.edu.entity.Cart;
import java.util.List;

public interface CartService {
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã tồn tại, cập nhật số lượng
     */
    Cart addToCart(Integer accountId, Integer variantId, Integer quantity);
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    Cart updateQuantity(Integer cartId, Integer accountId, Integer quantity);
    
    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     */
    void removeFromCart(Integer cartId, Integer accountId);
    
    /**
     * Xóa toàn bộ giỏ hàng
     */
    void clearCart(Integer accountId);
    
    /**
     * Lấy danh sách sản phẩm trong giỏ hàng
     */
    List<Cart> getCartItems(Integer accountId);
    
    /**
     * Đếm số lượng sản phẩm trong giỏ hàng
     */
    Long getCartItemCount(Integer accountId);
    
    /**
     * Tính tổng tiền giỏ hàng
     */
    Double getCartTotal(Integer accountId);
}
