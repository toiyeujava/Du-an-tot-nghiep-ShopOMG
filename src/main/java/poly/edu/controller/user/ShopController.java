package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dto.CategoryCountDTO;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.CategoryRepository;
import poly.edu.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShopController - Handles product browsing and product detail pages.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why split from HomeController?"
 *
 * HomeController was 318 lines, mixing 3 unrelated concerns:
 * 1. Homepage (marketing/landing page)
 * 2. Product browsing (shop, detail) ← THIS CONTROLLER
 * 3. Checkout (order creation)
 *
 * Product browsing is a read-only, catalog-focused concern that depends
 * only on ProductRepository and CategoryRepository. It doesn't need
 * CartService, OrderService, or AccountService.
 *
 * "Why does shop() duplicate some logic from index()?"
 * - They share the same filter/sort pattern but render different templates
 * - index() shows "Trang chủ" with featured products
 * - shop() shows "Tất cả sản phẩm" with full catalog
 * - Both need the same filter sidebar, but the page title differs
 */
@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    /**
     * Product listing page with filters and pagination.
     *
     * Algorithm:
     * 1. Parse sort parameter → Spring Sort object
     * 2. Build Pageable (page 12 items, sorted)
     * 3. Query with dynamic filters (keyword, gender, category, color, sale, price
     * range)
     * 4. Load category counts for sidebar
     */
    @GetMapping("/products")
    public String shop(Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "category", required = false) Integer categoryId,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "sale", required = false) Boolean sale,
            @RequestParam(name = "min", required = false) Double minPrice,
            @RequestParam(name = "max", required = false) Double maxPrice,
            @RequestParam(name = "sort", defaultValue = "newest") String sortType,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        Sort sort = buildSort(sortType);
        Pageable pageable = PageRequest.of(page, 12, sort);

        Page<Product> productPage = productRepository.filterProducts(keyword, gender, categoryId, color, sale,
                minPrice, maxPrice, pageable);
        List<CategoryCountDTO> categories = categoryRepository.getCategoryCounts(gender, sale, color);

        addFilterAttributes(model, keyword, gender, categoryId, color, sortType, sale, minPrice, maxPrice);
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);

        String pageTitle = "Tất cả sản phẩm";
        if (Boolean.TRUE.equals(sale))
            pageTitle = "Săn Sale Giá Sốc";
        else if (gender != null && !gender.isEmpty())
            pageTitle = "Thời trang " + gender;

        model.addAttribute("pageTitle", pageTitle);
        return "user/product-list";
    }

    /**
     * Product detail page.
     *
     * Algorithm:
     * 1. Load product by ID
     * 2. Extract unique colors/sizes from variants for UI buttons
     * 3. Build JSON string of variants manually (no Jackson dependency)
     *
     * "Why build JSON manually instead of using Jackson?"
     * - Avoids circular reference issues with JPA entities
     * - Only 4 fields needed (id, color, size, quantity)
     * - Simpler than configuring @JsonIgnore across all entities
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Integer id, Model model) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", product.getName());

            List<ProductVariant> variants = product.getVariants();
            if (variants == null)
                variants = new ArrayList<>();

            List<String> uniqueColors = variants.stream()
                    .map(ProductVariant::getColor).filter(c -> c != null && !c.isEmpty())
                    .distinct().collect(Collectors.toList());

            List<String> uniqueSizes = variants.stream()
                    .map(ProductVariant::getSize).filter(s -> s != null && !s.isEmpty())
                    .distinct().collect(Collectors.toList());

            // Build JSON manually - safe, no library issues
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < variants.size(); i++) {
                ProductVariant v = variants.get(i);
                json.append("{");
                json.append("\"id\":").append(v.getId()).append(",");
                json.append("\"color\":\"").append(v.getColor()).append("\",");
                json.append("\"size\":\"").append(v.getSize()).append("\",");
                json.append("\"quantity\":").append(v.getQuantity());
                json.append("}");
                if (i < variants.size() - 1)
                    json.append(",");
            }
            json.append("]");

            model.addAttribute("uniqueColors", uniqueColors);
            model.addAttribute("uniqueSizes", uniqueSizes);
            model.addAttribute("jsonVariants", json.toString());
            model.addAttribute("geminiApiKey", geminiApiKey);

            return "user/product-detail";
        } else {
            return "redirect:/home";
        }
    }

    // ===== PRIVATE HELPERS =====

    private Sort buildSort(String sortType) {
        return switch (sortType) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private void addFilterAttributes(Model model, String keyword, String gender,
            Integer categoryId, String color, String sortType,
            Boolean sale, Double minPrice, Double maxPrice) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedSort", sortType);
        model.addAttribute("isSale", sale);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
    }
}
