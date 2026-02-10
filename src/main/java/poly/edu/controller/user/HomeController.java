package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dto.CategoryCountDTO;
import poly.edu.entity.Product;
import poly.edu.repository.CategoryRepository;
import poly.edu.repository.ProductRepository;

import java.util.List;

/**
 * HomeController - Handles the homepage only.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why is HomeController now so small?"
 *
 * The original HomeController (318 lines) mixed 3 unrelated concerns:
 * - Homepage display → THIS CONTROLLER (single purpose)
 * - Product browsing → ShopController
 * - Checkout flow → CheckoutController
 *
 * Now it only renders the homepage with featured products and filters.
 * This is the "landing page" - the first thing users see.
 *
 * "Why does the homepage also have product filters?"
 * - UX decision: users can start browsing from the homepage
 * - Same filter sidebar as /products, but different layout/template
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Homepage with featured products and filter sidebar.
     */
    @GetMapping({ "/", "/home" })
    public String index(Model model,
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

        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedSort", sortType);
        model.addAttribute("isSale", sale);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        String pageTitle = "Trang chủ - ShopOMG";
        if (keyword != null && !keyword.isEmpty())
            pageTitle = "Tìm kiếm: " + keyword;
        else if (gender != null && !gender.isEmpty())
            pageTitle = "Thời trang " + gender;

        model.addAttribute("pageTitle", pageTitle);
        return "user/home";
    }

    private Sort buildSort(String sortType) {
        return switch (sortType) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
