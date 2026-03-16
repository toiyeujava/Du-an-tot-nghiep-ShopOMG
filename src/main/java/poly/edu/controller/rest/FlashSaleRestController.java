package poly.edu.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.dto.FlashSaleStatusDTO;
import poly.edu.service.FlashSaleService;

/**
 * REST API for the Flash Sale countdown timer.
 * Frontend polls this endpoint to get the current sale status,
 * remaining time, and voucher codes.
 */
@RestController
@RequestMapping("/api/flash-sale")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FlashSaleRestController {

    private final FlashSaleService flashSaleService;

    /**
     * GET /api/flash-sale/status
     *
     * Returns the current flash sale status:
     * - status: "UPCOMING" | "ACTIVE" | "ENDED"
     * - remainingSeconds: countdown value
     * - startHour / endHour: 11 / 13
     * - vouchers: list of available flash sale voucher codes (only when ACTIVE)
     */
    @GetMapping("/status")
    public ResponseEntity<FlashSaleStatusDTO> getStatus() {
        FlashSaleStatusDTO status = flashSaleService.getFlashSaleStatus();
        return ResponseEntity.ok(status);
    }
}
