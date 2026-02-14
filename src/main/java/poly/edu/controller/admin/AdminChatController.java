package poly.edu.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin") 
public class AdminChatController {

    public AdminChatController() {
    }

    // MỞ TRANG CHAT
    @GetMapping("/chat")
    public String openAdminChat(Model model) {
        model.addAttribute("pageTitle", "Hỗ trợ khách hàng");
        return "admin/admin_chat"; // Trả về file view (thường là .html hoặc .jsp)
    }

}