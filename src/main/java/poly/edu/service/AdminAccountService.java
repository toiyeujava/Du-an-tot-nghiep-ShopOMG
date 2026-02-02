package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Account;
import poly.edu.entity.Order;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with pagination
     * Algorithm: Simple pagination query
     * Time Complexity: O(n) where n = page size
     */
    public Page<Account> getAllUsers(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    /**
     * Get user by ID
     * Algorithm: Direct lookup by primary key
     * Time Complexity: O(1)
     */
    public Optional<Account> getUserById(Integer id) {
        return accountRepository.findById(id);
    }

    /**
     * Get user's purchase history (orders)
     * Algorithm: Foreign key lookup with pagination
     * Time Complexity: O(n) where n = user's orders
     */
    public Page<Order> getUserOrders(Integer userId, Pageable pageable) {
        return orderRepository.findByAccountIdOrderByOrderDateDesc(userId, pageable);
    }

    /**
     * Lock user account
     * Algorithm: Update isActive flag to false
     * Time Complexity: O(1)
     * 
     * Effect: User cannot login when isActive = false
     */
    @Transactional
    public Account lockAccount(Integer userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + userId));

        // Prevent locking admin accounts
        if (account.getRole() != null && "ADMIN".equals(account.getRole().getName())) {
            throw new IllegalStateException("Cannot lock admin accounts");
        }

        account.setIsActive(false);
        account.setAccountLockedUntil(LocalDateTime.now().plusYears(100)); // Effectively permanent
        return accountRepository.save(account);
    }

    /**
     * Unlock user account
     * Algorithm: Update isActive flag to true
     * Time Complexity: O(1)
     */
    @Transactional
    public Account unlockAccount(Integer userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + userId));

        account.setIsActive(true);
        account.setAccountLockedUntil(null);
        account.setFailedLoginAttempts(0); // Reset failed login attempts
        return accountRepository.save(account);
    }

    /**
     * Reset user password
     * Algorithm: Generate random password + Hash + Update
     * Time Complexity: O(1)
     * 
     * Security: Password is hashed using BCrypt before storage
     */
    @Transactional
    public String resetPassword(Integer userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + userId));

        // Generate random password (8 characters)
        String newPassword = generateRandomPassword();

        // Hash password
        String hashedPassword = passwordEncoder.encode(newPassword);
        account.setPassword(hashedPassword);

        // Reset failed login attempts
        account.setFailedLoginAttempts(0);
        account.setAccountLockedUntil(null);

        accountRepository.save(account);

        // Return plain password (admin should send this to user via email)
        return newPassword;
    }

    /**
     * Delete user account
     * Algorithm: Check constraints + Delete
     * Time Complexity: O(1)
     * 
     * Note: This is a hard delete. Consider soft delete for data retention.
     */
    @Transactional
    public void deleteAccount(Integer userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + userId));

        // Prevent deleting admin accounts
        if (account.getRole() != null && "ADMIN".equals(account.getRole().getName())) {
            throw new IllegalStateException("Cannot delete admin accounts");
        }

        // Check if user has active orders
        long activeOrderCount = orderRepository.findByAccountId(userId).stream()
                .filter(order -> "PENDING".equals(order.getStatus()) ||
                        "CONFIRMED".equals(order.getStatus()) ||
                        "SHIPPING".equals(order.getStatus()))
                .count();

        if (activeOrderCount > 0) {
            throw new IllegalStateException(
                    String.format("Cannot delete account. User has %d active order(s). " +
                            "Please complete or cancel these orders first.", activeOrderCount));
        }

        // Safe to delete
        accountRepository.delete(account);
    }

    /**
     * Search users by keyword (username, email, full name)
     */
    public List<Account> searchUsers(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return accountRepository.findAll().stream()
                .filter(account -> account.getUsername().toLowerCase().contains(lowerKeyword) ||
                        account.getEmail().toLowerCase().contains(lowerKeyword) ||
                        (account.getFullName() != null &&
                                account.getFullName().toLowerCase().contains(lowerKeyword)))
                .toList();
    }

    /**
     * Get users by role
     */
    public List<Account> getUsersByRole(String roleName) {
        return accountRepository.findAll().stream()
                .filter(account -> account.getRole() != null &&
                        roleName.equals(account.getRole().getName()))
                .toList();
    }

    /**
     * Get active users count
     */
    public long getActiveUsersCount() {
        return accountRepository.findAll().stream()
                .filter(account -> account.getIsActive() != null && account.getIsActive())
                .count();
    }

    /**
     * Get locked users count
     */
    public long getLockedUsersCount() {
        return accountRepository.findAll().stream()
                .filter(account -> account.getIsActive() != null && !account.getIsActive())
                .count();
    }

    /**
     * Helper method to generate random password
     * Algorithm: UUID-based random string
     */
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Update user information (admin can edit user details)
     */
    @Transactional
    public Account updateUserInfo(Integer userId, Account updatedInfo) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + userId));

        if (updatedInfo.getFullName() != null) {
            account.setFullName(updatedInfo.getFullName());
        }
        if (updatedInfo.getPhone() != null) {
            account.setPhone(updatedInfo.getPhone());
        }
        if (updatedInfo.getBirthDate() != null) {
            account.setBirthDate(updatedInfo.getBirthDate());
        }
        if (updatedInfo.getGender() != null) {
            account.setGender(updatedInfo.getGender());
        }

        return accountRepository.save(account);
    }
}
