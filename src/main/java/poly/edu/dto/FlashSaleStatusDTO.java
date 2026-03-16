package poly.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleStatusDTO {
    private String status;          // "UPCOMING", "ACTIVE", "ENDED"
    private long remainingSeconds;  // seconds until start (UPCOMING) or end (ACTIVE)
    private int startHour;          // 11
    private int endHour;            // 13
    private List<FlashVoucherDTO> vouchers;
}
