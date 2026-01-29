package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Category;
import poly.edu.repository.CategoryRepository;
import poly.edu.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Get all categories
     * Algorithm: Simple SELECT query
     * Time Complexity: O(n) where n = number of categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get category by ID
     * Algorithm: Direct lookup by primary key
     * Time Complexity: O(1)
     */
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    /**
     * Create new category
     * Algorithm: Validation + Insert
     * Time Complexity: O(1)
     */
    @Transactional
    public Category createCategory(Category category) {
        // Validate required fields
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // Set default values
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        return categoryRepository.save(category);
    }

    /**
     * Update existing category
     * Algorithm: Find + Update
     * Time Complexity: O(1)
     */
    @Transactional
    public Category updateCategory(Integer id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Update fields
        if (categoryDetails.getName() != null) {
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getSlug() != null) {
            category.setSlug(categoryDetails.getSlug());
        }
        if (categoryDetails.getImage() != null) {
            category.setImage(categoryDetails.getImage());
        }
        if (categoryDetails.getIsActive() != null) {
            category.setIsActive(categoryDetails.getIsActive());
        }

        return categoryRepository.save(category);
    }

    /**
     * Delete category with constraint checking
     * Algorithm:
     * 1. Count products in category: SELECT COUNT(*) FROM Products WHERE
     * category_id = ?
     * 2. If count > 0 -> throw exception with product count
     * 3. If count = 0 -> delete category
     * Time Complexity: O(1) for count + O(1) for delete = O(1)
     * 
     * Data Structure: Simple counter
     */
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Count products in this category
        long productCount = productRepository.findAll().stream()
                .filter(p -> p.getCategoryId().equals(id))
                .count();

        if (productCount > 0) {
            throw new IllegalStateException(
                    String.format("Cannot delete category '%s'. It has %d product(s). " +
                            "Please move or delete these products first.",
                            category.getName(), productCount));
        }

        // Safe to delete
        categoryRepository.delete(category);
    }

    /**
     * Get product count for each category
     * Useful for admin dashboard
     */
    public long getProductCountByCategory(Integer categoryId) {
        return productRepository.findAll().stream()
                .filter(p -> p.getCategoryId().equals(categoryId))
                .count();
    }

    /**
     * Get active categories only
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .toList();
    }

    /**
     * Count total categories
     */
    public long countTotalCategories() {
        return categoryRepository.count();
    }
}
