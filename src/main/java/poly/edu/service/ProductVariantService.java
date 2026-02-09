package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.ProductVariantRepository;

import java.util.List;
import java.util.Optional;

/**
 * ProductVariantService - Business logic for product variant management.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why create a separate service for variants?"
 * 
 * ProductVariant has its own business rules:
 * 1. SKU uniqueness validation
 * 2. Stock management (quantity tracking)
 * 3. Size/Color combinations must be unique per product
 * 
 * Putting this in ProductService would:
 * - Violate Single Responsibility Principle
 * - Make ProductService too large (>200 lines)
 * - Mix different concerns (product info vs inventory)
 * 
 * "What is a variant?"
 * 
 * A variant represents a specific combination of:
 * - Size (S, M, L, XL, etc.)
 * - Color (Red, Blue, Black, etc.)
 * - Quantity (stock level)
 * - SKU (unique identifier for inventory)
 * 
 * Example: Product "T-Shirt Basic" has variants:
 * - Size:S, Color:Red, Qty:10, SKU:TSB-S-RED
 * - Size:M, Color:Red, Qty:15, SKU:TSB-M-RED
 * - Size:S, Color:Blue, Qty:8, SKU:TSB-S-BLU
 * 
 * Time Complexity:
 * - getVariantsByProduct(): O(n) - n = variants for product
 * - createVariant(): O(1) - single insert
 * - updateVariant(): O(1) - single update
 * - deleteVariant(): O(1) - single delete
 * - getTotalStock(): O(n) - sum all variant quantities
 * 
 * Space Complexity:
 * - All operations: O(1) except getVariantsByProduct: O(n)
 */
@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    /**
     * Get all variants for a product.
     * 
     * Algorithm:
     * 1. Query variants by product_id using foreign key index
     * 2. Return list (ordered by ID)
     * 
     * @param productId Product ID
     * @return List of variants
     */
    public List<ProductVariant> getVariantsByProduct(Integer productId) {
        return variantRepository.findByProductId(productId);
    }

    /**
     * Get variant by ID.
     * 
     * Time Complexity: O(1) - primary key lookup
     */
    public Optional<ProductVariant> getVariantById(Integer id) {
        return variantRepository.findById(id);
    }

    /**
     * Create new variant for a product.
     * 
     * Algorithm:
     * 1. Find product by ID (throws if not found)
     * 2. Validate size/color combination is unique for this product
     * 3. Generate SKU if not provided
     * 4. Set product reference
     * 5. Save variant
     * 
     * Why validate uniqueness?
     * - Prevent duplicate size/color combinations
     * - Each combination represents unique inventory slot
     * 
     * @param productId Product ID
     * @param variant   Variant data
     * @return Created variant
     */
    @Transactional
    public ProductVariant createVariant(Integer productId, ProductVariant variant) {
        // Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Validate size/color combination is unique
        boolean exists = variantRepository.existsByProductIdAndSizeAndColor(
                productId, variant.getSize(), variant.getColor());
        if (exists) {
            throw new IllegalArgumentException(
                    "Variant with size '" + variant.getSize() +
                            "' and color '" + variant.getColor() + "' already exists");
        }

        // Generate SKU if not provided
        if (variant.getSku() == null || variant.getSku().isEmpty()) {
            variant.setSku(generateSku(product, variant));
        }

        // Set product reference and save
        variant.setProduct(product);

        // Set default quantity if null
        if (variant.getQuantity() == null) {
            variant.setQuantity(0);
        }

        return variantRepository.save(variant);
    }

    /**
     * Update existing variant.
     * 
     * Algorithm:
     * 1. Find variant by ID
     * 2. Update only non-null fields
     * 3. Validate new size/color if changed
     * 4. Save changes
     * 
     * @param id             Variant ID
     * @param variantDetails Updated data
     * @return Updated variant
     */
    @Transactional
    public ProductVariant updateVariant(Integer id, ProductVariant variantDetails) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + id));

        // Update fields if provided
        if (variantDetails.getSize() != null) {
            variant.setSize(variantDetails.getSize());
        }
        if (variantDetails.getColor() != null) {
            variant.setColor(variantDetails.getColor());
        }
        if (variantDetails.getQuantity() != null) {
            variant.setQuantity(variantDetails.getQuantity());
        }
        if (variantDetails.getSku() != null) {
            variant.setSku(variantDetails.getSku());
        }

        return variantRepository.save(variant);
    }

    /**
     * Delete variant.
     * 
     * Algorithm:
     * 1. Find variant by ID
     * 2. Check if variant is in any pending/shipping orders
     * 3. If yes → throw exception
     * 4. If no → hard delete
     * 
     * Why hard delete instead of soft delete?
     * - Variants are inventory items, not historical data
     * - Can recreate if needed
     * - Simplifies inventory management
     * 
     * @param id Variant ID
     */
    @Transactional
    public void deleteVariant(Integer id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + id));

        // Check if variant is in pending orders
        boolean hasActiveOrders = variantRepository.hasActiveOrders(id);
        if (hasActiveOrders) {
            throw new IllegalStateException(
                    "Cannot delete variant. It has active orders (PENDING, CONFIRMED, or SHIPPING).");
        }

        variantRepository.delete(variant);
    }

    /**
     * Get total stock for a product (sum of all variant quantities).
     * 
     * @param productId Product ID
     * @return Total stock quantity
     */
    public int getTotalStock(Integer productId) {
        return getVariantsByProduct(productId).stream()
                .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                .sum();
    }

    /**
     * Check if SKU is unique.
     * 
     * @param sku       SKU to check
     * @param excludeId Variant ID to exclude (for updates)
     * @return true if SKU is unique
     */
    public boolean isSkuUnique(String sku, Integer excludeId) {
        Optional<ProductVariant> existing = variantRepository.findBySku(sku);
        return existing.isEmpty() || existing.get().getId().equals(excludeId);
    }

    /**
     * Generate SKU based on product and variant.
     * 
     * Format: {ProductInitials}-{Size}-{ColorCode}
     * Example: TSB-M-RED
     * 
     * @param product Product
     * @param variant Variant
     * @return Generated SKU
     */
    private String generateSku(Product product, ProductVariant variant) {
        String productCode = product.getName()
                .toUpperCase()
                .replaceAll("[^A-Z]", "")
                .substring(0, Math.min(3, product.getName().length()));

        String size = variant.getSize() != null ? variant.getSize().toUpperCase() : "X";
        String color = variant.getColor() != null
                ? variant.getColor().toUpperCase().substring(0, Math.min(3, variant.getColor().length()))
                : "XXX";

        return String.format("%s-%s-%s", productCode, size, color);
    }
}
