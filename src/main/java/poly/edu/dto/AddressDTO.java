package poly.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Address responses
 * Used to return address data to clients without exposing entity internals
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Integer id;
    private String recipientName;
    private String phone;
    private String detailAddress;
    private String city;
    private String district;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Get full address as a single string
     * 
     * @return Formatted address
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s", detailAddress, district, city);
    }
}
