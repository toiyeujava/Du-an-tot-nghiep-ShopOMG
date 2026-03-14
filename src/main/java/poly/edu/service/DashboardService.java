package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.entity.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
     * Get monthly revenue for current month (COMPLETED orders only).
     */
    public BigDecimal getMonthlyRevenue() {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        BigDecimal revenue = orderRepository.getMonthlyRevenue(currentMonth, currentYear);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get revenue for specific month (COMPLETED orders only).
     */
    public BigDecimal getMonthlyRevenue(int month, int year) {
        BigDecimal revenue = orderRepository.getMonthlyRevenue(month, year);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Calculate revenue growth percentage vs previous month.
     * Formula: ((current - previous) / previous) * 100
     * Returns 100.0 if previous = 0 and current > 0, else 0.0.
     */
    public double getRevenueGrowthPercent() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentYM = YearMonth.from(now);
        YearMonth previousYM = currentYM.minusMonths(1);

        BigDecimal current = getMonthlyRevenue(currentYM.getMonthValue(), currentYM.getYear());
        BigDecimal previous = getMonthlyRevenue(previousYM.getMonthValue(), previousYM.getYear());

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Get revenue breakdown by order status for current month.
     * Returns map: status -> SUM(finalAmount)
     */
    public Map<String, BigDecimal> getRevenueByStatus() {
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Object[]> results = orderRepository.getRevenueByStatus(month, year);
        Map<String, BigDecimal> revenueByStatus = new LinkedHashMap<>();

        // Initialize all statuses with ZERO
        for (String status : Arrays.asList("PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED")) {
            revenueByStatus.put(status, BigDecimal.ZERO);
        }

        // Fill in actual values from DB
        for (Object[] row : results) {
            String status = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            revenueByStatus.put(status, amount != null ? amount : BigDecimal.ZERO);
        }

        return revenueByStatus;
    }

    /**
     * Get pending order count.
     */
    public long getPendingOrderCount() {
        return orderRepository.countByStatus("PENDING");
    }

    /**
     * Get total customer count (accounts with role USER).
     */
    public long getTotalCustomers() {
        return accountRepository.findAll().stream()
                .filter(account -> account.getRole() != null &&
                        "USER".equals(account.getRole().getName()))
                .count();
    }

    /**
     * Get total active products.
     */
    public long getTotalProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .count();
    }

    /**
     * Get revenue chart data for last N months (COMPLETED orders only).
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
     * Get hourly revenue for today (COMPLETED orders only).
     */
    public List<Map<String, Object>> getDailyRevenueByHour() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<Object[]> results = orderRepository.getHourlyRevenue(startOfDay, endOfDay);

        Map<Integer, BigDecimal> hourMap = new HashMap<>();
        for (Object[] result : results) {
            int hour = ((Number) result[0]).intValue();
            BigDecimal revenue = (BigDecimal) result[1];
            hourMap.put(hour, revenue != null ? revenue : BigDecimal.ZERO);
        }

        List<Map<String, Object>> hourlyData = new ArrayList<>();
        for (int h = 0; h <= 23; h++) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("label", String.format("%02d:00", h));
            dataPoint.put("revenue", hourMap.getOrDefault(h, BigDecimal.ZERO));
            hourlyData.add(dataPoint);
        }
        return hourlyData;
    }

    /**
     * Get daily revenue for current week Mon-Sun (COMPLETED orders only).
     */
    public List<Map<String, Object>> getWeeklyRevenueByDay() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDateTime startDT = startOfWeek.atStartOfDay();
        LocalDateTime endDT = endOfWeek.atTime(23, 59, 59);

        List<Object[]> results = orderRepository.getDailyRevenue(startDT, endDT);

        Map<String, BigDecimal> dayMap = new HashMap<>();
        for (Object[] result : results) {
            String dateStr = result[0].toString();
            BigDecimal revenue = (BigDecimal) result[1];
            dayMap.put(dateStr, revenue != null ? revenue : BigDecimal.ZERO);
        }

        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("label", dayNames[i]);
            dataPoint.put("revenue", dayMap.getOrDefault(day.toString(), BigDecimal.ZERO));
            weeklyData.add(dataPoint);
        }
        return weeklyData;
    }

    /**
     * Get daily revenue for current month with all days filled (COMPLETED orders only).
     */
    public List<Map<String, Object>> getMonthlyRevenueByDay() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth yearMonth = YearMonth.from(now);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Object[]> results = orderRepository.getDailyRevenue(startOfMonth, endOfMonth);

        Map<String, BigDecimal> dayMap = new HashMap<>();
        for (Object[] result : results) {
            String dateStr = result[0].toString();
            BigDecimal revenue = (BigDecimal) result[1];
            dayMap.put(dateStr, revenue != null ? revenue : BigDecimal.ZERO);
        }

        List<Map<String, Object>> dailyData = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("label", String.valueOf(day));
            dataPoint.put("revenue", dayMap.getOrDefault(date.toString(), BigDecimal.ZERO));
            dailyData.add(dataPoint);
        }
        return dailyData;
    }

    /**
     * Get monthly revenue for current year T1-T12 (COMPLETED orders only).
     */
    public List<Map<String, Object>> getYearlyRevenueByMonth() {
        int currentYear = LocalDateTime.now().getYear();
        List<Map<String, Object>> yearlyData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("label", String.format("T%d", month));
            dataPoint.put("revenue", getMonthlyRevenue(month, currentYear));
            yearlyData.add(dataPoint);
        }
        return yearlyData;
    }

    /**
     * Get top selling products by revenue in the current month.
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
     * Get all dashboard stats in a single call.
     * Includes: monthlyRevenue, revenueGrowth, revenueByStatus,
     * pendingOrders, totalCustomers, totalProducts, activeOrders, topProducts.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("monthlyRevenue", getMonthlyRevenue());
        stats.put("revenueGrowth", getRevenueGrowthPercent());
        stats.put("revenueByStatus", getRevenueByStatus());
        stats.put("pendingOrders", getPendingOrderCount());
        stats.put("totalCustomers", getTotalCustomers());
        stats.put("totalProducts", getTotalProducts());
        // Only non-completed, non-cancelled orders for the active list
        stats.put("activeOrders", getActiveOrders());
        stats.put("orderStatsByStatus", getOrderStatsByStatus());
        stats.put("topProducts", getTopProducts(5));

        return stats;
    }

    /**
     * Get active (in-progress) orders: PENDING, CONFIRMED, SHIPPING only.
     * COMPLETED and CANCELLED are excluded.
     */
    public List<Order> getActiveOrders() {
        List<Order> active = new ArrayList<>();
        active.addAll(orderRepository.findByStatus("PENDING"));
        active.addAll(orderRepository.findByStatus("CONFIRMED"));
        active.addAll(orderRepository.findByStatus("SHIPPING"));
        // Sort by orderDate descending
        active.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));
        return active;
    }

    /**
     * Get order count statistics by status.
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
