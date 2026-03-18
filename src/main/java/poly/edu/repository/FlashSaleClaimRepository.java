package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.entity.FlashSaleClaim;

import java.util.List;

public interface FlashSaleClaimRepository extends JpaRepository<FlashSaleClaim, Integer> {

    boolean existsByAccountIdAndVoucherId(Integer accountId, Integer voucherId);

    List<FlashSaleClaim> findByAccountId(Integer accountId);
}
