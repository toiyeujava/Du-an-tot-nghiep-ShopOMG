package poly.edu.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import poly.edu.entity.Account;
import poly.edu.service.AccountService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final AccountService accountService;

    public GlobalModelAttributes(AccountService accountService) {
        this.accountService = accountService;
    }

    @ModelAttribute("currentUser")
    public Account currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return accountService.findByEmail(principal.getName());
    }
}
