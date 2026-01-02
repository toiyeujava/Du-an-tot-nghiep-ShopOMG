package poly.edu.security;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Cho phép đăng nhập bằng Email hoặc Username
        Account acc = accountRepository.findByEmail(identifier)
            .orElseGet(() -> accountRepository.findByUsername(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + identifier)));

        String roleName = acc.getRole() != null ? acc.getRole().getName() : "USER";
        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        // SỬA: Dùng isActive (Boolean) thay vì status (String)
        boolean enabled = acc.getIsActive() != null && acc.getIsActive();

        // Dùng email làm principal để đồng bộ với profile, nhưng login có thể nhập username
        return User.withUsername(acc.getEmail())
                .password(acc.getPassword())
                .authorities(auths)
                .disabled(!enabled)
                .build();
    }
}