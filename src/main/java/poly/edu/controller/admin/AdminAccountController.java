package poly.edu.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Account;
import poly.edu.entity.Order;
import poly.edu.service.AdminAccountService;
import poly.edu.service.ExcelExportService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AdminAccountController - Handles user account management.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why have lock/unlock instead of just delete?"
 * 
 * 1. Data Retention: Order history needs the account for reports
 * 2. Reversibility: Locked accounts can be unlocked if needed
 * 3. Security: Lock suspicious accounts while investigating
 * 4. Legal: May need to preserve data for compliance
 * 
 * "How does locking work?"
 * 
 * When locked:
 * - isActive = false
 * - accountLockedUntil = now + 100 years (effectively permanent)
 * - User sees "Account locked" message on login attempt
 * 
 * When unlocked:
 * - isActive = true
 * - accountLockedUntil = null
 * - failedLoginAttempts = 0 (reset brute-force counter)
 * 
 * "Why reset password instead of send reset link?"
 * 
 * Admin reset is for emergency cases:
 * - User lost access to email
 * - Account compromised
 * - Support ticket
 * 
 * The new password is shown once, admin should communicate it securely.
 * 
 * Time Complexity:
 * - lockAccount(): O(1)
 * - unlockAccount(): O(1)
 * - resetPassword(): O(1) for UUID + BCrypt (constant time)
 * - deleteAccount(): O(m) where m = user's orders to check
 */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;
    private final ExcelExportService excelExportService;

    /**
     * List all user accounts with pagination.
     */
    @GetMapping
    public String accounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý tài khoản");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Account> accountPage = adminAccountService.getAllUsers(pageable);

        model.addAttribute("accounts", accountPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accountPage.getTotalPages());
        model.addAttribute("totalItems", accountPage.getTotalElements());

        // User statistics
        model.addAttribute("activeUsers", adminAccountService.getActiveUsersCount());
        model.addAttribute("lockedUsers", adminAccountService.getLockedUsersCount());

        return "admin/accounts";
    }

    /**
     * Export accounts to Excel file.
     * 
     * Time Complexity: O(n) where n = number of users
     */
    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Account> allUsers = adminAccountService.getAllUsers(pageable);
        excelExportService.exportAccounts(allUsers.getContent(), response.getOutputStream());
    }

    /**
     * View account details with order history.
     * 
     * Why include order history?
     * - VIP customer identification
     * - Support context (what did they buy?)
     * - Fraud detection (unusual patterns)
     */
    @GetMapping("/{id}")
    public String accountDetail(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Account account = adminAccountService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        model.addAttribute("pageTitle", "Chi tiết tài khoản: " + account.getUsername());
        model.addAttribute("account", account);

        // Get user's orders
        Pageable pageable = PageRequest.of(page, 10);
        Page<Order> orders = adminAccountService.getUserOrders(id, pageable);
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        return "admin/account-detail";
    }

    /**
     * Lock user account.
     * 
     * Algorithm:
     * 1. Check if account is ADMIN → throw exception
     * 2. Set isActive = false
     * 3. Set accountLockedUntil = now + 100 years
     * 
     * Security: Admins cannot lock other admin accounts
     */
    @PostMapping("/{id}/lock")
    public String lockAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.lockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    /**
     * Unlock user account.
     * 
     * Algorithm:
     * 1. Set isActive = true
     * 2. Set accountLockedUntil = null
     * 3. Reset failedLoginAttempts = 0
     */
    @PostMapping("/{id}/unlock")
    public String unlockAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.unlockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mở khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    /**
     * Reset user password.
     * 
     * Algorithm:
     * 1. Generate random password (UUID first 8 chars, uppercase)
     * 2. Hash with BCrypt
     * 3. Update account
     * 4. Reset failedLoginAttempts = 0
     * 5. Return plain password to show admin
     * 
     * Security Note:
     * - Password shown only once
     * - Admin should communicate it securely (phone, in-person)
     * - Recommend user change on next login
     */
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            String newPassword = adminAccountService.resetPassword(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reset mật khẩu thành công! Mật khẩu mới: " + newPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    /**
     * Delete user account (hard delete).
     * 
     * Algorithm:
     * 1. Check if account is ADMIN → throw exception
     * 2. Check if user has active orders → throw exception
     * 3. Hard delete account
     * 
     * Why not soft delete?
     * - GDPR "right to be forgotten"
     * - Lock is sufficient for most cases
     * - This is for complete removal requests
     * 
     * Why check active orders?
     * - Cannot delete user with pending orders
     * - Complete orders first, then delete
     */
    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
