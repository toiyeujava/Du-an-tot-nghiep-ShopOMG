package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.*;
import poly.edu.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import poly.edu.utils.NumberToWordsConverter;

@Service
@RequiredArgsConstructor
public class InventoryReceiptService {

    private final InventoryReceiptRepository receiptRepository;
    private final InventoryReceiptDetailRepository detailRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryLogRepository logRepository;
    private final SupplierRepository supplierRepository;
    private final AccountRepository accountRepository;

    public List<InventoryReceipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    public Optional<InventoryReceipt> getReceiptById(Integer id) {
        return receiptRepository.findById(id);
    }

    @Transactional
    public InventoryReceipt createReceipt(Integer supplierId, List<InventoryReceiptDetail> details, String note,
            String username) {
        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        InventoryReceipt receipt = new InventoryReceipt();
        receipt.setAccount(account);
        receipt.setSupplier(supplier);
        receipt.setStatus("PENDING");
        receipt.setNote(note);

        // Generate receipt code: PNyyMMddXXXX
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        receipt.setReceiptCode("PN" + dateStr + randomSuffix);

        double total = 0;
        for (InventoryReceiptDetail detail : details) {
            ProductVariant variant = variantRepository.findById(detail.getProductVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            detail.setProductVariant(variant);
            total += detail.getQuantity() * (detail.getImportPrice() != null ? detail.getImportPrice() : 0);
            detail.setInventoryReceipt(receipt);
        }
        receipt.setTotalAmount(total);
        receipt.setReceiptDetails(details);

        return receiptRepository.save(receipt);
    }

    @Transactional
    public InventoryReceipt updateReceipt(Integer id, Integer supplierId, List<InventoryReceiptDetail> newDetails,
            String note) {
        InventoryReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        if (!"PENDING".equals(receipt.getStatus())) {
            throw new RuntimeException("Chỉ có thể sửa phiếu đang ở trạng thái PENDING");
        }

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        receipt.setSupplier(supplier);
        receipt.setNote(note);

        // Clear existing details (since orphanRemoval = true)
        receipt.getReceiptDetails().clear();

        double total = 0;
        for (InventoryReceiptDetail detail : newDetails) {
            ProductVariant variant = variantRepository.findById(detail.getProductVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            detail.setProductVariant(variant);
            total += detail.getQuantity() * (detail.getImportPrice() != null ? detail.getImportPrice() : 0);
            detail.setInventoryReceipt(receipt);
            receipt.getReceiptDetails().add(detail);
        }
        receipt.setTotalAmount(total);

        return receiptRepository.save(receipt);
    }

    @Transactional
    public InventoryReceipt completeReceipt(Integer id) {
        InventoryReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        if (!"PENDING".equals(receipt.getStatus())) {
            throw new RuntimeException("Chỉ có thể hoàn thành phiếu đang ở trạng thái PENDING");
        }

        for (InventoryReceiptDetail detail : receipt.getReceiptDetails()) {
            ProductVariant variant = detail.getProductVariant();
            int currentQty = variant.getQuantity() != null ? variant.getQuantity() : 0;
            int importQty = detail.getQuantity();
            int newQty = currentQty + importQty;

            variant.setQuantity(newQty);
            variantRepository.save(variant);

            InventoryLog log = InventoryLog.builder()
                    .variant(variant)
                    .type("in")
                    .oldQuantity(currentQty)
                    .changeAmount(importQty)
                    .newQuantity(newQty)
                    .note("Nhập kho từ phiếu: " + receipt.getReceiptCode())
                    .account(receipt.getAccount())
                    .timestamp(LocalDateTime.now())
                    .build();
            logRepository.save(log);
        }

        receipt.setStatus("COMPLETED");
        return receiptRepository.save(receipt);
    }

    @Transactional
    public void cancelReceipt(Integer id) {
        InventoryReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        if (!"PENDING".equals(receipt.getStatus())) {
            throw new RuntimeException("Chỉ có thể hủy phiếu đang ở trạng thái PENDING");
        }
        receiptRepository.delete(receipt);
    }

    public byte[] exportReceiptToExcel(Integer id) throws Exception {
        InventoryReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Phiếu Nhập Kho");
            sheet.setColumnWidth(0, 1500); // STT
            sheet.setColumnWidth(1, 8000); // Tên sản phẩm
            sheet.setColumnWidth(2, 4000); // Mã số
            sheet.setColumnWidth(3, 3000); // ĐVT
            sheet.setColumnWidth(4, 3000); // SL Theo chứng từ
            sheet.setColumnWidth(5, 3000); // SL Thực nhập
            sheet.setColumnWidth(6, 4000); // Đơn giá
            sheet.setColumnWidth(7, 4500); // Thành tiền

            // Styles
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle italicCenterStyle = workbook.createCellStyle();
            Font italicFont = workbook.createFont();
            italicFont.setItalic(true);
            italicCenterStyle.setFont(italicFont);
            italicCenterStyle.setAlignment(HorizontalAlignment.CENTER);

            // Table Header Style
            CellStyle tableHeaderStyle = workbook.createCellStyle();
            tableHeaderStyle.setFont(boldFont);
            tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            tableHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            tableHeaderStyle.setBorderTop(BorderStyle.THIN);
            tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
            tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
            tableHeaderStyle.setBorderRight(BorderStyle.THIN);
            tableHeaderStyle.setWrapText(true);

            // Table Cell Style
            CellStyle tableCellStyle = workbook.createCellStyle();
            tableCellStyle.setBorderTop(BorderStyle.THIN);
            tableCellStyle.setBorderBottom(BorderStyle.THIN);
            tableCellStyle.setBorderLeft(BorderStyle.THIN);
            tableCellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle numCellStyle = workbook.createCellStyle();
            numCellStyle.cloneStyleFrom(tableCellStyle);
            DataFormat format = workbook.createDataFormat();
            numCellStyle.setDataFormat(format.getFormat("#,##0"));

            // Header Section
            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("SHOP OMG!");
            c0.setCellStyle(boldStyle);

            Cell c5 = r0.createCell(5);
            c5.setCellValue("Mẫu số: 01 - VT");
            c5.setCellStyle(boldStyle);

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Địa chỉ: FPT Polytechnic");
            r1.createCell(5).setCellValue("(Ban hành theo Thông tư số 133/2016/TT-BTC");

            Row r2 = sheet.createRow(2);
            r2.createCell(5).setCellValue("Ngày 26/08/2016 của Bộ Tài chính)");

            // Title section
            Row r4 = sheet.createRow(4);
            Cell titleCell = r4.createCell(0);
            titleCell.setCellValue("PHIẾU NHẬP KHO");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 7));

            LocalDateTime rTime = receipt.getCreatedAt() != null ? receipt.getCreatedAt() : LocalDateTime.now();
            Row r5 = sheet.createRow(5);
            Cell dateCell = r5.createCell(0);
            dateCell.setCellValue(String.format("Ngày %02d tháng %02d năm %d", rTime.getDayOfMonth(),
                    rTime.getMonthValue(), rTime.getYear()));
            dateCell.setCellStyle(italicCenterStyle);
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 7));

            Row r6 = sheet.createRow(6);
            Cell codeCell = r6.createCell(0);
            codeCell.setCellValue("Số: " + receipt.getReceiptCode());
            codeCell.setCellStyle(centerStyle);
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 7));

            // Info section
            int rowIdx = 8;
            sheet.createRow(rowIdx++).createCell(0)
                    .setCellValue("- Họ và tên người giao: " + receipt.getSupplier().getName());
            sheet.createRow(rowIdx++).createCell(0)
                    .setCellValue("- Theo hóa đơn số: ................ ngày ...... tháng ...... năm ...... của "
                            + receipt.getSupplier().getName());
            sheet.createRow(rowIdx++).createCell(0)
                    .setCellValue("- Nhập tại kho: Kho của Shop OMG!   Địa điểm: ................................");

            // Table Header
            rowIdx++;
            Row th1 = sheet.createRow(rowIdx);
            th1.setHeight((short) 600);
            String[] headers = { "STT", "Tên, nhãn hiệu, quy cách,\nphẩm chất vật tư, dụng cụ\nsản phẩm, hàng hóa",
                    "Mã số", "Đơn vị\ntính", "Số lượng", "", "Đơn giá", "Thành tiền" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = th1.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(tableHeaderStyle);
            }
            Row th2 = sheet.createRow(rowIdx + 1);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = th2.createCell(i);
                cell.setCellStyle(tableHeaderStyle);
            }
            th2.getCell(4).setCellValue("Theo\nchứng từ");
            th2.getCell(5).setCellValue("Thực\nnhập");

            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + 1, 0, 0)); // STT
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + 1, 1, 1)); // Tên
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + 1, 2, 2)); // Mã số
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + 1, 3, 3)); // ĐVT
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 4, 5)); // Số lượng
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + 1, 6, 6)); // Đơn giá
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + 1, 7, 7)); // Thành tiền

            rowIdx += 2;
            Row th3 = sheet.createRow(rowIdx);
            String[] colLetters = { "A", "B", "C", "D", "1", "2", "3", "4" };
            for (int i = 0; i < colLetters.length; i++) {
                Cell cell = th3.createCell(i);
                cell.setCellValue(colLetters[i]);
                cell.setCellStyle(tableHeaderStyle);
            }

            rowIdx++;
            // Table Data
            int stt = 1;
            double sumTotal = 0;
            for (InventoryReceiptDetail detail : receipt.getReceiptDetails()) {
                Row row = sheet.createRow(rowIdx++);

                Cell c_stt = row.createCell(0);
                c_stt.setCellValue(stt++);
                c_stt.setCellStyle(tableCellStyle);
                Cell c_name = row.createCell(1);
                c_name.setCellValue(detail.getProductVariant().getProduct().getName() + " - "
                        + detail.getProductVariant().getColor() + " - " + detail.getProductVariant().getSize());
                c_name.setCellStyle(tableCellStyle);
                Cell c_sku = row.createCell(2);
                c_sku.setCellValue(detail.getProductVariant().getSku());
                c_sku.setCellStyle(tableCellStyle);
                Cell c_dvt = row.createCell(3);
                c_dvt.setCellValue("Cái");
                c_dvt.setCellStyle(tableCellStyle);

                Cell c_sl1 = row.createCell(4);
                c_sl1.setCellValue(detail.getQuantity());
                c_sl1.setCellStyle(tableCellStyle);
                Cell c_sl2 = row.createCell(5);
                c_sl2.setCellValue(detail.getQuantity());
                c_sl2.setCellStyle(tableCellStyle);

                Cell c_price = row.createCell(6);
                c_price.setCellValue(detail.getImportPrice() != null ? detail.getImportPrice() : 0);
                c_price.setCellStyle(numCellStyle);

                double amount = detail.getQuantity() * (detail.getImportPrice() != null ? detail.getImportPrice() : 0);
                sumTotal += amount;
                Cell c_amount = row.createCell(7);
                c_amount.setCellValue(amount);
                c_amount.setCellStyle(numCellStyle);
            }

            // Total Row
            Row rowTotal = sheet.createRow(rowIdx++);
            Cell cTotalText = rowTotal.createCell(0);
            cTotalText.setCellValue("Cộng");
            cTotalText.setCellStyle(tableHeaderStyle);
            for (int i = 1; i <= 6; i++) {
                rowTotal.createCell(i).setCellStyle(tableCellStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 6)); // Merge "Cộng"

            Cell cTotalValue = rowTotal.createCell(7);
            cTotalValue.setCellValue(sumTotal);
            cTotalValue.setCellStyle(numCellStyle);

            // Text Amount
            rowIdx++;
            sheet.createRow(rowIdx++).createCell(0)
                    .setCellValue("- Tổng số tiền (Viết bằng chữ): " + NumberToWordsConverter.convert((long) sumTotal));
            sheet.createRow(rowIdx++).createCell(0).setCellValue(
                    "- Số chứng từ gốc kèm theo: ..............................................................");

            // Signatures
            rowIdx++;
            Row signDateRow = sheet.createRow(rowIdx++);
            Cell cDateSign = signDateRow.createCell(5);
            cDateSign.setCellValue(String.format("Ngày %02d tháng %02d năm %d", rTime.getDayOfMonth(),
                    rTime.getMonthValue(), rTime.getYear()));
            cDateSign.setCellStyle(italicCenterStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 5, 7));

            Row signRow = sheet.createRow(rowIdx++);
            Cell s1 = signRow.createCell(1);
            s1.setCellValue("Người lập phiếu");
            s1.setCellStyle(boldStyle);
            Cell s2 = signRow.createCell(5);
            s2.setCellValue("Người giao hàng");
            s2.setCellStyle(boldStyle);

            Row signDescRow = sheet.createRow(rowIdx++);
            Cell sd1 = signDescRow.createCell(1);
            sd1.setCellValue("(Ký, họ tên)");
            sd1.setCellStyle(italicCenterStyle);
            Cell sd2 = signDescRow.createCell(5);
            sd2.setCellValue("(Ký, họ tên)");
            sd2.setCellStyle(italicCenterStyle);

            // Name of creator
            rowIdx += 4;
            Row nameRow = sheet.createRow(rowIdx);
            Cell n1 = nameRow.createCell(1);
            n1.setCellValue(receipt.getAccount().getFullName());
            n1.setCellStyle(centerStyle);
            Cell n2 = nameRow.createCell(5);
            n2.setCellValue(receipt.getSupplier().getName());
            n2.setCellStyle(centerStyle);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }
}
