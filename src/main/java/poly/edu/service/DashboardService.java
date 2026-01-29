package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    /**
     * Get monthly revenue for current month
     * Algorithm: SQL SUM aggregation with date filtering
     * Time Complexity: O(n) where n = orders in current month
     * Space Complexity: O(1)
     * 
     * Query: SELECT SUM(final_amount) FROM Orders
     * WHERE status = 'COMPLETED'
     * AND MONTH(order_date) = current_month
     * AND YEAR(order_date) = current_year
     */
    public BigDecimal getMonthlyRevenue() {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        BigDecimal revenue = orderRepository.getMonthlyRevenue(currentMonth, currentYear);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get revenue for specific month
     */
    public BigDecimal getMonthlyRevenue(int month, int year) {
        BigDecimal revenue = orderRepository.getMonthlyRevenue(month, year);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get pending order count
     * Algorithm: Simple COUNT query with status filter
     * Time Complexity: O(1) - database indexed query
     */
    public long getPendingOrderCount() {
        return orderRepository.countByStatus("PENDING");
    }

    /**
     * Get total customer count
     * Algorithm: COUNT query with role filter
     * Time Complexity: O(1)
     */
    public long getTotalCustomers() {
        return accountRepository.findAll().stream()
                .filter(account -> account.getRole() != null &&
                        "USER".equals(account.getRole().getName()))
                .count();
    }

    /**
     * Get total active products
     * Algorithm: COUNT query with isActive filter
     * Time Complexity: O(1)
     */
    public long getTotalProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .count();
    }

    /**
     * Get revenue chart data for last N months
     * Algorithm:
     * 1. Calculate date range (last N months)
     * 2. For each month, query revenue
     * 3. Build array of [month_label, revenue]
     * 
     * Time Complexity: O(m * n) where m = months, n = orders per month
     * Space Complexity: O(m) for storing month data
     * 
     * Data Structure: List of Maps for chart data
     */
    public List<Map<String, Object>> getRevenueChartData(int months) {
        List<Map<String, Object>> chartData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime targetDate = now.minusMonths(i);
            int month = targetDate.getMonthValue();
            int year = targetDate.getYear();

            BigDecimal revenue = getMonthlyRevenue(month, year);

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("month", String.format("T%d", month));
            dataPoint.put("year", year);
            dataPoint.put("revenue", revenue);

            chartData.add(dataPoint);
        }

        return chartData;
    }

    /**
     * Get daily revenue for current month (for detailed chart)
     * Algorithm: GROUP BY date aggregation
     * Time Complexity: O(n) where n = orders in month
     */
    public List<Map<String, Object>> getDailyRevenueForMonth() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth yearMonth = YearMonth.from(now);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Object[]> results = orderRepository.getDailyRevenue(startOfMonth, endOfMonth);

        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", result[0]);
            dataPoint.put("revenue", result[1]);
            dailyData.add(dataPoint);
        }

        return dailyData;
    }

    /**
     * Get top selling products
     * Algorithm: JOIN + GROUP BY + ORDER BY + LIMIT
     * Time Complexity: O(n log n) where n = order details (for sorting)
     * Space Complexity: O(k) where k = limit
     * 
     * Data Structure: List of product statistics
     */
    public List<Map<String, Object>> getTopProducts(int limit) {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        List<Object[]> results = orderDetailRepository.getTopSellingProducts(
                currentMonth, currentYear, limit);

        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", result[0]);
            product.put("productName", result[1]);
            product.put("productImage", result[2]);
            product.put("totalSold", result[3]);
            product.put("totalRevenue", result[4]);
            topProducts.add(product);
        }

        return topProducts;
    }

    /**
     * Get dashboard statistics summary
     * Returns all key metrics in one call
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("monthlyRevenue", getMonthlyRevenue());
        stats.put("pendingOrders", getPendingOrderCount());
        stats.put("totalCustomers", getTotalCustomers());
        stats.put("totalProducts", getTotalProducts());
        stats.put("recentOrders", orderRepository.findTop10ByOrderByOrderDateDesc());
        stats.put("topProducts", getTopProducts(5));

        return stats;
    }

    /**
     * Get order statistics by status
     */
    public Map<String, Long> getOrderStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("PENDING", orderRepository.countByStatus("PENDING"));
        stats.put("CONFIRMED", orderRepository.countByStatus("CONFIRMED"));
        stats.put("SHIPPING", orderRepository.countByStatus("SHIPPING"));
        stats.put("COMPLETED", orderRepository.countByStatus("COMPLETED"));
        stats.put("CANCELLED", orderRepository.countByStatus("CANCELLED"));

        return stats;
    }
}
