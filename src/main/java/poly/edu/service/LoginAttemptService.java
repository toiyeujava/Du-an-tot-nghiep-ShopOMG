package poly.edu.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.Account;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final AccountService accountService;

    // Cấu hình
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    /**
     * Ghi nhận lần đăng nhập thất bại
     * 
     * @param email Email của tài khoản
     */
    @Transactional
    public void recordFailedAttempt(String email) {
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return; // Không tăng attempts nếu email không tồn tại (security)
        }

        int attempts = (account.getFailedLoginAttempts() != null ? account.getFailedLoginAttempts() : 0) + 1;
        account.setFailedLoginAttempts(attempts);

        // Khóa tài khoản sau MAX_ATTEMPTS lần sai
        if (attempts >= MAX_ATTEMPTS) {
            account.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        accountService.save(account);
    }

    /**
     * Ghi nhận đăng nhập thành công - reset attempts
     * 
     * @param email Email của tài khoản
     */
    @Transactional
    public void recordSuccessfulLogin(String email) {
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return;
        }

        // Reset failed attempts về 0
        account.setFailedLoginAttempts(0);
        account.setAccountLockedUntil(null);
        account.setLastLogin(LocalDateTime.now());

        accountService.save(account);
    }

    /**
     * Kiểm tra tài khoản có bị khóa không
     * 
     * @param email Email của tài khoản
     * @return true nếu đang bị khóa, false nếu không
     */
    public boolean isAccountLocked(String email) {
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return false;
        }

        LocalDateTime lockedUntil = account.getAccountLockedUntil();
        if (lockedUntil == null) {
            return false;
        }

        // Kiểm tra còn trong thời gian khóa không
        return lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Lấy số lần thử còn lại
     * 
     * @param email Email của tài khoản
     * @return Số lần thử còn lại (0-5)
     */
    public int getRemainingAttempts(String email) {
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return MAX_ATTEMPTS; // Không tiết lộ email có tồn tại hay không
        }

        int failedAttempts = account.getFailedLoginAttempts() != null ? account.getFailedLoginAttempts() : 0;
        int remaining = MAX_ATTEMPTS - failedAttempts;

        return Math.max(0, remaining);
    }

    /**
     * Unlock tài khoản thủ công (dành cho admin)
     * 
     * @param email Email của tài khoản
     */
    @Transactional
    public void unlockAccount(String email) {
        Account account = accountService.findByEmail(email);
        if (account == null) {
            return;
        }

        account.setFailedLoginAttempts(0);
        account.setAccountLockedUntil(null);

        accountService.save(account);
    }

    /**
     * Lấy thời gian còn lại bị khóa (phút)
     * 
     * @param email Email của tài khoản
     * @return Số phút còn lại, 0 nếu không bị khóa
     */
    public long getMinutesUntilUnlock(String email) {
        Account account = accountService.findByEmail(email);
        if (account == null || account.getAccountLockedUntil() == null) {
            return 0;
        }

        LocalDateTime lockedUntil = account.getAccountLockedUntil();
        LocalDateTime now = LocalDateTime.now();

        if (lockedUntil.isBefore(now)) {
            return 0;
        }

        return java.time.Duration.between(now, lockedUntil).toMinutes();
    }
}
