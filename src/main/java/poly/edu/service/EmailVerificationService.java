package poly.edu.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.Account;
import poly.edu.entity.EmailVerificationToken;
import poly.edu.repository.EmailVerificationTokenRepository;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final AccountService accountService;

    /**
     * Tạo token verification và gửi email
     * 
     * @param account Tài khoản cần verify
     * @return Token đã tạo
     */
    @Transactional
    public String createVerificationToken(Account account) {
        // Xóa tất cả token cũ của account này
        tokenRepository.deleteByAccount(account);

        // Tạo token mới (UUID random)
        String tokenString = UUID.randomUUID().toString();

        // Tạo entity
        EmailVerificationToken token = new EmailVerificationToken();
        token.setAccount(account);
        token.setToken(tokenString);
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // Hết hạn sau 24 giờ

        // Lưu vào DB
        tokenRepository.save(token);

        // Gửi email
        emailService.sendVerificationEmail(
                account.getEmail(),
                account.getFullName(),
                tokenString);

        return tokenString;
    }

    /**
     * Validate token verification
     * 
     * @param tokenString Chuỗi token
     * @return Optional<EmailVerificationToken> nếu token hợp lệ
     */
    public Optional<EmailVerificationToken> validateToken(String tokenString) {
        return tokenRepository.findValidToken(tokenString, LocalDateTime.now());
    }

    /**
     * Xác thực email và cập nhật account
     * 
     * @param tokenString Chuỗi token
     * @return true nếu thành công, false nếu token không hợp lệ
     */
    @Transactional
    public boolean verifyEmail(String tokenString) {
        Optional<EmailVerificationToken> tokenOpt = validateToken(tokenString);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        EmailVerificationToken token = tokenOpt.get();
        Account account = token.getAccount();

        // Cập nhật email_verified = true
        account.setEmailVerified(true);
        accountService.save(account);

        // Xóa token sau khi sử dụng
        tokenRepository.delete(token);

        return true;
    }

    /**
     * Gửi lại email xác thực
     * 
     * @param email Email của account
     * @return true nếu gửi thành công, false nếu email không tồn tại hoặc đã
     *         verified
     */
    @Transactional
    public boolean resendVerificationEmail(String email) {
        Account account = accountService.findByEmail(email);

        if (account == null) {
            return false; // Email không tồn tại
        }

        if (account.getEmailVerified() != null && account.getEmailVerified()) {
            return false; // Đã verified rồi
        }

        // Tạo token mới và gửi email
        createVerificationToken(account);

        return true;
    }

    /**
     * Xóa các token đã hết hạn (cleanup job - có thể chạy định kỳ)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
