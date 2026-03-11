package poly.edu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AsyncConfig - Bật tính năng @Async cho Spring Boot
 *
 * Cần thiết để EmailService.sendOrderStatusEmail() và
 * EmailService.sendOrderCancelledEmail() chạy bất đồng bộ,
 * không làm chậm response trả về cho nhân viên bán hàng.
 *
 * Chỉ cần thêm file này vào package poly.edu.config
 * (cùng chỗ với các file config khác trong dự án)
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Không cần thêm gì - @EnableAsync là đủ để kích hoạt @Async
}