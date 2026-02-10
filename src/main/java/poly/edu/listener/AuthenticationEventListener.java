package poly.edu.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import poly.edu.service.LoginAttemptService;

@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    /**
     * Lắng nghe sự kiện đăng nhập thành công
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String email = event.getAuthentication().getName();

        // Reset failed attempts về 0 và cập nhật last_login
        loginAttemptService.recordSuccessfulLogin(email);
    }

    /**
     * Lắng nghe sự kiện đăng nhập thất bại (sai mật khẩu)
     */
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof String) {
            String email = (String) principal;

            // Tăng failed attempts và khóa tài khoản nếu cần
            loginAttemptService.recordFailedAttempt(email);
        }
    }
}
