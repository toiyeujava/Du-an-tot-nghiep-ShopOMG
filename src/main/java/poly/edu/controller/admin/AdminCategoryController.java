package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Category;
import poly.edu.service.CategoryService;

import java.util.Map;

/**
 * AdminCategoryController - Handles category management with AJAX support.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why use AJAX for categories?"
 * 
 * Categories are managed inline (modal dialog) without page reload because:
 * 1. Better UX - no page flicker
 * 2. Faster feedback - instant response
 * 3. Categories are simple entities (just name, slug, image)
 * 
 * "Why return Map<String, Object> for AJAX?"
 * 
 * JSON response format:
 * {
 * "success": true/false,
 * "message": "Human readable message",
 * "category": { ... } // only on success
 * }
 * 
 * This allows frontend to:
 * - Show success/error toast
 * - Update UI without refresh
 * - Handle errors gracefully
 * 
 * Time Complexity:
 * - createCategory(): O(1)
 * - updateCategory(): O(1)
 * - deleteCategory(): O(1) for count check + O(1) for delete
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * List all categories.
     */
    @GetMapping
    public String categories(Model model) {
        model.addAttribute("pageTitle", "Quản lý loại");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    /**
     * Create new category (AJAX).
     * 
     * Algorithm:
     * 1. Validate name is not empty
     * 2. Set default isActive = true
     * 3. Save to database
     * 4. Return JSON response with created category
     */
    @PostMapping
    @ResponseBody
    public Map<String, Object> createCategory(@RequestBody Category category) {
        try {
            Category saved = categoryService.createCategory(category);
            return Map.of(
                    "success", true,
                    "message", "Thêm danh mục thành công!",
                    "category", saved);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage());
        }
    }

    /**
     * Update existing category (AJAX).
     * 
     * Algorithm:
     * 1. Find category by ID
     * 2. Update only non-null fields
     * 3. Save changes
     */
    @PutMapping("/{id}")
    @ResponseBody
    public Map<String, Object> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        try {
            Category updated = categoryService.updateCategory(id, category);
            return Map.of(
                    "success", true,
                    "message", "Cập nhật danh mục thành công!",
                    "category", updated);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage());
        }
    }

    /**
     * Delete category (AJAX).
     * 
     * Algorithm:
     * 1. Count products in this category
     * 2. If count > 0 → return error with product count
     * 3. If count = 0 → delete category
     * 
     * Why constraint check?
     * - Prevent orphaned products
     * - Force admin to move products first
     * - Data integrity
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public Map<String, Object> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return Map.of(
                    "success", true,
                    "message", "Xóa danh mục thành công!");
        } catch (IllegalStateException e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage());
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage());
        }
    }
}
