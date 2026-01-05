package poly.edu.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordStrengthValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // Không cần khởi tạo gì
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null hoặc empty sẽ được xử lý bởi @NotBlank
        if (password == null || password.isBlank()) {
            return false;
        }

        // Kiểm tra các yêu cầu
        boolean hasMinLength = password.length() >= 8;
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[@#$%^&+=!].*");

        // Tất cả phải đạt
        return hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecialChar;
    }
}
