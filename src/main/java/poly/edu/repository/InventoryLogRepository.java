package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.entity.InventoryLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {
    List<InventoryLog> findAllByOrderByTimestampDesc();
    List<InventoryLog> findByVariantIdOrderByTimestampDesc(Integer variantId);
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
