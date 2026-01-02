package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Chỉ giữ lại cái này để hiển thị form đăng nhập
    @GetMapping("/login")
    public String loginForm() {
        return "user/login"; 
    }
    
    @GetMapping("/register")
    public String redirectRegister() {
        return "redirect:/account/sign-up";
    }
    
    // XÓA HẾT CÁC PHẦN ĐĂNG KÝ (REGISTER) Ở ĐÂY ĐỂ TRÁNH XUNG ĐỘT
}