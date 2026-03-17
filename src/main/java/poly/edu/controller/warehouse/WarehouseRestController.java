package poly.edu.controller.warehouse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.InventoryReceipt;
import poly.edu.entity.InventoryReceiptDetail;
import poly.edu.entity.ProductVariant;
import poly.edu.entity.Supplier;
import poly.edu.service.InventoryReceiptService;
import poly.edu.service.SupplierService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warehouse/api")
@PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
@RequiredArgsConstructor
public class WarehouseRestController {

    private final SupplierService supplierService;
    private final InventoryReceiptService receiptService;

    // ==========================================
    // SUPPLIER API
    // ==========================================

    @GetMapping("/suppliers")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @PostMapping("/suppliers")
    public ResponseEntity<?> createSupplier(@RequestBody Supplier supplier) {
        try {
            Supplier saved = supplierService.saveSupplier(supplier);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<?> updateSupplier(@PathVariable Integer id, @RequestBody Supplier supplierData) {
        try {
            Supplier existing = supplierService.getSupplierById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp"));
            existing.setName(supplierData.getName());
            existing.setPhone(supplierData.getPhone());
            existing.setEmail(supplierData.getEmail());
            existing.setAddress(supplierData.getAddress());
            existing.setTaxCode(supplierData.getTaxCode());
            existing.setIsActive(supplierData.getIsActive());

            Supplier saved = supplierService.saveSupplier(existing);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable Integer id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // INVENTORY RECEIPT API
    // ==========================================

    @GetMapping("/receipts")
    public ResponseEntity<List<InventoryReceipt>> getAllReceipts() {
        return ResponseEntity.ok(receiptService.getAllReceipts());
    }

    @GetMapping("/receipts/{id}")
    public ResponseEntity<?> getReceiptById(@PathVariable Integer id) {
        return receiptService.getReceiptById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/receipts")
    public ResponseEntity<?> createReceipt(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer supplierId = Integer.valueOf(payload.get("supplierId").toString());
            String note = payload.get("note") != null ? payload.get("note").toString() : "";
            
            List<Map<String, Object>> detailsList = (List<Map<String, Object>>) payload.get("details");
            List<InventoryReceiptDetail> details = new ArrayList<>();
            
            for (Map<String, Object> d : detailsList) {
                InventoryReceiptDetail detail = new InventoryReceiptDetail();
                ProductVariant pv = new ProductVariant();
                pv.setId(Integer.valueOf(d.get("variantId").toString()));
                detail.setProductVariant(pv);
                detail.setQuantity(Integer.valueOf(d.get("quantity").toString()));
                detail.setImportPrice(Double.valueOf(d.get("importPrice").toString()));
                details.add(detail);
            }

            InventoryReceipt receipt = receiptService.createReceipt(supplierId, details, note, userDetails.getUsername());
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/receipts/{id}/complete")
    public ResponseEntity<?> completeReceipt(@PathVariable Integer id) {
        try {
            InventoryReceipt receipt = receiptService.completeReceipt(id);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/receipts/{id}/cancel")
    public ResponseEntity<?> cancelReceipt(@PathVariable Integer id) {
        try {
            receiptService.cancelReceipt(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
