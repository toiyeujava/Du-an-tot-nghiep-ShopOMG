package poly.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import poly.edu.validation.StrongPassword;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordForm {

    private String token;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự")
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;
}
