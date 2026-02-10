package poly.edu.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.service.EmailVerificationService;

/**
 * EmailVerificationController - Handles email verification flow.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why common/ and not user/?"
 * Email verification is a pre-authentication flow. The user may not
 * even have a session yet - they're clicking a link from their email.
 * It's part of the registration pipeline, not the authenticated user area.
 */
@Controller
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(required = false) String token,
            Model model,
            RedirectAttributes ra) {

        if (token == null || token.isBlank()) {
            model.addAttribute("error", "Link xác thực không hợp lệ.");
            return "user/verify-email-error";
        }

        boolean success = emailVerificationService.verifyEmail(token);

        if (success) {
            return "user/verify-email-success";
        } else {
            model.addAttribute("error", "Link xác thực đã hết hạn hoặc không hợp lệ.");
            return "user/verify-email-error";
        }
    }

    @GetMapping("/verify-email-sent")
    public String verifyEmailSent(Model model) {
        return "user/verify-email-sent";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(
            @RequestParam String email,
            RedirectAttributes ra) {

        boolean success = emailVerificationService.resendVerificationEmail(email);

        if (success) {
            ra.addFlashAttribute("success",
                    "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư của bạn.");
        } else {
            ra.addFlashAttribute("error",
                    "Không thể gửi email. Email không tồn tại hoặc đã được xác thực.");
        }

        return "redirect:/verify-email-sent";
    }

    @GetMapping("/resend-verification")
    public String showResendForm(Model model) {
        return "user/resend-verification";
    }
}
