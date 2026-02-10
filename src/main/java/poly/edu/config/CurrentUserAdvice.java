package poly.edu.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;
import poly.edu.service.AccountService;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserAdvice {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @ModelAttribute("currentUser")
    public Account getCurrentUser(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String identifier = "";

            if (auth instanceof OAuth2AuthenticationToken token) {
                identifier = token.getPrincipal().getAttribute("email");
                if (identifier == null) {
                    identifier = token.getPrincipal().getName();
                }
            } else {
                identifier = auth.getName();
            }

            // Tìm tài khoản đồng bộ
            Account acc = accountService.findByEmail(identifier);
            if (acc == null) {
                acc = accountRepository.findByUsername(identifier).orElse(null);
            }
            return acc;
        }
        return null;
    }
}