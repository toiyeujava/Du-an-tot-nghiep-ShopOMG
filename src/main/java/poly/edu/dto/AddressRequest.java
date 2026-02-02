package poly.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating addresses
 * Contains validation rules for input data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận không được vượt quá 100 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Số điện thoại không hợp lệ (phải là 10 số, bắt đầu bằng 03, 05, 07, 08, 09)")
    private String phone;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String detailAddress;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100, message = "Tên tỉnh/thành không được vượt quá 100 ký tự")
    private String city;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Tên quận/huyện không được vượt quá 100 ký tự")
    private String district;

    private Boolean isDefault = false;
}
