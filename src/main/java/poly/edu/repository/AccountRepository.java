package poly.edu.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import poly.edu.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Account> findByUsername(String username);
    
    Page<Account> findByRoleName(String roleName, Pageable pageable);

    @Query("SELECT a.email FROM Account a WHERE a.role.id = 2")
    List<String> findAllUsernames();
    
    // Tìm kiếm khách hàng theo tên, email, SĐT
    @Query("SELECT a FROM Account a WHERE a.role.name = 'USER' AND " +
           "(:keyword IS NULL OR " +
           " LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(a.email)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " a.phone           LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY a.createdAt DESC")
    Page<Account> searchCustomers(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            Pageable pageable);
}