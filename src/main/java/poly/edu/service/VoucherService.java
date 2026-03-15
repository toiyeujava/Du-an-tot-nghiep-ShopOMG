package poly.edu.service;

import poly.edu.dto.VoucherResponseDTO;
import java.math.BigDecimal;

public interface VoucherService {
    VoucherResponseDTO applyVoucher(String code, BigDecimal cartTotalAmount);
    VoucherResponseDTO applyVoucher(poly.edu.dto.VoucherApplyRequestDTO request);
}
