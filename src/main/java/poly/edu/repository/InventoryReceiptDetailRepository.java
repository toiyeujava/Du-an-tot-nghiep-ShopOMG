package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.InventoryReceiptDetail;

@Repository
public interface InventoryReceiptDetailRepository extends JpaRepository<InventoryReceiptDetail, Integer> {
}
