package poly.edu.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class PasswordResetController {
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    // ==================== FORGOT PASSWORD ====================
    
    /**
     * Hiển thị form nhập email để reset password
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ForgotPasswordForm());
        }
        return "user/forgot-password";
    }
    
    /**
     * Xử lý gửi email reset password
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("form") ForgotPasswordForm form,
            BindingResult binding,
            RedirectAttributes ra) {
        
        // Validate form
        if (binding.hasErrors()) {
            return "user/forgot-password";
        }
        
        // Tìm account theo email
        Account account = accountService.findByEmail(form.getEmail());
        
        // QUAN TRỌNG: Không tiết lộ email có tồn tại hay không (tránh email enumeration)
        // Luôn hiển thị thông báo thành công
        if (account != null && account.getIsActive()) {
            try {
                // Tạo token và gửi email
                passwordResetService.createPasswordResetToken(account);
            } catch (Exception e) {
                // Log lỗi nhưng vẫn hiển thị thông báo thành công cho user
                e.printStackTrace();
            }
        }
        
        // Luôn redirect với thông báo thành công (security best practice)
        ra.addFlashAttribute("success", 
            "Nếu email này tồn tại trong hệ thống, bạn sẽ nhận được email hướng dẫn đặt lại mật khẩu trong vài phút.");
        
        return "redirect:/forgot-password";
    }
    
    // ==================== RESET PASSWORD ====================
    
    /**
     * Hiển thị form reset password (từ link trong email)
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(required = false) String token,
            Model model,
            RedirectAttributes ra) {
        
        // Kiểm tra token có được cung cấp không
        if (token == null || token.isBlank()) {
            ra.addFlashAttribute("error", "Link không hợp lệ.");
            return "redirect:/login";
        }
        
        // Validate token
        Optional<PasswordResetToken> resetToken = passwordResetService.validateToken(token);
        
        if (resetToken.isEmpty()) {
            ra.addFlashAttribute("error", 
                "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu lại.");
            return "redirect:/forgot-password";
        }
        
        // Token hợp lệ - hiển thị form
        if (!model.containsAttribute("form")) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setToken(token);
            model.addAttribute("form", form);
        }
        
        return "user/reset-password";
    }
    
    /**
     * Xử lý reset password
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("form") ResetPasswordForm form,
            BindingResult binding,
            RedirectAttributes ra) {
        
        // Validate form
        if (binding.hasErrors()) {
            return "user/reset-password";
        }
        
        // Kiểm tra mật khẩu nhập lại
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "error.form", "Mật khẩu nhập lại không khớp!");
            return "user/reset-password";
        }
        
        // Validate token lần nữa
        Optional<PasswordResetToken> resetToken = passwordResetService.validateToken(form.getToken());
        
        if (resetToken.isEmpty()) {
            ra.addFlashAttribute("error", 
                "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }
        
        // Đổi mật khẩu
        PasswordResetToken token = resetToken.get();
        accountService.changePassword(token.getAccount(), form.getNewPassword());
        
        // Đánh dấu token đã sử dụng
        passwordResetService.markTokenAsUsed(token);
        
        // Thông báo thành công
        ra.addFlashAttribute("success", 
            "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
        
        return "redirect:/login";
    }
}
