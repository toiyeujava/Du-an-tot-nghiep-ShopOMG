package poly.edu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.*;
import poly.edu.service.*;

import java.util.Map;

/**
 * @deprecated This controller has been refactored into separate controllers:
 *             - AdminDashboardController
 *             - AdminProductController
 *             - AdminOrderController
 *             - AdminCategoryController
 *             - AdminAccountController
 * 
 *             Keeping this file for reference. To re-enable,
 *             uncomment @Controller below.
 */
// @Controller // DISABLED: Refactored into poly.edu.controller.admin.* package
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final AdminAccountService adminAccountService;

    // ========== DASHBOARD ==========

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tổng quan - Admin");

        // Get dashboard statistics
        Map<String, Object> stats = dashboardService.getDashboardStats();
        model.addAttribute("monthlyRevenue", stats.get("monthlyRevenue"));
        model.addAttribute("pendingOrders", stats.get("pendingOrders"));
        model.addAttribute("totalCustomers", stats.get("totalCustomers"));
        model.addAttribute("totalProducts", stats.get("totalProducts"));
        model.addAttribute("recentOrders", stats.get("recentOrders"));
        model.addAttribute("topProducts", stats.get("topProducts"));

        // Get chart data (last 7 months)
        model.addAttribute("revenueChartData", dashboardService.getRevenueChartData(7));

        return "admin/dashboard";
    }

    // ========== PRODUCT MANAGEMENT ==========

    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý sản phẩm");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage = productService.getAllProducts(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        return "admin/products";
    }

    @GetMapping("/products/create")
    public String createProductForm(Model model) {
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("pageTitle", "Chỉnh sửa sản phẩm");
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Integer id, @ModelAttribute Product product,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ========== CATEGORY MANAGEMENT ==========

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("pageTitle", "Quản lý loại");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/categories")
    @ResponseBody
    public Map<String, Object> createCategory(@RequestBody Category category) {
        try {
            Category saved = categoryService.createCategory(category);
            return Map.of("success", true, "message", "Thêm danh mục thành công!", "category", saved);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/categories/{id}")
    @ResponseBody
    public Map<String, Object> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        try {
            Category updated = categoryService.updateCategory(id, category);
            return Map.of("success", true, "message", "Cập nhật danh mục thành công!", "category", updated);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    @ResponseBody
    public Map<String, Object> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return Map.of("success", true, "message", "Xóa danh mục thành công!");
        } catch (IllegalStateException e) {
            return Map.of("success", false, "message", e.getMessage());
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    // ========== ORDER MANAGEMENT ==========

    @GetMapping("/orders")
    public String orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý đơn hàng");

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<Order> orderPage;

        if (status != null && !status.isEmpty()) {
            orderPage = orderService.getOrdersByStatus(status, pageable);
            model.addAttribute("selectedStatus", status);
        } else {
            orderPage = orderService.getAllOrders(pageable);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        // Order statistics
        model.addAttribute("orderStats", dashboardService.getOrderStatsByStatus());

        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderService.getOrderDetails(id));

        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/approve")
    public String approveOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.approveOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Duyệt đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công! Kho hàng đã được hoàn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/ship")
    public String shipOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.shipOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã chuyển sang trạng thái Đang giao!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/complete")
    public String completeOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.completeOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã hoàn thành!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // ========== USER ACCOUNT MANAGEMENT ==========

    @GetMapping("/accounts")
    public String accounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý tài khoản");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Account> accountPage = adminAccountService.getAllUsers(pageable);

        model.addAttribute("accounts", accountPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accountPage.getTotalPages());
        model.addAttribute("totalItems", accountPage.getTotalElements());

        // User statistics
        model.addAttribute("activeUsers", adminAccountService.getActiveUsersCount());
        model.addAttribute("lockedUsers", adminAccountService.getLockedUsersCount());

        return "admin/accounts";
    }

    @GetMapping("/accounts/{id}")
    public String accountDetail(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Account account = adminAccountService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        model.addAttribute("pageTitle", "Chi tiết tài khoản: " + account.getUsername());
        model.addAttribute("account", account);

        // Get user's orders
        Pageable pageable = PageRequest.of(page, 10);
        Page<Order> orders = adminAccountService.getUserOrders(id, pageable);
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        return "admin/account-detail";
    }

    @PostMapping("/accounts/{id}/lock")
    public String lockAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.lockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/{id}/unlock")
    public String unlockAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.unlockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mở khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/{id}/reset-password")
    public String resetPassword(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            String newPassword = adminAccountService.resetPassword(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reset mật khẩu thành công! Mật khẩu mới: " + newPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/{id}/delete")
    public String deleteAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
