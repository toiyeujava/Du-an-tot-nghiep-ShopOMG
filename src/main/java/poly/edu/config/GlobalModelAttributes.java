package poly.edu.config;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import poly.edu.entity.Account;
import poly.edu.entity.Cart;
import poly.edu.entity.Category;
import poly.edu.service.AccountService;
import poly.edu.service.CartService;
import poly.edu.service.CategoryService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final AccountService accountService;
    private final CartService cartService;

    
    @Autowired
    private CategoryService categoryService;
    
    public GlobalModelAttributes(AccountService accountService, CartService cartService) {
        this.accountService = accountService;
        this.cartService = cartService;
    }

    // "currentUser" is provided by CurrentUserAdvice (handles OAuth2 properly)
    // Removed duplicate @ModelAttribute("currentUser") to avoid conflict
    
    @ModelAttribute("cartItemCount")
    public Long cartItemCount(Principal principal) {
        if (principal == null) {
            return 0L;
        }
        Account account = accountService.findByEmail(principal.getName());
        if (account == null) {
            return 0L;
        }
        return cartService.getCartItemCount(account.getId());
    }
    
    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            
            // Lưu ý: Nếu báo đỏ ở getCartByUsername, hãy tạo hàm này trong CartService
            // Hoặc đổi thành hàm tương đương bạn đang có, ví dụ: cartService.findByAccountId(account.getId())
            List<Cart> cartItems = cartService.getCartByUsername(username); 
            
            int cartCount = cartItems != null ? cartItems.stream().mapToInt(Cart::getQuantity).sum() : 0;
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartCount", cartCount);
        }
    }
    // Dữ liệu này sẽ tự động được inject vào mọi trang (mọi URL)
    @ModelAttribute("categories")
    public List<Category> getGlobalCategories() {
        try {
            return categoryService.findAll();
        } catch (Exception e) {
            // Prevent DB exceptions from propagating to Spring Security's
            // ExceptionTranslationFilter, which would redirect to /login
            return Collections.emptyList();
        }
    }
    
}