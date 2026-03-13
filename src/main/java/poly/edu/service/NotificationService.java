package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import poly.edu.entity.Order;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * NotificationService - Gửi thông báo real-time qua WebSocket (Bonus-1)
 * Khi khách đặt đơn mới → push notification đến tất cả SALES đang online.
 *
 * Fix Spring Boot 4.x: không dùng @RequiredArgsConstructor + final cho
 * SimpMessagingTemplate vì bean này được tạo sau ApplicationContext,
 * dẫn đến lỗi inject lúc startup. Dùng @Autowired(required=false) thay thế.
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo đơn hàng mới đến tất cả nhân viên bán hàng
     * Topic: /topic/sales/new-order
     */
    public void notifyNewOrder(Order order) {
        if (messagingTemplate == null) {
            log.warn("[Bonus-1] SimpMessagingTemplate chưa sẵn sàng, bỏ qua notification đơn #{}",
                    order.getId());
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type",         "NEW_ORDER");
            payload.put("orderId",      order.getId());
            payload.put("customerName", order.getReceiverName() != null
                    ? order.getReceiverName() : "Khách vãng lai");
            payload.put("amount",       order.getFinalAmount() != null
                    ? order.getFinalAmount().longValue() : 0);
            payload.put("time",         order.getOrderDate() != null
                    ? order.getOrderDate().format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))
                    : "vừa xong");
            payload.put("message",      "Đơn hàng mới từ " + payload.get("customerName"));

            messagingTemplate.convertAndSend("/topic/sales/new-order", (Object) payload);
            log.info("[Bonus-1] Đã push notification đơn mới #{} → /topic/sales/new-order",
                    order.getId());
        } catch (Exception e) {
            log.error("[Bonus-1] Lỗi push notification: {}", e.getMessage());
        }
    }
}