package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import poly.edu.dto.ForgotPasswordForm;
import poly.edu.dto.ResetPasswordForm;
import poly.edu.entity.Account;
import poly.edu.entity.PasswordResetToken;
import poly.edu.service.AccountService;
import poly.edu.service.PasswordResetService;

import java.util.Optional;

/**
 * PasswordResetController - Handles forgot/reset password flow.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why is this in user/ and not common/?"
 *
 * While password reset is technically a pre-authentication flow,
 * it's tightly coupled to user accounts. It modifies user data
 * (password field) and uses AccountService directly.
 *
 * "Why always show 'success' even when email doesn't exist?"
 * - Security best practice: prevents email enumeration attacks
 * - Attacker can't determine if an email is registered
 * - Same response regardless of whether email exists in DB
 */
@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final AccountService accountService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ForgotPasswordForm());
        }
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("form") ForgotPasswordForm form,
            BindingResult binding,
            RedirectAttributes ra) {

        if (binding.hasErrors()) {
            return "user/forgot-password";
        }

        Account account = accountService.findByEmail(form.getEmail());

        if (account != null && account.getIsActive()) {
            try {
                passwordResetService.createPasswordResetToken(account);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ra.addFlashAttribute("success",
                "Nếu email này tồn tại trong hệ thống, bạn sẽ nhận được email hướng dẫn đặt lại mật khẩu trong vài phút.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(required = false) String token,
            Model model,
            RedirectAttributes ra) {

        if (token == null || token.isBlank()) {
            ra.addFlashAttribute("error", "Link không hợp lệ.");
            return "redirect:/login";
        }

        Optional<PasswordResetToken> resetToken = passwordResetService.validateToken(token);

        if (resetToken.isEmpty()) {
            ra.addFlashAttribute("error",
                    "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.");
            return "redirect:/forgot-password";
        }

        if (!model.containsAttribute("form")) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setToken(token);
            model.addAttribute("form", form);
        }
        return "user/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("form") ResetPasswordForm form,
            BindingResult binding,
            RedirectAttributes ra) {

        if (binding.hasErrors()) {
            return "user/reset-password";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "error.form", "Mật khẩu nhập lại không khớp!");
            return "user/reset-password";
        }

        Optional<PasswordResetToken> resetToken = passwordResetService.validateToken(form.getToken());

        if (resetToken.isEmpty()) {
            ra.addFlashAttribute("error",
                    "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }

        PasswordResetToken token = resetToken.get();
        accountService.changePassword(token.getAccount(), form.getNewPassword());
        passwordResetService.markTokenAsUsed(token);

        ra.addFlashAttribute("success",
                "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
        return "redirect:/login";
    }
}
