package poly.edu.service;

import poly.edu.dto.AddressDTO;
import poly.edu.dto.AddressRequest;

import java.util.List;

/**
 * Service interface for Address management
 */
public interface AddressService {

    /**
     * Create a new address for a user
     * Auto-sets as default if it's the first address
     * 
     * @param accountId The account ID
     * @param request   Address data
     * @return Created address DTO
     */
    AddressDTO createAddress(Integer accountId, AddressRequest request);

    /**
     * Update an existing address
     * 
     * @param addressId Address ID to update
     * @param accountId Account ID (for ownership check)
     * @param request   Updated address data
     * @return Updated address DTO
     */
    AddressDTO updateAddress(Integer addressId, Integer accountId, AddressRequest request);

    /**
     * Delete an address
     * Prevents deletion of default address
     * 
     * @param addressId Address ID to delete
     * @param accountId Account ID (for ownership check)
     */
    void deleteAddress(Integer addressId, Integer accountId);

    /**
     * Set an address as default
     * Automatically unsets other default addresses
     * 
     * @param addressId Address ID to set as default
     * @param accountId Account ID (for ownership check)
     */
    void setDefaultAddress(Integer addressId, Integer accountId);

    /**
     * Get all addresses of a user
     * 
     * @param accountId The account ID
     * @return List of address DTOs (default first, then by created date)
     */
    List<AddressDTO> getAllAddresses(Integer accountId);

    /**
     * Get a single address by ID
     * 
     * @param addressId Address ID
     * @param accountId Account ID (for ownership check)
     * @return Address DTO
     */
    AddressDTO getAddressById(Integer addressId, Integer accountId);

    /**
     * Get the default address of a user
     * 
     * @param accountId The account ID
     * @return Default address DTO or null if none
     */
    AddressDTO getDefaultAddress(Integer accountId);
}
