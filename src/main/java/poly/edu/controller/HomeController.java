package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import poly.edu.dto.CategoryCountDTO;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.CategoryRepository;
import poly.edu.repository.ProductRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // --- TRANG CHỦ ---
    @GetMapping({ "/", "/home" })
    public String index(Model model, 
                        @RequestParam(name = "page", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 8, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findAll(pageable);
        model.addAttribute("products", productPage);
        model.addAttribute("pageTitle", "Trang chủ - ShopOMG");
     // THÊM DÒNG NÀY: Truyền tên user xuống View an toàn
        return "user/home";
    }

    // --- CỬA HÀNG ---
    @GetMapping("/products")
    public String shop(Model model, 
                       @RequestParam(name = "gender", required = false) String gender,
                       @RequestParam(name = "category", required = false) Integer categoryId,
                       @RequestParam(name = "color", required = false) String color,
                       @RequestParam(name = "sale", required = false) Boolean sale,
                       @RequestParam(name = "sort", defaultValue = "newest") String sortType,
                       @RequestParam(name = "page", defaultValue = "0") int page) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price_asc".equals(sortType)) sort = Sort.by(Sort.Direction.ASC, "price");
        else if ("price_desc".equals(sortType)) sort = Sort.by(Sort.Direction.DESC, "price");
        else if ("name_asc".equals(sortType)) sort = Sort.by(Sort.Direction.ASC, "name");

        Pageable pageable = PageRequest.of(page, 12, sort);
        Page<Product> productPage = productRepository.filterProducts(gender, categoryId, color, sale, pageable);
        List<CategoryCountDTO> categories = categoryRepository.getCategoryCounts(gender, sale, color);

        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedSort", sortType);
        model.addAttribute("isSale", sale);
        
        String pageTitle = "Tất cả sản phẩm";
        if (Boolean.TRUE.equals(sale)) pageTitle = "Săn Sale Giá Sốc";
        else if (gender != null && !gender.isEmpty()) pageTitle = "Thời trang " + gender;
        
        model.addAttribute("pageTitle", pageTitle);
        return "user/product-list";
    }

    // --- CHI TIẾT SẢN PHẨM (CODE KHÔNG DÙNG JACKSON) ---
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Integer id, Model model) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", product.getName());

            // 1. Lấy danh sách biến thể
            List<ProductVariant> variants = product.getVariants();
            if (variants == null) variants = new ArrayList<>();

            // 2. Lọc màu và size để vẽ nút (Java làm việc này rất tốt)
            List<String> uniqueColors = variants.stream()
                    .map(ProductVariant::getColor).filter(c -> c != null && !c.isEmpty())
                    .distinct().collect(Collectors.toList());

            List<String> uniqueSizes = variants.stream()
                    .map(ProductVariant::getSize).filter(s -> s != null && !s.isEmpty())
                    .distinct().collect(Collectors.toList());

            // 3. TẠO CHUỖI JSON BẰNG TAY (An toàn tuyệt đối, không sợ lỗi thư viện)
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < variants.size(); i++) {
                ProductVariant v = variants.get(i);
                json.append("{");
                json.append("\"color\":\"").append(v.getColor()).append("\",");
                json.append("\"size\":\"").append(v.getSize()).append("\",");
                json.append("\"quantity\":").append(v.getQuantity());
                json.append("}");
                if (i < variants.size() - 1) json.append(",");
            }
            json.append("]");

            model.addAttribute("uniqueColors", uniqueColors);
            model.addAttribute("uniqueSizes", uniqueSizes);
            
            // QUAN TRỌNG: Gửi chuỗi này xuống View
            model.addAttribute("jsonVariants", json.toString());

            return "user/product-detail";
        } else {
            return "redirect:/home";
        }
    }

    // --- THANH TOÁN ---
    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("pageTitle", "Thanh toán");
        return "user/checkout";
    }

    @PostMapping("/checkout/process")
    public String processCheckout(
            @RequestParam(value = "selectedRecipientName", required = false) String recipientName,
            @RequestParam(value = "selectedPhone", required = false) String phone,
            @RequestParam(value = "selectedAddress", required = false) String address,
            Model model) {
        
        String maskedPhone = maskPhoneNumber(phone);
        model.addAttribute("pageTitle", "Đặt hàng thành công");
        model.addAttribute("recipientName", recipientName != null ? recipientName : "Chưa chọn địa chỉ");
        model.addAttribute("phone", maskedPhone);
        model.addAttribute("fullAddress", address != null ? address : "");
        return "user/order-success";
    }

    // --- UTILS ---
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() < 4) return phone; 
        String first2 = digitsOnly.substring(0, 2);
        String last2 = digitsOnly.substring(digitsOnly.length() - 2);
        int asteriskCount = digitsOnly.length() - 4;
        String asterisks = "*".repeat(Math.max(0, asteriskCount));
        return "(+84)" + first2 + asterisks + last2;
    }
}