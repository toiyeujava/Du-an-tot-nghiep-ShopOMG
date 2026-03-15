package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Account;
import poly.edu.entity.LookbookPost;
import poly.edu.repository.AccountRepository;
import poly.edu.service.LookbookService;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LookbookController {

    private final LookbookService lookbookService;
    private final AccountRepository accountRepository;

    /**
     * Public feed — shows all approved Lookbook posts (masonry grid).
     */
    @GetMapping("/lookbook")
    public String lookbookFeed(Model model) {
        List<LookbookPost> posts = lookbookService.getApprovedPosts();
        model.addAttribute("posts", posts);
        return "lookbook/index";
    }

    /**
     * User creates a new Lookbook post (from order page modal).
     */
    @PostMapping("/account/lookbook/create")
    public String createPost(@RequestParam("orderId") Integer orderId,
                             @RequestParam("productId") Integer productId,
                             @RequestParam(value = "caption", required = false) String caption,
                             @RequestParam("image") MultipartFile image,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            Account account = resolveAccount(principal);
            if (account == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập lại");
                return "redirect:/account/orders";
            }

            lookbookService.createPost(account, orderId, productId, caption, image);
            redirectAttributes.addFlashAttribute("success", "Đã đăng bài lên Lookbook! Bài đăng sẽ hiển thị sau khi được duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account/orders";
    }

    /**
     * User's own Lookbook posts page.
     */
    @GetMapping("/account/lookbook")
    public String myPosts(Model model, Principal principal) {
        Account account = resolveAccount(principal);
        if (account == null) {
            return "redirect:/login";
        }
        List<LookbookPost> posts = lookbookService.getPostsByAccount(account.getId());
        model.addAttribute("posts", posts);
        return "lookbook/my-posts";
    }

    private Account resolveAccount(Principal principal) {
        if (principal == null) return null;
        try {
            String identifier;
            if (principal instanceof OAuth2AuthenticationToken token) {
                identifier = token.getPrincipal().getAttribute("email");
                if (identifier == null) identifier = token.getPrincipal().getName();
            } else {
                identifier = principal.getName();
            }
            Account acc = accountRepository.findByEmail(identifier).orElse(null);
            if (acc == null) {
                acc = accountRepository.findByUsername(identifier).orElse(null);
            }
            return acc;
        } catch (Exception e) {
            return null;
        }
    }
}
