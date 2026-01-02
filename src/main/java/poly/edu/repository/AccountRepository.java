package poly.edu.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Account> findByUsername(String username);
}