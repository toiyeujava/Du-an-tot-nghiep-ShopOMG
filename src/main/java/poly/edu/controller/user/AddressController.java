package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.dto.AddressDTO;
import poly.edu.dto.AddressRequest;
import poly.edu.entity.Account;
import poly.edu.exception.AddressNotFoundException;
import poly.edu.exception.CannotDeleteDefaultAddressException;
import poly.edu.exception.UnauthorizedAccessException;
import poly.edu.repository.AccountRepository;
import poly.edu.service.AddressService;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AddressController - REST API for user address management.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why is this a @RestController instead of @Controller?"
 *
 * Address management is called via JavaScript (AJAX) from the
 * account-addresses.html page. It returns JSON data, not HTML views.
 * This is different from other controllers that return Thymeleaf templates.
 *
 * "Why keep local @ExceptionHandlers instead of using GlobalExceptionHandler?"
 * - This is a REST API - it returns JSON error responses
 * - The GlobalExceptionHandler returns HTML error pages
 * - Mixing JSON and HTML error responses would break the frontend
 * - When the app adds more REST APIs, we could create a separate
 * 
 * @RestControllerAdvice for JSON-only error handling
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAddresses(Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        List<AddressDTO> addresses = addressService.getAllAddresses(accountId);
        return ResponseEntity.ok(createResponse(true, "Lấy danh sách địa chỉ thành công", addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAddressById(@PathVariable Integer id, Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO address = addressService.getAddressById(id, accountId);
        return ResponseEntity.ok(createResponse(true, "Lấy thông tin địa chỉ thành công", address));
    }

    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> getDefaultAddress(Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO address = addressService.getDefaultAddress(accountId);
        if (address == null) {
            return ResponseEntity.ok(createResponse(true, "Chưa có địa chỉ mặc định", null));
        }
        return ResponseEntity.ok(createResponse(true, "Lấy địa chỉ mặc định thành công", address));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAddress(
            @Valid @RequestBody AddressRequest request, Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO createdAddress = addressService.createAddress(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createResponse(true, "Thêm địa chỉ thành công", createdAddress));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable Integer id,
            @Valid @RequestBody AddressRequest request, Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO updatedAddress = addressService.updateAddress(id, accountId, request);
        return ResponseEntity.ok(createResponse(true, "Cập nhật địa chỉ thành công", updatedAddress));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @PathVariable Integer id, Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        addressService.deleteAddress(id, accountId);
        return ResponseEntity.ok(createResponse(true, "Xóa địa chỉ thành công", null));
    }

    @PatchMapping("/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(
            @PathVariable Integer id, Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        addressService.setDefaultAddress(id, accountId);
        return ResponseEntity.ok(createResponse(true, "Đặt địa chỉ mặc định thành công", null));
    }

    // ===== LOCAL EXCEPTION HANDLERS (REST-only, returns JSON) =====

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAddressNotFound(AddressNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(CannotDeleteDefaultAddressException.class)
    public ResponseEntity<Map<String, Object>> handleCannotDeleteDefault(CannotDeleteDefaultAddressException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponse(false, "Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponse(false, "Đã xảy ra lỗi: " + ex.getMessage(), null));
    }

    // ===== PRIVATE HELPERS =====

    private Integer getAccountIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedAccessException("Bạn cần đăng nhập để thực hiện thao tác này");
        }
        String identifier = principal.getName();
        Account account = accountRepository.findByUsername(identifier)
                .orElseGet(() -> accountRepository.findByEmail(identifier)
                        .orElseThrow(() -> new UnauthorizedAccessException("Không tìm thấy tài khoản")));
        return account.getId();
    }

    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
