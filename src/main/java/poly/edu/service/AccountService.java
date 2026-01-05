package poly.edu.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import poly.edu.entity.Account;
import poly.edu.entity.Role;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.RoleRepository;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email){
        return accountRepository.existsByEmail(email);
    }

    public boolean usernameExists(String username){
        return accountRepository.existsByUsername(username);
    }

    @Transactional
    public Account register(@Valid Account acc){
        // Tìm Role USER
        Role roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("ROLE USER chưa tồn tại"));
        
        acc.setRole(roleUser);
        acc.setPassword(passwordEncoder.encode(acc.getPassword()));
        acc.setIsActive(true); // Set mặc định kích hoạt
        
        return accountRepository.save(acc);
    }

    public Account findByEmail(String email){
        return accountRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public Account updateProfile(Account acc, String fullName, String phone, String avatarUrl){
        if(fullName != null) acc.setFullName(fullName);
        acc.setPhone(phone);
        
        if(avatarUrl != null && !avatarUrl.isBlank()){
            acc.setAvatar(avatarUrl);
        }
        return accountRepository.save(acc);
    }

    // --- BỔ SUNG ĐỂ FIX LỖI TẠI ACCOUNT CONTROLLER ---
    public void save(Account account) {
        accountRepository.save(account);
    }
    
    /**
     * Đổi mật khẩu cho tài khoản (dùng cho reset password)
     * @param account Tài khoản cần đổi mật khẩu
     * @param newPassword Mật khẩu mới (chưa mã hóa)
     */
    @Transactional
    public void changePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}