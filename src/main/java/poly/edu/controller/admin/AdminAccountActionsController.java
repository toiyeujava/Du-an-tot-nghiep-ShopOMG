package poly.edu.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;
import poly.edu.service.AdminAccountService;
import poly.edu.service.AuditLogService;

/**
 * AdminAccountActionsController — all mutating (POST) operations on accounts.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why group all POST actions here instead of with the GET detail view?"
 *
 * Write operations are the natural boundary for audit logging. By owning every
 * state-changing action in one class we can:
 * 1. Log consistently — every action calls auditLogService.log() before redirect.
 * 2. Apply a single @PreAuthorize annotation in the future without touching read logic.
 * 3. Test mutations independently (mock AdminAccountService + AuditLogService).
 *
 * "Why resolve the admin Account object rather than just storing the username?"
 *
 * AuditLog.admin is a @ManyToOne FK to Accounts(id). Storing the entity gives
 * us referential integrity and lets us JOIN on admin_id for "who did what"
 * reports. The extra SELECT by username is O(1) indexed and happens only on
 * admin actions — rare enough to be negligible.
 *
 * "Why redirect to /admin/accounts/{id}?tab=logs after each action?"
 *
 * The admin just performed an action — they almost certainly want to see its
 * audit entry. Opening the logs tab automatically saves them a manual click and
 * confirms the action was recorded.
 */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountActionsController {

    private final AdminAccountService adminAccountService;
    private final AuditLogService auditLogService;
    private final AccountRepository accountRepository;

    @PostMapping("/{id}/lock")
    public String lockAccount(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            Account target = adminAccountService.lockAccount(id);
            auditLogService.log(
                    resolveAdmin(authentication), "LOCK", "Account", id,
                    "Khóa tài khoản: " + target.getUsername(),
                    getClientIp(request));
            redirectAttributes.addFlashAttribute("successMessage", "Khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id + "?tab=logs";
    }

    @PostMapping("/{id}/unlock")
    public String unlockAccount(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            Account target = adminAccountService.unlockAccount(id);
            auditLogService.log(
                    resolveAdmin(authentication), "UNLOCK", "Account", id,
                    "Mở khóa tài khoản: " + target.getUsername(),
                    getClientIp(request));
            redirectAttributes.addFlashAttribute("successMessage", "Mở khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id + "?tab=logs";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            Account target = adminAccountService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            String newPassword = adminAccountService.resetPassword(id);
            auditLogService.log(
                    resolveAdmin(authentication), "RESET_PASSWORD", "Account", id,
                    "Đặt lại mật khẩu cho: " + target.getUsername(),
                    getClientIp(request));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reset mật khẩu thành công! Mật khẩu mới: " + newPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id + "?tab=logs";
    }

    @PostMapping("/{id}/role")
    public String changeRole(
            @PathVariable Integer id,
            @RequestParam String roleName,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            Account target = adminAccountService.updateRole(id, roleName, authentication.getName());
            auditLogService.log(
                    resolveAdmin(authentication), "CHANGE_ROLE", "Account", id,
                    "Đổi quyền tài khoản " + target.getUsername() + " → " + roleName,
                    getClientIp(request));
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi quyền thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id + "?tab=logs";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            String targetUsername = adminAccountService.getUserById(id)
                    .map(Account::getUsername).orElse("unknown");
            adminAccountService.deleteAccount(id);
            // Log after delete — account no longer exists in DB but we record the event
            auditLogService.log(
                    resolveAdmin(authentication), "DELETE", "Account", id,
                    "Xóa tài khoản: " + targetUsername,
                    getClientIp(request));
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
            return "redirect:/admin/accounts";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Account resolveAdmin(Authentication authentication) {
        if (authentication == null) return null;
        return accountRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader != null) ? xfHeader.split(",")[0].trim() : request.getRemoteAddr();
    }
}
