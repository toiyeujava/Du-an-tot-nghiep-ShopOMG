package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import poly.edu.entity.Account;
import poly.edu.entity.Order;
import poly.edu.entity.Product;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ExcelExportService - Exports data to Excel files.
 * 
 * Rubber Duck Explanation:
 * -------------------------
 * "Why use Apache POI for Excel?"
 * 
 * 1. Industry standard for Java → Excel
 * 2. Supports both .xls (HSSF) and .xlsx (XSSF)
 * 3. Can handle large datasets with streaming
 * 4. Good documentation and community support
 * 
 * "Why use XSSF (xlsx) format?"
 * 
 * - .xlsx supports more rows (1M vs 65K)
 * - Smaller file size (ZIP compressed)
 * - Modern Excel standard (Office 2007+)
 * 
 * Time Complexity: O(n) where n = number of records
 * Space Complexity: O(n) for workbook in memory
 * 
 * For very large datasets (100K+), consider SXSSFWorkbook (streaming).
 */
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export accounts to Excel.
     * 
     * Columns: ID, Username, Email, Fullname, Phone, Role, Status, Created
     */
    public void exportAccounts(List<Account> accounts, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // Header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Username", "Email", "Họ tên", "SĐT", "Vai trò", "Trạng thái", "Ngày tạo" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Account acc : accounts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(acc.getId());
                row.createCell(1).setCellValue(acc.getUsername() != null ? acc.getUsername() : "");
                row.createCell(2).setCellValue(acc.getEmail() != null ? acc.getEmail() : "");
                row.createCell(3).setCellValue(acc.getFullName() != null ? acc.getFullName() : "");
                row.createCell(4).setCellValue(acc.getPhone() != null ? acc.getPhone() : "");
                row.createCell(5).setCellValue(acc.getRole() != null ? acc.getRole().getName() : "USER");
                row.createCell(6)
                        .setCellValue(acc.getIsActive() != null && acc.getIsActive() ? "Hoạt động" : "Đã khóa");
                row.createCell(7)
                        .setCellValue(acc.getCreatedAt() != null ? acc.getCreatedAt().format(DATE_FORMAT) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * Export orders to Excel.
     * 
     * Columns: ID, Customer, Phone, Date, Status, Payment, Total
     */
    public void exportOrders(List<Order> orders, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "Mã đơn", "Khách hàng", "SĐT", "Ngày đặt", "Trạng thái", "Thanh toán", "Tổng tiền" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("ORD-" + order.getId());
                row.createCell(1).setCellValue(order.getReceiverName() != null ? order.getReceiverName()
                        : (order.getAccount() != null ? order.getAccount().getUsername() : ""));
                row.createCell(2).setCellValue(order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
                row.createCell(3)
                        .setCellValue(order.getOrderDate() != null ? order.getOrderDate().format(DATE_FORMAT) : "");
                row.createCell(4).setCellValue(order.getStatus() != null ? order.getStatus() : "");
                row.createCell(5).setCellValue(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
                row.createCell(6)
                        .setCellValue(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * Export products to Excel.
     * Note: Product entity has categoryId (Integer) not category object,
     * and doesn't have sku or quantity fields directly.
     */
    public void exportProducts(List<Product> products, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Tên SP", "Slug", "Danh mục ID", "Giá", "Giảm giá %", "Trạng thái" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName() != null ? product.getName() : "");
                row.createCell(2).setCellValue(product.getSlug() != null ? product.getSlug() : "");
                row.createCell(3).setCellValue(product.getCategoryId() != null ? product.getCategoryId() : 0);
                row.createCell(4).setCellValue(product.getPrice() != null ? product.getPrice() : 0);
                row.createCell(5).setCellValue(product.getDiscount() != null ? product.getDiscount() : 0);
                row.createCell(6)
                        .setCellValue(product.getIsActive() != null && product.getIsActive() ? "Đang bán" : "Ẩn");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * Create header cell style.
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
