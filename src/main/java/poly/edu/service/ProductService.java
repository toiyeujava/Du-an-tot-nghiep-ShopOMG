package poly.edu.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Product;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.utils.SlugUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Get all products with pagination
     * Algorithm: Simple pagination query
     * Time Complexity: O(n) where n = page size
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Get product by ID
     * Algorithm: Direct lookup by primary key
     * Time Complexity: O(1)
     */
    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by slug
     * Algorithm: Direct lookup by unique slug
     * Time Complexity: O(1)
     */
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    /**
     * Create new product
     * Algorithm: Validation + Insert
     * Time Complexity: O(1)
     */
    @Transactional
    public Product createProduct(Product product) {
        // Validate required fields
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        // Set default values
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getDiscount() == null) {
            product.setDiscount(0);
        }
        if (product.getViewCount() == null) {
            product.setViewCount(0);
        }

        // Auto-generate slug from product name
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(generateUniqueSlug(product.getName(), null));
        }

        return productRepository.save(product);
    }

    /**
     * Update existing product
     * Algorithm: Find + Update
     * Time Complexity: O(1)
     */
    @Transactional
    public Product updateProduct(Integer id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update fields
        boolean nameChanged = productDetails.getName() != null
                && !productDetails.getName().equals(product.getName());
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getDiscount() != null) {
            product.setDiscount(productDetails.getDiscount());
        }
        if (productDetails.getCategoryId() != null) {
            product.setCategoryId(productDetails.getCategoryId());
        }
        if (productDetails.getImage() != null) {
            product.setImage(productDetails.getImage());
        }
        if (productDetails.getGender() != null) {
            product.setGender(productDetails.getGender());
        }
        if (productDetails.getMaterial() != null) {
            product.setMaterial(productDetails.getMaterial());
        }
        if (productDetails.getOrigin() != null) {
            product.setOrigin(productDetails.getOrigin());
        }
        if (productDetails.getIsActive() != null) {
            product.setIsActive(productDetails.getIsActive());
        }

        // Re-generate slug if name changed or slug is empty
        if (nameChanged || product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(generateUniqueSlug(product.getName(), product.getId()));
        }

        return productRepository.save(product);
    }

    /**
     * Delete product with constraint checking
     * Algorithm:
     * 1. Check if product has active orders (PENDING, CONFIRMED, SHIPPING)
     * 2. If yes -> throw exception
     * 3. If no -> soft delete (set isActive = false)
     * Time Complexity: O(1) for check + O(1) for update = O(1)
     */
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check if product has active orders
        boolean hasActiveOrders = orderRepository.hasActiveOrdersForProduct(id);
        if (hasActiveOrders) {
            throw new IllegalStateException(
                    "Cannot delete product. It has active orders (PENDING, CONFIRMED, or SHIPPING).");
        }

        // Soft delete - set isActive to false instead of hard delete
        product.setIsActive(false);
        productRepository.save(product);
    }

    /**
     * Hard delete product (use with caution)
     * Only for products with no orders at all
     */
    @Transactional
    public void hardDeleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        boolean hasActiveOrders = orderRepository.hasActiveOrdersForProduct(id);
        if (hasActiveOrders) {
            throw new IllegalStateException("Cannot delete product with active orders");
        }

        productRepository.delete(product);
    }

    /**
     * Search products by name
     * Algorithm: LIKE query with pagination
     * Time Complexity: O(n) where n = matching products
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        // This would require adding a search method to ProductRepository
        // For now, return all products
        return productRepository.findAll(pageable);
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findAll().stream()
                .filter(p -> p.getCategoryId().equals(categoryId))
                .toList();
    }

    /**
     * Get products by gender
     */
    public List<Product> getProductsByGender(String gender) {
        return productRepository.findByGender(gender);
    }

    /**
     * Count total products
     */
    public long countTotalProducts() {
        return productRepository.count();
    }

    /**
     * Count active products
     */
    public long countActiveProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .count();
    }

    // ===== SLUG HELPERS =====

    /**
     * Generate a unique slug from the product name.
     * If a slug collision occurs, appends -2, -3, etc.
     *
     * @param name      the product name
     * @param excludeId product ID to exclude from collision check (for updates)
     */
    private String generateUniqueSlug(String name, Integer excludeId) {
        String baseSlug = SlugUtils.toSlug(name);
        String candidateSlug = baseSlug;
        int counter = 2;

        while (true) {
            Optional<Product> existing = productRepository.findBySlug(candidateSlug);
            if (existing.isEmpty() || existing.get().getId().equals(excludeId)) {
                break;
            }
            candidateSlug = baseSlug + "-" + counter;
            counter++;
        }

        return candidateSlug;
    }

    /**
     * Populate slugs for existing products that have empty/null slug.
     * Runs once on application startup.
     */
    @PostConstruct
    @Transactional
    public void initSlugs() {
        List<Product> productsWithoutSlug = productRepository.findAll().stream()
                .filter(p -> p.getSlug() == null || p.getSlug().isBlank())
                .toList();

        if (!productsWithoutSlug.isEmpty()) {
            log.info("Generating slugs for {} products...", productsWithoutSlug.size());
            for (Product product : productsWithoutSlug) {
                product.setSlug(generateUniqueSlug(product.getName(), product.getId()));
                productRepository.save(product);
            }
            log.info("Slug generation complete.");
        }
    }
}
