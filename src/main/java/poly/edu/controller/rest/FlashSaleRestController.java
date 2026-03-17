package poly.edu.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.FlashSaleStatusDTO;
import poly.edu.entity.Account;
import poly.edu.repository.AccountRepository;
import poly.edu.service.FlashSaleService;

import java.security.Principal;
import java.util.Map;

/**
 * REST API for the Flash Sale countdown timer and claim system.
 * Frontend polls status endpoint and calls claim endpoint.
 */
@RestController
@RequestMapping("/api/flash-sale")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FlashSaleRestController {

    private final FlashSaleService flashSaleService;
    private final AccountRepository accountRepository;

    /**
     * GET /api/flash-sale/status
     * Returns current flash sale status with per-user claim info.
     */
    @GetMapping("/status")
    public ResponseEntity<FlashSaleStatusDTO> getStatus(Principal principal) {
        Integer accountId = null;
        if (principal != null) {
            Account acc = accountRepository.findByUsername(principal.getName()).orElse(null);
            if (acc != null) accountId = acc.getId();
        }
        FlashSaleStatusDTO status = flashSaleService.getFlashSaleStatus(accountId);
        return ResponseEntity.ok(status);
    }

    /**
     * POST /api/flash-sale/claim
     * Claim a flash sale voucher for the current user.
     */
    @PostMapping("/claim")
    public ResponseEntity<?> claimVoucher(@RequestBody Map<String, Integer> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Vui lòng đăng nhập để nhận mã Flash Sale!"
            ));
        }

        Account acc = accountRepository.findByUsername(principal.getName()).orElse(null);
        if (acc == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Không tìm thấy tài khoản."
            ));
        }

        Integer voucherId = body.get("voucherId");
        if (voucherId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Thiếu thông tin voucher."
            ));
        }

        try {
            String code = flashSaleService.claimVoucher(acc.getId(), voucherId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Nhận mã Flash Sale thành công!",
                "code", code
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
