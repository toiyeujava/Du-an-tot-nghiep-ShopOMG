package poly.edu.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import poly.edu.entity.Account;
import poly.edu.service.AccountService;
import poly.edu.service.CartService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final AccountService accountService;
    private final CartService cartService;

    public GlobalModelAttributes(AccountService accountService, CartService cartService) {
        this.accountService = accountService;
        this.cartService = cartService;
    }

    @ModelAttribute("currentUser")
    public Account currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return accountService.findByEmail(principal.getName());
    }
    
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
}

