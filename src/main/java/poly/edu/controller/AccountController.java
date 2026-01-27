package poly.edu.controller;

import java.io.IOException;
import java.security.Principal;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.edu.dto.ProfileForm; // Import mới
import poly.edu.dto.SignUpForm;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;
import poly.edu.service.AccountService;
import poly.edu.service.EmailVerificationService;
import poly.edu.service.FileService;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository; // Thêm dòng này để sửa lỗi "cannot be resolved"

    @Autowired
    FileService fileService;

    @Autowired
    EmailVerificationService emailVerificationService;

    // HÀM BỔ TRỢ: Tìm account dù là đăng nhập bằng Form hay mạng xã hội
    private Account getAuthenticatedAccount(Principal principal) {
        if (principal == null)
            return null;

        String identifier = "";
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
            // Lấy email từ Facebook/Google, nếu không có thì lấy ID (Name)
            identifier = token.getPrincipal().getAttribute("email");
            if (identifier == null)
                identifier = token.getPrincipal().getName();
        } else {
            identifier = principal.getName(); // Đăng nhập Form
        }

        // Tìm theo Email trước, nếu không thấy tìm theo Username (nơi lưu ID)
        Account acc = accountService.findByEmail(identifier);
        if (acc == null) {
            acc = accountRepository.findByUsername(identifier).orElse(null);
        }
        return acc;
    }

    // --- 1. HIỂN THỊ TRANG PROFILE (GET) ---
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        Account acc = getAuthenticatedAccount(principal); // Dùng hàm bổ trợ

        if (acc == null)
            return "redirect:/login?error=true";

        ProfileForm form = new ProfileForm();
        form.setUsername(acc.getUsername());
        form.setFullName(acc.getFullName());
        form.setPhone(acc.getPhone());
        form.setEmail(acc.getEmail());
        form.setAvatarUrl(acc.getAvatar());
        form.setBirthDate(acc.getBirthDate());
        form.setGender(acc.getGender());

        model.addAttribute("profileForm", form);
        model.addAttribute("activePage", "profile");
        return "user/account-profile";
    }

    // --- 2. XỬ LÝ CẬP NHẬT (POST) ---
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm form,
            BindingResult binding,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Principal principal,
            RedirectAttributes ra) {

        if (binding.hasErrors())
            return "user/account-profile";

        // SỬA TẠI ĐÂY: Dùng hàm bổ trợ thay vì principal.getName()
        Account acc = getAuthenticatedAccount(principal);

        if (acc == null)
            return "redirect:/login?error=true";

        acc.setFullName(form.getFullName());
        acc.setPhone(form.getPhone());
        acc.setBirthDate(form.getBirthDate());
        acc.setGender(form.getGender());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarPath = fileService.save(avatarFile);
                acc.setAvatar(avatarPath);
            } catch (IOException e) { e.printStackTrace(); }
        }

        accountService.save(acc);
        ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        return "redirect:/account/profile";
    }

    // --- 2. CÁC TRANG KHÁC CỦA TÀI KHOẢN ---

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("activePage", "orders");
        return "user/account-orders";
    }

    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("activePage", "reviews");
        return "user/account-reviews";
    }

    @GetMapping("/addresses")
    public String addresses(Model model) {
        model.addAttribute("activePage", "addresses");
        return "user/account-addresses";
    }

    // --- 3. ĐĂNG KÝ TÀI KHOẢN ---

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignUpForm());
        }
        return "user/register";
    }

    @PostMapping("/sign-up")
    public String doSignUp(@Valid @ModelAttribute("form") SignUpForm form,
            BindingResult binding,
            RedirectAttributes ra) {

        // 1. Kiểm tra mật khẩu nhập lại
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "error.form", "Mật khẩu nhập lại không khớp!");
        }

        // 2. Kiểm tra trùng Email
        if (accountService.emailExists(form.getEmail())) {
            binding.rejectValue("email", "error.form", "Email này đã được sử dụng!");
        }

        // 3. Kiểm tra trùng Username (nếu cần)
        if (accountService.usernameExists(form.getUsername())) {
            binding.rejectValue("username", "error.form", "Tên đăng nhập đã tồn tại!");
        }

        // 4. Nếu có lỗi -> Quay lại form
        if (binding.hasErrors()) {
            return "user/register";
        }

        // 5. Nếu ổn -> Lưu vào DB
        Account acc = new Account();
        acc.setUsername(form.getUsername());
        acc.setFullName(form.getFullName());
        acc.setEmail(form.getEmail());
        acc.setPassword(form.getPassword());
        acc.setPhone(form.getPhone());

        // Đăng ký tài khoản (emailVerified = false)
        Account savedAccount = accountService.register(acc);

        // Tạo token và gửi email xác thực
        emailVerificationService.createVerificationToken(savedAccount);

        // Redirect về trang thông báo kiểm tra email
        return "redirect:/verify-email-sent";
    }
}