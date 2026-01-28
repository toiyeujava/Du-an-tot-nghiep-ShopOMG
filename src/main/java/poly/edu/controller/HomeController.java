package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    // --- CÁC TRANG CÔNG KHAI ---

    @GetMapping({ "/", "/home" })
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

    @PostMapping("/checkout/process")
    public String processCheckout(
            @RequestParam(value = "selectedRecipientName", required = false) String recipientName,
            @RequestParam(value = "selectedPhone", required = false) String phone,
            @RequestParam(value = "selectedAddress", required = false) String address,
            Model model) {
        // TODO: Process order logic here
        // For now, just show success page with selected address data

        // Mask phone number: show first 2 and last 2 digits only
        String maskedPhone = maskPhoneNumber(phone);

        model.addAttribute("pageTitle", "Đặt hàng thành công");
        model.addAttribute("recipientName", recipientName != null ? recipientName : "Chưa chọn địa chỉ");
        model.addAttribute("phone", maskedPhone);
        model.addAttribute("fullAddress", address != null ? address : "");
        return "user/order-success";
    }

    // Helper method to mask phone number
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }

        // Remove all non-digit characters
        String digitsOnly = phone.replaceAll("\\D", "");

        if (digitsOnly.length() < 4) {
            return phone; // Too short to mask
        }

        // Get first 2 and last 2 digits
        String first2 = digitsOnly.substring(0, 2);
        String last2 = digitsOnly.substring(digitsOnly.length() - 2);

        // Calculate number of asterisks needed
        int asteriskCount = digitsOnly.length() - 4;
        String asterisks = "*".repeat(Math.max(0, asteriskCount));

        // Format as (+84)XX******XX
        return "(+84)" + first2 + asterisks + last2;
    }


    /*
     * ❌ XÓA HOẶC COMMENT ĐOẠN DƯỚI ĐÂY ĐỂ TRÁNH XUNG ĐỘT VỚI AccountController
     * (Vì AccountController đã quản lý các đường dẫn /account/... này rồi)
     */

    // @GetMapping("/account/profile")
    // public String profile(Model model) {
    // model.addAttribute("activePage", "profile");
    // return "user/account-profile";
    // }

    // @GetMapping("/account/orders")
    // public String orders(Model model) {
    // model.addAttribute("activePage", "orders");
    // return "user/account-orders";
    // }

    // @GetMapping("/account/reviews")
    // public String reviews(Model model) {
    // model.addAttribute("activePage", "reviews");
    // return "user/account-reviews";
    // }
}