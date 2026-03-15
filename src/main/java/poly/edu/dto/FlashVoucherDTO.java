package poly.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashVoucherDTO {
    private String code;
    private Integer discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer remainingQuantity;
}
