package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    // --- CÁC TRANG CÔNG KHAI ---

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("pageTitle", "Trang chủ - ShopOMG");
        return "user/home";
    }

    @GetMapping("/products")
    public String shop(Model model) {
        model.addAttribute("pageTitle", "Cửa hàng - ShopOMG");
        return "user/product-list";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết sản phẩm");
        return "user/product-detail";
    }

    // --- GIỎ HÀNG & THANH TOÁN ---

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("pageTitle", "Giỏ hàng");
        return "user/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("pageTitle", "Thanh toán");
        return "user/checkout";
    }

    /* ❌ XÓA HOẶC COMMENT ĐOẠN DƯỚI ĐÂY ĐỂ TRÁNH XUNG ĐỘT VỚI AccountController
       (Vì AccountController đã quản lý các đường dẫn /account/... này rồi)
    */
    
    // @GetMapping("/account/profile")
    // public String profile(Model model) {
    //     model.addAttribute("activePage", "profile");
    //     return "user/account-profile";
    // }

    // @GetMapping("/account/orders")
    // public String orders(Model model) {
    //     model.addAttribute("activePage", "orders");
    //     return "user/account-orders";
    // }

    // @GetMapping("/account/reviews")
    // public String reviews(Model model) {
    //     model.addAttribute("activePage", "reviews");
    //     return "user/account-reviews";
    // }
}