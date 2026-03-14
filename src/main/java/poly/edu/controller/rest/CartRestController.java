package poly.edu.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.repository.AccountRepository;
import poly.edu.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;
    private final AccountRepository accountRepository;

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok(0);
        }

        return accountRepository.findByUsername(auth.getName())
                .map(account -> {
                    Long countObj = cartService.getCartItemCount(account.getId());
                    int count = (countObj != null) ? countObj.intValue() : 0;
                    return ResponseEntity.ok(count);
                })
                .orElse(ResponseEntity.ok(0));
    }
}
