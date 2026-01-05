package poly.edu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import poly.edu.validation.StrongPassword;

@Data
public class SignUpForm {
    @NotBlank(message = "Vui lòng nhập Username")
    private String username;

    @NotBlank(message = "Vui lòng nhập Họ tên")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập Email")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Vui lòng nhập Mật khẩu")
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự")
    @StrongPassword
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    private String phone;
}