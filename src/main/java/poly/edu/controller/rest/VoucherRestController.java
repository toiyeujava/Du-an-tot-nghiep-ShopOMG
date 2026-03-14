package poly.edu.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.VoucherApplyRequestDTO;
import poly.edu.dto.VoucherResponseDTO;
import poly.edu.service.VoucherService;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép frontend gọi từ domain/port khác (nếu có)
public class VoucherRestController {

    private final VoucherService voucherService;

    @PostMapping("/apply")
    public ResponseEntity<VoucherResponseDTO> applyVoucher(@Valid @RequestBody VoucherApplyRequestDTO request) {
        VoucherResponseDTO response = voucherService.applyVoucher(request);
        return ResponseEntity.ok(response);
    }
}
