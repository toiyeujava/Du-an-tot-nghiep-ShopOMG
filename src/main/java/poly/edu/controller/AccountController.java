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
import poly.edu.service.AccountService;
import poly.edu.service.EmailVerificationService;
import poly.edu.service.FileService;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    AccountService accountService;
    @Autowired
    FileService fileService;
    @Autowired
    EmailVerificationService emailVerificationService;

    // --- 1. HIỂN THỊ TRANG PROFILE (GET) ---
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        Account acc = accountService.findByEmail(username);

        // Đổ dữ liệu từ Entity sang DTO (Form) để hiển thị lên giao diện
        ProfileForm form = new ProfileForm();
        form.setUsername(acc.getUsername());
        form.setFullName(acc.getFullName());
        form.setPhone(acc.getPhone());
        form.setEmail(acc.getEmail());
        form.setAvatarUrl(acc.getAvatar());
        form.setBirthDate(acc.getBirthDate());
        form.setGender(acc.getGender());

        model.addAttribute("profileForm", form); // Đẩy form sang HTML
        model.addAttribute("activePage", "profile");

        return "user/account-profile";
    }

    // --- 2. XỬ LÝ CẬP NHẬT (POST) ---
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm form, // Hứng dữ liệu vào Form
            BindingResult binding,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Principal principal,
            RedirectAttributes ra) {

        // 1. Nếu validate lỗi -> Quay lại trang profile và báo lỗi
        if (binding.hasErrors()) {
            // Cần set lại activePage để sidebar không bị mất active
            return "user/account-profile";
        }

        // 2. Lấy tài khoản hiện tại từ DB
        Account acc = accountService.findByEmail(principal.getName());

        // 3. Cập nhật thông tin từ Form vào Entity
        acc.setFullName(form.getFullName());
        acc.setPhone(form.getPhone());
        acc.setBirthDate(form.getBirthDate());
        acc.setGender(form.getGender());

        // 4. Xử lý Avatar (nếu có upload ảnh mới)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarPath = fileService.save(avatarFile);
                acc.setAvatar(avatarPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 5. Lưu xuống DB
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