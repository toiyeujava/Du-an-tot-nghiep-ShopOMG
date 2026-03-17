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
    public InventoryReceipt createReceipt(Integer supplierId, List<InventoryReceiptDetail> details, String note, String username) {
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
        for(InventoryReceiptDetail detail : details) {
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
        receipt.setStatus("CANCELLED");
        receiptRepository.save(receipt);
    }
}
