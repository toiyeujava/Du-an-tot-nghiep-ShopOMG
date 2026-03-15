package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.AccountVoucher;

import java.util.List;
import java.util.Optional;

public interface AccountVoucherRepository extends JpaRepository<AccountVoucher, Integer> {

    // Lấy danh sách voucher chưa dùng của 1 User
    @Query("SELECT av FROM AccountVoucher av WHERE av.account.id = :accountId AND av.isUsed = false")
    List<AccountVoucher> findUnusedVouchersByAccountId(@Param("accountId") Integer accountId);

    // Kiểm tra xem User đã lưu Voucher này chưa
    boolean existsByAccountIdAndVoucherId(Integer accountId, Integer voucherId);

    // Lấy bản ghi AccountVoucher cụ thể
    Optional<AccountVoucher> findByAccountIdAndVoucherId(Integer accountId, Integer voucherId);
}
