package poly.edu.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import poly.edu.dto.SignUpForm;
import poly.edu.service.LoginAttemptService;

/**
 * AuthController - Handles login page and authentication redirects.
 *
 * The login page now includes both login AND register forms
 * with a flip-card toggle switch for seamless switching.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final LoginAttemptService loginAttemptService;

    @GetMapping("/login")
    public String showLoginForm(Model model,
            String email,
            String error,
            String locked,
            @RequestParam(name = "mode", required = false) String mode) {

        if (email != null && !email.isBlank()) {
            int remaining = loginAttemptService.getRemainingAttempts(email);
            model.addAttribute("remainingAttempts", remaining);
            model.addAttribute("email", email);

            boolean isLocked = loginAttemptService.isAccountLocked(email);
            if (isLocked) {
                long minutesLeft = loginAttemptService.getMinutesUntilUnlock(email);
                model.addAttribute("minutesUntilUnlock", minutesLeft);
            }
        }

        // Provide empty SignUpForm for the register side of the flip card
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignUpForm());
        }

        // If mode=register, tell template to show register side
        if ("register".equals(mode)) {
            model.addAttribute("showRegister", true);
        }

        return "user/login";
    }

    @GetMapping("/register")
    public String redirectToSignUp() {
        return "redirect:/login?mode=register";
    }

    @GetMapping("/auth/require-login")
    public String requireLogin(@RequestParam("target") String targetUrl, HttpSession session) {
        if (targetUrl != null && !targetUrl.isBlank()) {
            session.setAttribute("redirectAfterLogin", targetUrl);
        }
        return "redirect:/login";
    }
}
