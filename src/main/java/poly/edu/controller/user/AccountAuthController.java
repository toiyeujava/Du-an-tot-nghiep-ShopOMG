package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.dto.SignUpForm;
import poly.edu.entity.Account;
import poly.edu.service.AccountService;
import poly.edu.service.EmailVerificationService;

import jakarta.validation.Valid;

/**
 * AccountAuthController - Handles user registration (sign-up).
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why separate registration from the profile controller?"
 *
 * Registration is a public, unauthenticated operation with:
 * - Different dependencies (EmailVerificationService, no FileService)
 * - Different validation (username/email uniqueness, password confirmation)
 * - Different UX flow (public form → email verification → login)
 *
 * Profile management is an authenticated operation with:
 * - Different dependencies (FileService, OrderService)
 * - Different validation (profile updates, avatar upload)
 * - Different UX flow (/account/profile → edit → save)
 *
 * Keeping them separate makes each controller easier to test and maintain.
 */
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountAuthController {

    private final AccountService accountService;
    private final EmailVerificationService emailVerificationService;

    /**
     * Display sign-up form (GET).
     */
    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignUpForm());
        }
        return "user/register";
    }

    /**
     * Process registration (POST).
     *
     * Algorithm:
     * 1. Validate password confirmation match
     * 2. Check email uniqueness
     * 3. Check username uniqueness
     * 4. If errors → re-display form with validation messages
     * 5. If valid → create account (emailVerified=false) + send verification email
     *
     * "Why set emailVerified=false by default?"
     * - Prevents spam/bot registrations from accessing the system
     * - Email verification confirms the user owns the email address
     * - Required for password reset functionality to work securely
     */
    @PostMapping("/sign-up")
    public String doSignUp(@Valid @ModelAttribute("form") SignUpForm form,
            BindingResult binding,
            RedirectAttributes ra) {

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "error.form", "Mật khẩu nhập lại không khớp!");
        }

        if (accountService.emailExists(form.getEmail())) {
            binding.rejectValue("email", "error.form", "Email này đã được sử dụng!");
        }

        if (accountService.usernameExists(form.getUsername())) {
            binding.rejectValue("username", "error.form", "Tên đăng nhập đã tồn tại!");
        }

        if (binding.hasErrors()) {
            return "user/register";
        }

        Account acc = new Account();
        acc.setUsername(form.getUsername());
        acc.setFullName(form.getFullName());
        acc.setEmail(form.getEmail());
        acc.setPassword(form.getPassword());
        acc.setPhone(form.getPhone());

        Account savedAccount = accountService.register(acc);
        emailVerificationService.createVerificationToken(savedAccount);

        return "redirect:/verify-email-sent";
    }
}
