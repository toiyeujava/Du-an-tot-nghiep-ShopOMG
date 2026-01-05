package poly.edu.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Account;
import poly.edu.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    
    // Tìm token theo chuỗi token
    Optional<PasswordResetToken> findByToken(String token);
    
    // Tìm token chưa sử dụng và chưa hết hạn
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.used = false AND t.expiryDate > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    // Xóa tất cả token cũ của một account (khi tạo token mới)
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.account = :account")
    void deleteByAccount(@Param("account") Account account);
    
    // Xóa các token đã hết hạn (cleanup job)
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
