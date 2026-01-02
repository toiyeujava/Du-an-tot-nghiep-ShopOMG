package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import poly.edu.entity.Account;
import poly.edu.service.AccountService;

@ControllerAdvice
public class CurrentUserAdvice {

    @Autowired
    private AccountService accountService;

    // Hàm này sẽ tự động chạy cho mọi Request và gắn biến "currentUser" vào Model
    @ModelAttribute("currentUser")
    public Account getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            return accountService.findByEmail(email);
        }
        return null; // Trả về null nếu chưa đăng nhập
    }
}