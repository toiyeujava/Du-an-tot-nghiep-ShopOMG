package poly.edu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // 1. Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tổng quan - Admin");
        return "admin/dashboard";
    }

    // 2. Quản lý Sản phẩm
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        return "admin/products";
    }

    // Form thêm/sửa sản phẩm
    @GetMapping("/products/create")
    public String createProduct(Model model) {
        return "admin/product-form";
    }

    // 3. Quản lý Đơn hàng
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        return "admin/orders";
    }

    // 4. Quản lý Loại
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("pageTitle", "Quản lý loại");
        return "admin/categories";
    }
    
 // 5. Quản lý Loại
    @GetMapping("/catalog")
    public String catalog(Model model) {
        model.addAttribute("pageTitle", "Quản lý danh mục");
        return "admin/catalog";
    }


    // 5. Quản lý Tài khoản
    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("pageTitle", "Quản lý tài khoản");
        return "admin/accounts";
    }
}