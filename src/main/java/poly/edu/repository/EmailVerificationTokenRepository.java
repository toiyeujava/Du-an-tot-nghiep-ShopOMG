package poly.edu.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poly.edu.entity.Account;
import poly.edu.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {

    // Tìm token theo chuỗi token
    Optional<EmailVerificationToken> findByToken(String token);

    // Tìm token chưa hết hạn
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.token = :token AND t.expiryDate > :now")
    Optional<EmailVerificationToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Tìm token theo account
    Optional<EmailVerificationToken> findByAccount(Account account);

    // Xóa tất cả token cũ của một account (khi tạo token mới)
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.account = :account")
    void deleteByAccount(@Param("account") Account account);

    // Xóa các token đã hết hạn (cleanup job)
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
