package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.dto.AddressDTO;
import poly.edu.dto.AddressRequest;
import poly.edu.entity.Address;
import poly.edu.entity.Account;
import poly.edu.exception.AddressNotFoundException;
import poly.edu.exception.CannotDeleteDefaultAddressException;
import poly.edu.repository.AddressRepository;
import poly.edu.repository.AccountRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AddressService
 * Handles all business logic for address management
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AddressDTO createAddress(Integer accountId, AddressRequest request) {
        // Find the account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Check if this is the first address
        long addressCount = addressRepository.countByAccountId(accountId);

        // Create new address
        Address address = new Address();
        address.setAccount(account);
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setDetailAddress(request.getDetailAddress());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());

        // Logic: First address is automatically default
        if (addressCount == 0) {
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null && request.getIsDefault()) {
            // If user wants this as default, unset others
            addressRepository.unsetAllDefaultsByAccountId(accountId);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        Address savedAddress = addressRepository.save(address);
        return toDTO(savedAddress);
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(Integer addressId, Integer accountId, AddressRequest request) {
        // Find address with ownership check
        Address address = addressRepository.findByIdAndAccountId(addressId, accountId)
                .orElseThrow(
                        () -> new AddressNotFoundException("Không tìm thấy địa chỉ hoặc bạn không có quyền truy cập"));

        // Update fields
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setDetailAddress(request.getDetailAddress());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());

        // Handle default address change
        if (request.getIsDefault() != null && request.getIsDefault() && !address.getIsDefault()) {
            // User wants to set this as default
            addressRepository.unsetAllDefaultsByAccountId(accountId);
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null && !request.getIsDefault() && address.getIsDefault()) {
            // User wants to unset default - only allow if there are other addresses
            long count = addressRepository.countByAccountId(accountId);
            if (count > 1) {
                address.setIsDefault(false);
            }
            // If this is the only address, keep it as default
        }

        Address updatedAddress = addressRepository.save(address);
        return toDTO(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Integer addressId, Integer accountId) {
        // Find address with ownership check
        Address address = addressRepository.findByIdAndAccountId(addressId, accountId)
                .orElseThrow(
                        () -> new AddressNotFoundException("Không tìm thấy địa chỉ hoặc bạn không có quyền truy cập"));

        // Prevent deletion of default address
        if (address.getIsDefault()) {
            throw new CannotDeleteDefaultAddressException(
                    "Không thể xóa địa chỉ mặc định. Vui lòng chọn địa chỉ khác làm mặc định trước khi xóa.");
        }

        // Delete the address
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Integer addressId, Integer accountId) {
        // Find address with ownership check
        Address address = addressRepository.findByIdAndAccountId(addressId, accountId)
                .orElseThrow(
                        () -> new AddressNotFoundException("Không tìm thấy địa chỉ hoặc bạn không có quyền truy cập"));

        // If already default, do nothing
        if (address.getIsDefault()) {
            return;
        }

        // Unset all other defaults (CRITICAL: Must be done in transaction)
        addressRepository.unsetAllDefaultsByAccountId(accountId);

        // Set this address as default
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAllAddresses(Integer accountId) {
        List<Address> addresses = addressRepository.findByAccountId(accountId);

        // Sort: Default first, then by created date descending
        return addresses.stream()
                .sorted((a1, a2) -> {
                    if (a1.getIsDefault() && !a2.getIsDefault())
                        return -1;
                    if (!a1.getIsDefault() && a2.getIsDefault())
                        return 1;
                    // Both same default status, sort by ID descending (newest first)
                    return a2.getId().compareTo(a1.getId());
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Integer addressId, Integer accountId) {
        Address address = addressRepository.findByIdAndAccountId(addressId, accountId)
                .orElseThrow(
                        () -> new AddressNotFoundException("Không tìm thấy địa chỉ hoặc bạn không có quyền truy cập"));

        return toDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO getDefaultAddress(Integer accountId) {
        return addressRepository.findByAccountIdAndIsDefaultTrue(accountId)
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Convert Address entity to DTO
     * 
     * @param address Address entity
     * @return AddressDTO
     */
    private AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setRecipientName(address.getRecipientName());
        dto.setPhone(address.getPhone());
        dto.setDetailAddress(address.getDetailAddress());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setIsDefault(address.getIsDefault());
        // dto.setCreatedAt(address.getCreatedAt()); // Removed
        // dto.setUpdatedAt(address.getUpdatedAt()); // Removed
        return dto;
    }
}
