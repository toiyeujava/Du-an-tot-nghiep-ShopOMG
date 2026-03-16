package poly.edu.controller.warehouse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.ProductVariantRepository;
import poly.edu.repository.CategoryRepository;
import poly.edu.repository.InventoryLogRepository;
import poly.edu.entity.Category;
import poly.edu.entity.InventoryLog;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WarehouseController
 * - Handles all /warehouse/** routes
 * - Accessible only to WAREHOUSE and ADMIN roles
 */
@Controller
@RequestMapping("/warehouse")
@PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
@RequiredArgsConstructor
public class WarehouseController {

    private final ProductVariantRepository productVariantRepository;
    private final AccountRepository accountRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final poly.edu.repository.OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    // ─── Low-stock threshold (configurable) ──────────────────────
    private static final int LOW_STOCK_THRESHOLD = 10;

    // ─── Helper: inject currentAccount into model ─────────────────
    private void injectCurrentAccount(UserDetails userDetails, Model model) {
        if (userDetails != null) {
            accountRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(acc -> model.addAttribute("currentAccount", acc));
        }
    }

    /**
     * GET /warehouse/dashboard
     * Redirects straight to the main inventory page as the "dashboard".
     */
    @GetMapping({"/dashboard", "/"})
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        return "redirect:/warehouse/inventory";
    }

    /**
     * GET /warehouse/inventory
     * Main inventory management page — shows all variants with stock stats.
     */
    @GetMapping("/inventory")
    public String inventory(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        injectCurrentAccount(userDetails, model);

        List<ProductVariant> allVariants = productVariantRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        Map<Integer, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        long totalSku        = allVariants.size();
        long lowStockCount   = allVariants.stream()
                .filter(v -> v.getQuantity() != null && v.getQuantity() > 0 && v.getQuantity() <= LOW_STOCK_THRESHOLD)
                .count();
        long outOfStockCount = allVariants.stream()
                .filter(v -> v.getQuantity() == null || v.getQuantity() == 0)
                .count();

        // ── Dashboard chart data: Daily Products Sold ───────────────────────
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        List<Object[]> dailySoldData = orderDetailRepository.getDailyUnitsSold(startOfMonth, endOfMonth);
        
        java.util.Map<Integer, Long> unitsByDay = new java.util.HashMap<>();
        for (Object[] row : dailySoldData) {
            java.util.Date date = (java.util.Date) row[0];
            java.time.LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
            unitsByDay.put(localDate.getDayOfMonth(), ((Number) row[1]).longValue());
        }

        List<String> dailySoldLabels = new ArrayList<>();
        List<Long> dailySoldValues = new ArrayList<>();
        
        int maxDays = java.time.YearMonth.now().lengthOfMonth();
        int currentMonth = LocalDate.now().getMonthValue();
        
        for (int i = 1; i <= maxDays; i++) {
            dailySoldLabels.add(i + "/" + currentMonth);
            dailySoldValues.add(unitsByDay.getOrDefault(i, 0L));
        }

        // ── Keep existing Top products sold TODAY for mini-cards ────────────────
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().plusDays(1).atStartOfDay();

        List<Object[]> topRows = orderDetailRepository.getTopProductsSoldToday(startOfDay, endOfDay);
        Long unitsSoldToday    = orderDetailRepository.countUnitsSoldToday(startOfDay, endOfDay);

        List<String> chartLabels = new ArrayList<>();
        List<Long>   chartValues = new ArrayList<>();
        int limit = Math.min(topRows.size(), 10);
        for (int i = 0; i < limit; i++) {
            Object[] row = topRows.get(i);
            chartLabels.add(row[0] != null ? row[0].toString() : "N/A");
            chartValues.add(row[1] != null ? ((Number) row[1]).longValue() : 0L);
        }

        // ── New: Completed products sold TODAY list ────────────────
        List<Object[]> completedProductsToday = orderDetailRepository.getCompletedProductsSoldToday(startOfDay, endOfDay);
        model.addAttribute("completedProductsToday", completedProductsToday);

        List<InventoryLog> recentLogs = inventoryLogRepository.findAllByOrderByTimestampDesc();
        long todayLogsCount = inventoryLogRepository.countByTimestampBetween(startOfDay, endOfDay);

        model.addAttribute("pageTitle",       "Quản lý Tồn kho - Kho hàng");
        model.addAttribute("totalSku",        totalSku);
        model.addAttribute("lowStockCount",   lowStockCount);
        model.addAttribute("outOfStockCount",  outOfStockCount);
        model.addAttribute("todayLogsCount",  todayLogsCount);
        model.addAttribute("recentLogs",      recentLogs);
        model.addAttribute("unitsSoldToday",  unitsSoldToday != null ? unitsSoldToday : 0L);
        model.addAttribute("chartLabels",     chartLabels);
        model.addAttribute("chartValues",     chartValues);
        
        // New dashboard widget attributes
        model.addAttribute("dailySoldLabels", dailySoldLabels);
        model.addAttribute("dailySoldValues", dailySoldValues);
        
        model.addAttribute("variants",        allVariants);
        model.addAttribute("categories",      categories);
        model.addAttribute("categoryMap",     categoryMap);

        return "warehouse/inventory";
    }

    /**
     * GET /warehouse/low-stock
     * Redirects to inventory tab — cảnh báo hết hàng is a tab inside inventory.html.
     */
    @GetMapping("/low-stock")
    public String lowStock() {
        return "redirect:/warehouse/inventory#tab-alerts";
    }

    /**
     * GET /warehouse/import-export
     * Redirects to inventory tab — log is a tab inside inventory.html.
     */
    @GetMapping("/import-export")
    public String importExport() {
        return "redirect:/warehouse/inventory#tab-log";
    }

    /**
     * GET /warehouse/update-stock
     * Redirects to inventory — update is done via modal on main page.
     */
    @GetMapping("/update-stock")
    public String updateStock() {
        return "redirect:/warehouse/inventory";
    }

    // ─── REST API ─────────────────────────────────────────────────

    /**
     * GET /warehouse/api/variants
     * Returns all product variants as JSON (for AJAX table refresh).
     */
    @GetMapping("/api/variants")
    @ResponseBody
    public ResponseEntity<List<ProductVariant>> getVariants() {
        return ResponseEntity.ok(productVariantRepository.findAll());
    }

    /**
     * GET /warehouse/api/variants/search?q=...
     * Returns variants whose SKU, color, or size matches the query.
     */
    @GetMapping("/api/variants/search")
    @ResponseBody
    public ResponseEntity<List<ProductVariant>> searchVariants(@RequestParam String q) {
        String lower = q.toLowerCase();
        List<ProductVariant> results = productVariantRepository.findAll().stream()
                .filter(v ->
                    (v.getSku()   != null && v.getSku().toLowerCase().contains(lower)) ||
                    (v.getColor() != null && v.getColor().toLowerCase().contains(lower)) ||
                    (v.getSize()  != null && v.getSize().toLowerCase().contains(lower)) ||
                    (v.getProduct() != null && v.getProduct().getName() != null &&
                     v.getProduct().getName().toLowerCase().contains(lower))
                )
                .toList();
        return ResponseEntity.ok(results);
    }

    /**
     * PUT /warehouse/api/variants/{id}/quantity
     * Updates the stock quantity for a given variant.
     * Body: { "type": "in|out|adj", "quantity": 5, "note": "..." }
     */
    @PutMapping("/api/variants/{id}/quantity")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ProductVariant variant = productVariantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể #" + id));

            String type = String.valueOf(body.getOrDefault("type", "adj"));
            int    qty  = Integer.parseInt(String.valueOf(body.getOrDefault("quantity", 0)));
            int    current = variant.getQuantity() != null ? variant.getQuantity() : 0;
            String note = (String) body.getOrDefault("note", "");

            int newQty;
            switch (type) {
                case "in"  -> newQty = current + qty;
                case "out" -> newQty = Math.max(0, current - qty);
                default    -> newQty = qty; // "adj" = set trực tiếp
            }

            int effectiveChange = newQty - current;

            variant.setQuantity(newQty);
            productVariantRepository.save(variant);

            poly.edu.entity.Account currentUser = accountRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Lỗi xác thực người dùng"));

            InventoryLog log = InventoryLog.builder()
                .variant(variant)
                .type(type)
                .oldQuantity(current)
                .changeAmount(effectiveChange)
                .newQuantity(newQty)
                .note(note)
                .account(currentUser)
                .build();
            inventoryLogRepository.save(log);

            return ResponseEntity.ok(Map.of(
                "success",    true,
                "sku",        variant.getSku(),
                "oldQuantity", current,
                "newQuantity", newQty,
                "message",    "Cập nhật tồn kho thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /warehouse/api/daily-sold?month=3&year=2026
     * Returns daily sold labels and values for any given month/year.
     */
    @GetMapping("/api/daily-sold")
    @ResponseBody
    public ResponseEntity<?> getDailySoldByMonth(
            @RequestParam int month,
            @RequestParam int year) {
        try {
            java.time.YearMonth ym = java.time.YearMonth.of(year, month);
            LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = ym.plusMonths(1).atDay(1).atStartOfDay();

            List<Object[]> dailySoldData = orderDetailRepository.getDailyUnitsSold(startOfMonth, endOfMonth);

            java.util.Map<Integer, Long> unitsByDay = new java.util.HashMap<>();
            for (Object[] row : dailySoldData) {
                java.util.Date date = (java.util.Date) row[0];
                java.time.LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
                unitsByDay.put(localDate.getDayOfMonth(), ((Number) row[1]).longValue());
            }

            int maxDays = ym.lengthOfMonth();
            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();
            for (int i = 1; i <= maxDays; i++) {
                labels.add(i + "/" + month);
                values.add(unitsByDay.getOrDefault(i, 0L));
            }

            return ResponseEntity.ok(Map.of(
                "labels", labels,
                "values", values,
                "month", month,
                "year", year
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /warehouse/api/sold-products
     * Returns products sold on a specific date string (e.g. "1/3")
     */
    @GetMapping("/api/sold-products")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getSoldProductsOnDate(
            @RequestParam String dateStr,
            @RequestParam(required = false) Integer year) {
        try {
            int day = Integer.parseInt(dateStr.split("/")[0]);
            int month = Integer.parseInt(dateStr.split("/")[1]);
            int resolvedYear = (year != null) ? year : LocalDate.now().getYear();

            LocalDateTime startOfDay = LocalDate.of(resolvedYear, month, day).atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            List<Object[]> products = orderDetailRepository.getCompletedProductsSoldOnDate(startOfDay, endOfDay);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /warehouse/api/variants/{id}/logs
     * Returns inventory log history for a specific variant as JSON.
     */
    @GetMapping("/api/variants/{id}/logs")
    @ResponseBody
    public ResponseEntity<?> getVariantLogs(@PathVariable Integer id) {
        try {
            ProductVariant variant = productVariantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể #" + id));

            List<InventoryLog> logs = inventoryLogRepository.findByVariantIdOrderByTimestampDesc(id);

            List<Map<String, Object>> result = logs.stream().map(log -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", log.getId());
                m.put("timestamp", log.getTimestamp().toString());
                m.put("type", log.getType());
                m.put("oldQuantity", log.getOldQuantity());
                m.put("changeAmount", log.getChangeAmount());
                m.put("newQuantity", log.getNewQuantity());
                m.put("note", log.getNote());
                m.put("accountName", log.getAccount() != null ? log.getAccount().getFullName() : "N/A");
                return m;
            }).toList();

            return ResponseEntity.ok(Map.of(
                "sku", variant.getSku(),
                "productName", variant.getProduct() != null ? variant.getProduct().getName() : "N/A",
                "color", variant.getColor() != null ? variant.getColor() : "",
                "size", variant.getSize() != null ? variant.getSize() : "",
                "currentQty", variant.getQuantity() != null ? variant.getQuantity() : 0,
                "logs", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
