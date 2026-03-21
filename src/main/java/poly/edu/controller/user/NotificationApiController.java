package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;
import poly.edu.service.AccountService;
import poly.edu.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();
        Account account = getAuthenticatedAccount();
        
        if (account == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        notificationService.markAsRead(id, account.getId());
        
        response.put("success", true);
        response.put("unreadCount", notificationService.getUnreadCount(account.getId()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        Map<String, Object> response = new HashMap<>();
        Account account = getAuthenticatedAccount();
        
        if (account == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        notificationService.markAllAsRead(account.getId());
        
        response.put("success", true);
        response.put("unreadCount", 0);
        return ResponseEntity.ok(response);
    }

    private Account getAuthenticatedAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String identifier = auth.getName();
        return accountRepository.findByUsername(identifier)
                .orElseGet(() -> accountService.findByEmail(identifier));
    }
}
