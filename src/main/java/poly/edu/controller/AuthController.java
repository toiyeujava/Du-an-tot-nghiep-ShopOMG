package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import poly.edu.service.LoginAttemptService;

@Controller
public class AuthController {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @GetMapping("/login")
    public String showLoginForm(Model model,
            String email,
            String error,
            String locked) {

        // Nếu có email parameter, kiểm tra số lần thử còn lại
        if (email != null && !email.isBlank()) {
            int remaining = loginAttemptService.getRemainingAttempts(email);
            model.addAttribute("remainingAttempts", remaining);
            model.addAttribute("email", email);

            // Kiểm tra xem có bị locked không
            boolean isLocked = loginAttemptService.isAccountLocked(email);
            if (isLocked) {
                long minutesLeft = loginAttemptService.getMinutesUntilUnlock(email);
                model.addAttribute("minutesUntilUnlock", minutesLeft);
            }
        }

        return "user/login";
    }

    @GetMapping("/register")
    public String redirectToSignUp() {
        return "redirect:/account/sign-up";
    }

    @GetMapping("/auth/require-login")
    public String requireLogin(@RequestParam("target") String targetUrl, HttpSession session) {
        if (targetUrl != null && !targetUrl.isBlank()) {
            session.setAttribute("redirectAfterLogin", targetUrl);
        }
        return "redirect:/login";
    }

    // XÓA HẾT CÁC PHẦN ĐĂNG KÝ (REGISTER) Ở ĐÂY ĐỂ TRÁNH XUNG ĐỘT
}