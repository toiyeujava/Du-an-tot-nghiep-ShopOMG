package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.LookbookPost;
import poly.edu.service.LookbookService;

import java.util.List;

@Controller
@RequestMapping("/admin/lookbook")
@RequiredArgsConstructor
public class AdminLookbookController {

    private final LookbookService lookbookService;

    /**
     * Admin — list all Lookbook posts for moderation.
     */
    @GetMapping
    public String index(Model model) {
        List<LookbookPost> posts = lookbookService.getAllPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("pageTitle", "Quản lý Lookbook");
        return "admin/lookbook/index";
    }

    /**
     * Admin — approve a post.
     */
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            lookbookService.approve(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt bài đăng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lookbook";
    }

    /**
     * Admin — reject a post.
     */
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            lookbookService.reject(id);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối bài đăng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lookbook";
    }

    /**
     * Admin — delete a post.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            lookbookService.deletePost(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bài đăng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lookbook";
    }
}
