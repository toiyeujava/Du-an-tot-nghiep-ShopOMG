package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.service.EmailVerificationService;

@Controller
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    /**
     * Xác thực email từ link trong email
     */
    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(required = false) String token,
            Model model,
            RedirectAttributes ra) {

        // Kiểm tra token có được cung cấp không
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "Link xác thực không hợp lệ.");
            return "user/verify-email-error";
        }

        // Xác thực email
        boolean success = emailVerificationService.verifyEmail(token);

        if (success) {
            // Thành công
            return "user/verify-email-success";
        } else {
            // Thất bại (token hết hạn hoặc không hợp lệ)
            model.addAttribute("error", "Link xác thực đã hết hạn hoặc không hợp lệ.");
            return "user/verify-email-error";
        }
    }

    /**
     * Hiển thị trang thông báo đã gửi email verification
     */
    @GetMapping("/verify-email-sent")
    public String verifyEmailSent(Model model) {
        return "user/verify-email-sent";
    }

    /**
     * Gửi lại email xác thực
     */
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

    /**
     * Hiển thị form nhập email để resend verification
     */
    @GetMapping("/resend-verification")
    public String showResendForm(Model model) {
        return "user/resend-verification";
    }
}
