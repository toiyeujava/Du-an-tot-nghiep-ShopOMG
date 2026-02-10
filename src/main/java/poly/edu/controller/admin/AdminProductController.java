package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Product;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;

/**
 * AdminProductController - Handles product CRUD for admin.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why separate product management into its own controller?"
 * 
 * 1. Single Responsibility Principle (SRP): Each controller handles one domain
 * 2. Easier to test: Can mock only product-related dependencies
 * 3. Easier to maintain: Changes to products don't affect orders
 * 4. Easier to scale: Can add product-specific middleware
 * 
 * Variant management has been extracted to AdminProductVariantController
 * to further adhere to SRP - each controller handles one concern.
 * 
 * Time/Space Complexity:
 * - getAllProducts(): O(n) time, O(n) space (n = page size)
 * - createProduct(): O(1) time (single insert)
 * - updateProduct(): O(1) time
 * - deleteProduct(): O(1) time (soft delete)
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // ==================== PRODUCT CRUD ====================

    /**
     * List all products with pagination.
     * 
     * Algorithm:
     * 1. Create Pageable with page, size, and sort by ID descending
     * 2. Query products using pagination
     * 3. Add pagination metadata to model
     * 
     * Why sort by ID descending?
     * - Newest products appear first (most relevant for admin)
     * - ID is indexed, so sorting is O(log n)
     */
    @GetMapping
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

    /**
     * Show create product form.
     */
    @GetMapping("/create")
    public String createProductForm(Model model) {
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    /**
     * Create new product.
     * 
     * Algorithm:
     * 1. Validate product data (in service layer)
     * 2. Set defaults (isActive=true, discount=0, viewCount=0)
     * 3. Save to database
     * 4. Redirect with success/error message
     * 
     * Why use RedirectAttributes?
     * - PRG pattern (Post-Redirect-Get) prevents form resubmission
     * - Flash attributes survive the redirect
     */
    @PostMapping
    public String createProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * Show edit product form.
     */
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("pageTitle", "Chỉnh sửa sản phẩm");
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    /**
     * Update existing product.
     * 
     * Algorithm:
     * 1. Find product by ID (throws if not found)
     * 2. Update only non-null fields
     * 3. Save changes
     * 
     * Why partial update?
     * - More flexible (don't need to send all fields)
     * - Reduces data transfer
     */
    @PostMapping("/{id}")
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

    /**
     * Delete product (soft delete).
     * 
     * Algorithm:
     * 1. Check if product has active orders
     * 2. If yes → throw exception (cannot delete)
     * 3. If no → set isActive = false (soft delete)
     * 
     * Why soft delete?
     * - Preserve order history integrity
     * - Allow recovery if deleted by mistake
     * - Maintain referential integrity
     */
    @PostMapping("/{id}/delete")
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
}
