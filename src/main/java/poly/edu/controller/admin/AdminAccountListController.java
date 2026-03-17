package poly.edu.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.entity.Account;
import poly.edu.entity.Role;
import poly.edu.repository.RoleRepository;
import poly.edu.service.AdminAccountService;
import poly.edu.service.ExcelExportService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AdminAccountListController — accounts list + Excel export.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why is listing separated from viewing a single account?"
 *
 * The list view aggregates across all accounts (pagination, role filter,
 * statistics, export). The detail view focuses on one account's full history.
 * Keeping them in separate controllers means:
 * 1. Each class has a single clear job.
 * 2. Adding search/filter to the list doesn't pollute the detail logic.
 * 3. Role-based access guards can be applied per controller independently.
 *
 * Alternative considered: one AdminAccountController doing everything.
 * Rejected because: it grew past 250 lines and mixes read-aggregate concerns
 * with single-entity concerns, making each harder to test in isolation.
 */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountListController {

    private final AdminAccountService adminAccountService;
    private final ExcelExportService excelExportService;
    private final RoleRepository roleRepository;

    @GetMapping
    public String accounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý tài khoản");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Account> accountPage;
        if (role != null && !role.trim().isEmpty()) {
            accountPage = adminAccountService.getUsersByRole(role, pageable);
            model.addAttribute("selectedRole", role);
        } else {
            accountPage = adminAccountService.getAllUsers(pageable);
            model.addAttribute("selectedRole", "");
        }

        model.addAttribute("accounts", accountPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accountPage.getTotalPages());
        model.addAttribute("totalItems", accountPage.getTotalElements());
        model.addAttribute("activeUsers", adminAccountService.getActiveUsersCount());
        model.addAttribute("lockedUsers", adminAccountService.getLockedUsersCount());

        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);

        return "admin/accounts";
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Account> allUsers = adminAccountService.getAllUsers(pageable);
        excelExportService.exportAccounts(allUsers.getContent(), response.getOutputStream());
    }
}
