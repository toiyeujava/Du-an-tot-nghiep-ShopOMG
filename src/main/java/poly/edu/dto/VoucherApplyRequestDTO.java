package poly.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherApplyRequestDTO {

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    private String voucherCode;

    @NotNull(message = "Không thể xác định tổng tiền giỏ hàng")
    @Min(value = 0, message = "Tổng tiền không hợp lệ")
    private BigDecimal cartTotalAmount;

    private BigDecimal shippingFee; // Optional, only needed for FREESHIP vouchers
}
