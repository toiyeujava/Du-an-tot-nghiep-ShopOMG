package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.InventoryReceipt;

@Repository
public interface InventoryReceiptRepository extends JpaRepository<InventoryReceipt, Integer> {
    boolean existsByReceiptCode(String receiptCode);
}
