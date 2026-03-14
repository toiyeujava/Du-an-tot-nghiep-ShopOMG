package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Voucher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    // 1. Tìm voucher theo mã code (áp dụng khi thanh toán)
    Optional<Voucher> findByCode(String code);

    // 2. Lấy danh sách voucher đang hoạt động, còn số lượng, và trong thời hạn
    @Query("SELECT v FROM Voucher v " +
           "WHERE v.isActive = true " +
           "AND v.quantity > 0 " +
           "AND v.startDate <= :now " +
           "AND v.endDate >= :now")
    List<Voucher> findAllValid(@Param("now") LocalDateTime now);

    // 3. Tìm kiếm voucher theo mã code có chứa từ khóa (cho Admin search)
    List<Voucher> findByCodeContainingIgnoreCase(String keyword);
}
