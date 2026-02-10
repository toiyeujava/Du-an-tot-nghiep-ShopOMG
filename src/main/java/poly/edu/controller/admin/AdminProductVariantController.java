package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.service.ProductService;
import poly.edu.service.ProductVariantService;

/**
 * AdminProductVariantController - Manages product variant CRUD for admin.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why separate variant management from AdminProductController?"
 *
 * The original AdminProductController handled both products AND variants (266
 * lines),
 * violating SRP. Variants have their own lifecycle:
 * - Different validation rules (SKU uniqueness, color+size combination)
 * - Different delete logic (check active orders vs. soft delete)
 * - Different form/template (product-variants.html vs. product-form.html)
 *
 * "Why keep the URL nested under /admin/products/{id}/variants?"
 * - REST best practice: variants are sub-resources of products
 * - Admin flow: navigate to product → manage its variants
 * - URL clearly shows the parent-child relationship
 *
 * Time Complexity:
 * - listVariants(): O(n) where n = number of variants for the product
 * - addVariant(): O(1) single insert + O(1) duplicate check
 * - updateVariant(): O(1) single update
 * - deleteVariant(): O(m) where m = pending orders to check
 */
@Controller
@RequestMapping("/admin/products/{productId}/variants")
@RequiredArgsConstructor
public class AdminProductVariantController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;

    /**
     * List all variants for a specific product.
     */
    @GetMapping
    public String listVariants(@PathVariable Integer productId, Model model) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        model.addAttribute("pageTitle", "Quản lý biến thể - " + product.getName());
        model.addAttribute("product", product);
        model.addAttribute("variants", productVariantService.getVariantsByProduct(productId));
        model.addAttribute("newVariant", new ProductVariant());

        return "admin/product-variants";
    }

    /**
     * Add a new variant to the product.
     *
     * Algorithm:
     * 1. Validate color+size combination uniqueness (in service)
     * 2. Generate SKU if not provided (in service)
     * 3. Save variant linked to parent product
     */
    @PostMapping
    public String addVariant(@PathVariable Integer productId,
            @ModelAttribute ProductVariant variant,
            RedirectAttributes redirectAttributes) {
        try {
            productVariantService.createVariant(productId, variant);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    /**
     * Update an existing variant.
     */
    @PostMapping("/{variantId}")
    public String updateVariant(@PathVariable Integer productId,
            @PathVariable Integer variantId,
            @ModelAttribute ProductVariant variant,
            RedirectAttributes redirectAttributes) {
        try {
            productVariantService.updateVariant(variantId, variant);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    /**
     * Delete a variant.
     *
     * Algorithm:
     * 1. Check if variant is referenced in pending orders
     * 2. If yes → throw exception
     * 3. If no → hard delete (variants can be recreated)
     */
    @PostMapping("/{variantId}/delete")
    public String deleteVariant(@PathVariable Integer productId,
            @PathVariable Integer variantId,
            RedirectAttributes redirectAttributes) {
        try {
            productVariantService.deleteVariant(variantId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa biến thể thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }
}
