package poly.edu.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.Account;
import poly.edu.entity.PasswordResetToken;
import poly.edu.repository.PasswordResetTokenRepository;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    /**
     * Tạo token reset password và gửi email
     * 
     * @param account Tài khoản cần reset password
     * @return Token đã tạo
     */
    @Transactional
    public String createPasswordResetToken(Account account) {
        // Xóa tất cả token cũ của account này
        tokenRepository.deleteByAccount(account);

        // Tạo token mới (UUID random)
        String tokenString = UUID.randomUUID().toString();

        // Tạo entity
        PasswordResetToken token = new PasswordResetToken();
        token.setAccount(account);
        token.setToken(tokenString);
        token.setExpiryDate(LocalDateTime.now().plusHours(1)); // Hết hạn sau 1 giờ
        token.setUsed(false);

        // Lưu vào DB
        tokenRepository.save(token);

        // Gửi email
        emailService.sendPasswordResetEmail(
                account.getEmail(),
                account.getFullName(),
                tokenString);

        return tokenString;
    }

    /**
     * Validate token reset password
     * 
     * @param tokenString Chuỗi token
     * @return Optional<PasswordResetToken> nếu token hợp lệ
     */
    public Optional<PasswordResetToken> validateToken(String tokenString) {
        return tokenRepository.findValidToken(tokenString, LocalDateTime.now());
    }

    /**
     * Đánh dấu token đã sử dụng
     * 
     * @param token Token cần đánh dấu
     */
    @Transactional
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }

    /**
     * Xóa các token đã hết hạn (cleanup job - có thể chạy định kỳ)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
