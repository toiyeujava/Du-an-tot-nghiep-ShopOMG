package poly.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileForm {
    
    private String username; // Chỉ hiển thị, không cho sửa

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String email; // Chỉ để hiển thị (readonly)
    private String avatarUrl; // Để hiển thị ảnh cũ

 // --- BỔ SUNG THÊM 2 TRƯỜNG NÀY ---
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String gender; // MALE, FEMALE, OTHER
}