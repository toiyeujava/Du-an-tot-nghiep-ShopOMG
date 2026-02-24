package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.CartRepository;
import poly.edu.repository.ProductVariantRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final AccountRepository accountRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public Cart addToCart(Integer accountId, Integer variantId, Integer quantity) {
        // Validate account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Validate product variant
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Check stock
        if (variant.getQuantity() == null || variant.getQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho. Còn lại: " +
                    (variant.getQuantity() != null ? variant.getQuantity() : 0));
        }

        // Check if item already exists in cart
        Optional<Cart> existingCart = cartRepository.findByAccountIdAndVariantId(accountId, variantId);

        if (existingCart.isPresent()) {
            // Update quantity
            Cart cart = existingCart.get();
            Integer newQuantity = cart.getQuantity() + quantity;

            // Validate new quantity against stock
            if (newQuantity > variant.getQuantity()) {
                throw new RuntimeException("Số lượng vượt quá tồn kho. Còn lại: " + variant.getQuantity());
            }

            cart.setQuantity(newQuantity);
            return cartRepository.save(cart);
        } else {
            // Create new cart item
            Cart cart = new Cart();
            cart.setAccount(account);
            cart.setProductVariant(variant);
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public Cart updateQuantity(Integer cartId, Integer accountId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        // Verify ownership
        if (!cart.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("Không có quyền truy cập");
        }

        // Validate quantity
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        // Check stock
        ProductVariant variant = cart.getProductVariant();
        if (variant.getQuantity() == null || variant.getQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho. Còn lại: " +
                    (variant.getQuantity() != null ? variant.getQuantity() : 0));
        }

        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(Integer cartId, Integer accountId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        // Verify ownership
        if (!cart.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("Không có quyền truy cập");
        }

        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public void clearCart(Integer accountId) {
        cartRepository.deleteByAccountId(accountId);
    }

    @Override
    public List<Cart> getCartItems(Integer accountId) {
        return cartRepository.findByAccountId(accountId);
    }

    @Override
    public Long getCartItemCount(Integer accountId) {
        return cartRepository.countByAccountId(accountId);
    }

    @Override
    public Double getCartTotal(Integer accountId) {
        List<Cart> cartItems = cartRepository.findByAccountId(accountId);
        return calculateTotal(cartItems);
    }

    @Override
    public List<Cart> getCartItemsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return java.util.Collections.emptyList();
        return cartRepository.findAllById(ids);
    }

    @Override
    public Double getCartTotalByIds(List<Integer> ids) {
        List<Cart> items = getCartItemsByIds(ids);
        return calculateTotal(items);
    }

    @Override
    @Transactional
    public void removeItemsFromCart(List<Integer> ids, Integer accountId) {
        if (ids == null || ids.isEmpty())
            return;
        List<Cart> items = cartRepository.findAllById(ids);
        // Verify ownership and delete
        items.stream()
                .filter(item -> item.getAccount().getId().equals(accountId))
                .forEach(cartRepository::delete);
    }

    private Double calculateTotal(List<Cart> cartItems) {
        return cartItems.stream()
                .mapToDouble(cart -> {
                    ProductVariant variant = cart.getProductVariant();
                    Product product = variant.getProduct();

                    // Calculate price with discount
                    Double price = product.getPrice();
                    Integer discount = product.getDiscount() != null ? product.getDiscount() : 0;
                    Double finalPrice = price * (100 - discount) / 100.0;

                    return finalPrice * cart.getQuantity();
                })
                .sum();
    }
}
