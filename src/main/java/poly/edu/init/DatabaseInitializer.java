package poly.edu.init;

import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;

/**
 * Khởi tạo / sửa dữ liệu demo trong DB khi ứng dụng start.
 *
 * Mục đích chính: cập nhật lại mật khẩu cho các tài khoản seed sẵn
 * (admin, khách hàng demo ...) dùng chung một chuỗi placeholder
 * để có thể đăng nhập bằng mật khẩu 123456 như yêu cầu.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // Chuỗi password placeholder đang có trong script SQL seed
    private static final String PLACEHOLDER_PASSWORD = "$2a$12$JD2.7/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s";

    // Mật khẩu mặc định mong muốn cho các tài khoản demo
    private static final String DEFAULT_DEMO_PASSWORD = "123456";

    public DatabaseInitializer(AccountRepository accountRepository,
                               PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Tìm các account vẫn đang dùng chuỗi placeholder
        List<Account> accounts = accountRepository.findAll();
        boolean updated = false;
        for (Account acc : accounts) {
            if (PLACEHOLDER_PASSWORD.equals(acc.getPassword())) {
                acc.setPassword(passwordEncoder.encode(DEFAULT_DEMO_PASSWORD));
                if (acc.getIsActive() == null) {
                    acc.setIsActive(true);
                }
                accountRepository.save(acc);
                updated = true;
            }
        }

        if (updated) {
            System.out.println("[DatabaseInitializer] Đã cập nhật lại mật khẩu demo (123456) cho các tài khoản seed.");
        }

        // Đảm bảo riêng tài khoản admin luôn đăng nhập được với mật khẩu demo
        accountRepository.findByUsername("admin").ifPresent(acc -> {
            acc.setPassword(passwordEncoder.encode(DEFAULT_DEMO_PASSWORD));
            acc.setIsActive(true);
            accountRepository.save(acc);
            System.out.println("[DatabaseInitializer] Đã đảm bảo mật khẩu demo cho tài khoản admin.");
        });
    }
}
