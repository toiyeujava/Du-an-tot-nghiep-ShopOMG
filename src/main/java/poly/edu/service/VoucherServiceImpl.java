package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.dto.VoucherResponseDTO;
import poly.edu.entity.Voucher;
import poly.edu.repository.VoucherRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public VoucherResponseDTO applyVoucher(String code, BigDecimal cartTotalAmount) {
        // Dummy fallback for interface compatibility if needed, you should use the new one below
        return null;
    }

    public VoucherResponseDTO applyVoucher(poly.edu.dto.VoucherApplyRequestDTO request) {
        String code = request.getVoucherCode();
        BigDecimal cartTotalAmount = request.getCartTotalAmount();
        // 1. Tìm Voucher theo mã
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code);
        if (voucherOpt.isEmpty()) {
            return buildErrorResponse("Mã giảm giá không tồn tại");
        }

        Voucher voucher = voucherOpt.get();

        // 2. Kiểm tra isActive
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            return buildErrorResponse("Mã giảm giá đã bị vô hiệu hóa");
        }

        // 3. Kiểm tra quantity > 0
        if (voucher.getQuantity() == null || voucher.getQuantity() <= 0) {
            return buildErrorResponse("Mã giảm giá đã hết lượt sử dụng");
        }

        // 4. Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            return buildErrorResponse("Mã giảm giá chưa đến thời gian áp dụng hoặc đã hết hạn");
        }

        // 5. Kiểm tra điều kiện tối thiểu
        if (voucher.getMinOrderAmount() != null && cartTotalAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            return buildErrorResponse("Đơn hàng chưa đạt mức tối thiểu " + voucher.getMinOrderAmount() + "đ để áp dụng mã này");
        }

        // 6. Tính toán số tiền được giảm
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Custom logic cho voucher FREESHIP
        if (voucher.getCode() != null && voucher.getCode().toUpperCase().contains("FREESHIP")) {
            // Lấy phí ship từ request, nếu không thì mặc định 30k
            discountAmount = (request.getShippingFee() != null && request.getShippingFee().compareTo(BigDecimal.ZERO) > 0)
                    ? request.getShippingFee() : BigDecimal.valueOf(30000);
            
            // Giới hạn max freeship nếu có cấu hình
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else if (voucher.getDiscountPercent() != null && voucher.getDiscountPercent() > 0) {
            // Giảm theo %
            BigDecimal percent = new BigDecimal(voucher.getDiscountPercent()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            discountAmount = cartTotalAmount.multiply(percent);

            // Kiểm tra mức giảm tối đa
            if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else if (voucher.getDiscountAmount() != null && voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Giảm theo số tiền cố định
            discountAmount = voucher.getDiscountAmount();
        }

        // Không cho phép số tiền giảm chung lớn hơn tổng tiền giỏ hàng + ship
        BigDecimal maxAllowedDiscount = cartTotalAmount;
        if (request.getShippingFee() != null) {
            maxAllowedDiscount = maxAllowedDiscount.add(request.getShippingFee());
        }
        if (discountAmount.compareTo(maxAllowedDiscount) > 0) {
            discountAmount = maxAllowedDiscount;
        }

        // 7. Tính tổng tiền còn lại
        BigDecimal finalTotal = cartTotalAmount.subtract(discountAmount);

        // Trả kết quả thành công
        return VoucherResponseDTO.builder()
                .valid(true)
                .message("Áp dụng mã giảm giá thành công!")
                .discountAmount(discountAmount)
                .finalTotal(finalTotal)
                .build();
    }

    private VoucherResponseDTO buildErrorResponse(String message) {
        return VoucherResponseDTO.builder()
                .valid(false)
                .message(message)
                .discountAmount(BigDecimal.ZERO)
                .finalTotal(BigDecimal.ZERO)
                .build();
    }
}
