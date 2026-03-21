package poly.edu.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import poly.edu.entity.Order;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.repository.NotificationRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.entity.Notification;
import poly.edu.entity.OrderDetail;

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

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

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

    /**
     * Gửi thông báo khi đơn hàng được đặt thành công (COD) hoặc khi thanh toán QR thành công.
     * Dành cho Customer.
     */
    @Transactional
    public void sendOrderPlacedNotification(Order order) {
        if (order.getAccount() == null) return; // Ignore guest orders
        
        Notification notification = new Notification();
        notification.setAccount(order.getAccount());
        notification.setOrder(order);
        notification.setType("ORDER_PLACED");
        notification.setTitle("Xác nhận đơn hàng");
        
        // Fetch order details explicitly to avoid LazyInitializationException or empty unattached lists
        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        if (details != null && !details.isEmpty()) {
            String urls = details.stream()
                    .filter(d -> d.getProductVariant() != null && d.getProductVariant().getProduct() != null && d.getProductVariant().getProduct().getImage() != null)
                    .map(d -> d.getProductVariant().getProduct().getImage())
                    .collect(java.util.stream.Collectors.joining(","));
            notification.setImageUrls(urls);
        }

        // Build generic content as per Tiktok shop
        String content = "Cảm ơn bạn đã đặt hàng! Hãy kiểm tra lại thông tin đơn hàng và địa chỉ giao hàng của bạn.";
        notification.setContent(content);
        notification.setLink("/account/orders"); // Directs user to their order list
        
        notificationRepository.save(notification);
    }

    /**
     * Gửi thông báo khi đơn hàng bắt đầu giao.
     */
    @Transactional
    public void sendOrderShippedNotification(Order order) {
        if (order.getAccount() == null) return;
        
        Notification notification = new Notification();
        notification.setAccount(order.getAccount());
        notification.setOrder(order);
        notification.setType("ORDER_SHIPPING");
        notification.setTitle("Đơn hàng đang giao");
        
        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        if (details != null && !details.isEmpty()) {
            String urls = details.stream()
                    .filter(d -> d.getProductVariant() != null && d.getProductVariant().getProduct() != null && d.getProductVariant().getProduct().getImage() != null)
                    .map(d -> d.getProductVariant().getProduct().getImage())
                    .collect(java.util.stream.Collectors.joining(","));
            notification.setImageUrls(urls);
        }

        String content = "Kiện hàng của bạn đang được giao đến! Đừng ngại liên hệ với chúng tôi nếu bạn có bất kỳ thắc mắc nào.";
        notification.setContent(content);
        notification.setLink("/account/orders");
        
        notificationRepository.save(notification);
    }

    public List<Notification> getRecentNotifications(Integer accountId, int limit) {
        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, limit));
    }

    public Long getUnreadCount(Integer accountId) {
        return notificationRepository.countByAccountIdAndIsReadFalse(accountId);
    }

    @Transactional
    public void markAsRead(Integer notificationId, Integer accountId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getAccount() != null && notification.getAccount().getId().equals(accountId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Integer accountId) {
        List<Notification> unread = notificationRepository.findByAccountIdAndIsReadFalse(accountId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}