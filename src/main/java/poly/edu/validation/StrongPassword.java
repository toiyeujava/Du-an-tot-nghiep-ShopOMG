package poly.edu.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Documented
public @interface StrongPassword {

    String message() default "Mật khẩu không đủ mạnh. Yêu cầu: tối thiểu 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt (@#$%^&+=!)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
