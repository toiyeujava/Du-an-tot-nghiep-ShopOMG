package poly.edu.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poly.edu.service.DashboardService;

import java.util.Map;

/**
 * AdminDashboardController - Handles dashboard statistics and overview.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why create a separate controller for just the dashboard?"
 * 
 * 1. Single Responsibility: Dashboard has its own concerns (statistics, charts)
 * 2. Testability: Can mock DashboardService without other dependencies
 * 3. Scalability: If we add more dashboard features (real-time, notifications),
 * this controller will grow naturally
 * 4. Following System Design principle: Separation of Concerns
 * 
 * Time Complexity Analysis:
 * - getDashboardStats(): O(1) for count queries, O(n) for revenue aggregation
 * - getRevenueChartData(7): O(7 * n) = O(n) where n = orders per month
 * 
 * Space Complexity: O(m) where m = number of chart data points
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * Display admin dashboard with statistics.
     * 
     * Algorithm:
     * 1. Call getDashboardStats() to get all metrics in one service call
     * 2. Add each metric to model
     * 3. Get chart data for last 7 months
     * 4. Return dashboard view
     * 
     * Why this approach?
     * - Using getDashboardStats() reduces database calls by batching queries
     * - 7 months for chart is a reasonable default for trend analysis
     * 
     * @param model Spring MVC Model
     * @return admin/dashboard view
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tổng quan - Admin");

        // Get dashboard statistics
        Map<String, Object> stats = dashboardService.getDashboardStats();
        model.addAttribute("monthlyRevenue", stats.get("monthlyRevenue"));
        model.addAttribute("pendingOrders", stats.get("pendingOrders"));
        model.addAttribute("totalCustomers", stats.get("totalCustomers"));
        model.addAttribute("totalProducts", stats.get("totalProducts"));
        model.addAttribute("recentOrders", stats.get("recentOrders"));
        model.addAttribute("topProducts", stats.get("topProducts"));

        // Get chart data (last 7 months)
        model.addAttribute("revenueChartData", dashboardService.getRevenueChartData(7));

        return "admin/dashboard";
    }
}
