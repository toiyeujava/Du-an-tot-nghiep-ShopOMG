package poly.edu.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import poly.edu.service.LoginAttemptService;

/**
 * AuthController - Handles login page and authentication redirects.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why is this in the common/ package?"
 *
 * Authentication endpoints (/login, /register) are neither user-specific
 * nor admin-specific - they're used by BOTH roles. The common/ package
 * holds cross-cutting controllers that don't fit neatly into user/ or admin/.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final LoginAttemptService loginAttemptService;

    @GetMapping("/login")
    public String showLoginForm(Model model,
            String email,
            String error,
            String locked) {

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
}
