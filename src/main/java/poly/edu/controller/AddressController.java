package poly.edu.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Address Management
 * Provides CRUD operations for user addresses
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Get all addresses of current user
     * 
     * @param principal Authenticated user
     * @return List of addresses
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAddresses(Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        List<AddressDTO> addresses = addressService.getAllAddresses(accountId);

        return ResponseEntity.ok(createResponse(true, "Lấy danh sách địa chỉ thành công", addresses));
    }

    /**
     * Get single address by ID
     * 
     * @param id        Address ID
     * @param principal Authenticated user
     * @return Address details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAddressById(@PathVariable Integer id, Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO address = addressService.getAddressById(id, accountId);

        return ResponseEntity.ok(createResponse(true, "Lấy thông tin địa chỉ thành công", address));
    }

    /**
     * Get default address of current user
     * 
     * @param principal Authenticated user
     * @return Default address or null
     */
    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> getDefaultAddress(Principal principal) {
        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO address = addressService.getDefaultAddress(accountId);

        if (address == null) {
            return ResponseEntity.ok(createResponse(true, "Chưa có địa chỉ mặc định", null));
        }

        return ResponseEntity.ok(createResponse(true, "Lấy địa chỉ mặc định thành công", address));
    }

    /**
     * Create new address
     * 
     * @param request   Address data
     * @param principal Authenticated user
     * @return Created address
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAddress(
            @Valid @RequestBody AddressRequest request,
            Principal principal) {

        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO createdAddress = addressService.createAddress(accountId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createResponse(true, "Thêm địa chỉ thành công", createdAddress));
    }

    /**
     * Update existing address
     * 
     * @param id        Address ID
     * @param request   Updated address data
     * @param principal Authenticated user
     * @return Updated address
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable Integer id,
            @Valid @RequestBody AddressRequest request,
            Principal principal) {

        Integer accountId = getAccountIdFromPrincipal(principal);
        AddressDTO updatedAddress = addressService.updateAddress(id, accountId, request);

        return ResponseEntity.ok(createResponse(true, "Cập nhật địa chỉ thành công", updatedAddress));
    }

    /**
     * Delete address
     * 
     * @param id        Address ID
     * @param principal Authenticated user
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @PathVariable Integer id,
            Principal principal) {

        Integer accountId = getAccountIdFromPrincipal(principal);
        addressService.deleteAddress(id, accountId);

        return ResponseEntity.ok(createResponse(true, "Xóa địa chỉ thành công", null));
    }

    /**
     * Set address as default
     * 
     * @param id        Address ID
     * @param principal Authenticated user
     * @return Success message
     */
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(
            @PathVariable Integer id,
            Principal principal) {

        Integer accountId = getAccountIdFromPrincipal(principal);
        addressService.setDefaultAddress(id, accountId);

        return ResponseEntity.ok(createResponse(true, "Đặt địa chỉ mặc định thành công", null));
    }

    /**
     * Exception handler for AddressNotFoundException
     */
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAddressNotFound(AddressNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(createResponse(false, ex.getMessage(), null));
    }

    /**
     * Exception handler for UnauthorizedAccessException
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(createResponse(false, ex.getMessage(), null));
    }

    /**
     * Exception handler for CannotDeleteDefaultAddressException
     */
    @ExceptionHandler(CannotDeleteDefaultAddressException.class)
    public ResponseEntity<Map<String, Object>> handleCannotDeleteDefault(CannotDeleteDefaultAddressException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponse(false, ex.getMessage(), null));
    }

    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponse(false, "Dữ liệu không hợp lệ", errors));
    }

    /**
     * Generic exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponse(false, "Đã xảy ra lỗi: " + ex.getMessage(), null));
    }

    /**
     * Helper method to get account ID from authenticated principal
     * Supports both form login and OAuth2
     */
    private Integer getAccountIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedAccessException("Bạn cần đăng nhập để thực hiện thao tác này");
        }

        String identifier = principal.getName();

        // Try to find by username first
        Account account = accountRepository.findByUsername(identifier)
                .orElseGet(() -> accountRepository.findByEmail(identifier)
                        .orElseThrow(() -> new UnauthorizedAccessException("Không tìm thấy tài khoản")));

        return account.getId();
    }

    /**
     * Helper method to create standardized API response
     */
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
