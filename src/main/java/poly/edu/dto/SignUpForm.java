package poly.edu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    private String phone;
}