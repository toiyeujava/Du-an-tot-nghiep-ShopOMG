package poly.edu.controller.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.entity.Order;
import poly.edu.repository.OrderRepository;
import poly.edu.service.SseEmitterRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SePayWebhookController - Receives and validates payment webhooks from SePay.
 *
 * Webhook flow (SePay → our server):
 * 1. SePay POSTs to /api/webhook/sepay whenever a new transaction hits the MB Bank account.
 * 2. We validate the Bearer token against our sepay.api-key config property.
 * 3. We parse "content" to extract the orderId (format: "OMG-{id}").
 * 4. We compare transferAmount with order.finalAmount.
 * 5. On match  → update Order status, push SSE success event.
 *    On mismatch → push SSE error event (no DB change).
 * 6. Always return HTTP 200 so SePay does not retry.
 *
 * Security: endpoint is excluded from CSRF in SecurityConfig.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SePayWebhookController {

    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("OMG(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    @Value("${sepay.api-key}")
    private String sePayApiKey;

    private final OrderRepository orderRepository;
    private final SseEmitterRegistry sseEmitterRegistry;

    // ─── SePay Webhook Payload DTO ──────────────────────────────────────────────
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SePayWebhookPayload {
        @JsonProperty("gateway")         private String gateway;
        @JsonProperty("transactionDate") private String transactionDate;
        @JsonProperty("accountNumber")   private String accountNumber;
        @JsonProperty("code")            private String code;           // order code if auto-matched
        @JsonProperty("content")         private String content;        // transfer description
        @JsonProperty("transferType")    private String transferType;   // "in" or "out"
        @JsonProperty("transferAmount")  private Long   transferAmount;
        @JsonProperty("accumulated")     private Long   accumulated;
        @JsonProperty("subAccount")      private String subAccount;
        @JsonProperty("referenceCode")   private String referenceCode;  // bank reference number
        @JsonProperty("description")     private String description;
    }

    /**
     * POST /api/webhook/sepay
     * Called by SePay servers on every incoming transaction.
     * Must always return HTTP 200; otherwise SePay retries indefinitely.
     */
    @PostMapping("/api/webhook/sepay")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SePayWebhookPayload payload) {

        Map<String, Object> response = new HashMap<>();

        // ── 1. Validate Bearer token ────────────────────────────────────────────
        // Log the raw header so we can debug auth issues in console
        log.info("SePay webhook incoming. Authorization header: '{}'", authHeader);

        if (!isValidToken(authHeader)) {
            log.warn("SePay webhook REJECTED: invalid token. Expected key starting with '{}...'",
                    sePayApiKey != null && sePayApiKey.length() > 8 ? sePayApiKey.substring(0, 8) : "???");
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.ok(response); // still 200 to stop retry storms
        }

        log.info("SePay webhook AUTH OK. amount={} transferType='{}' content='{}' ref={}",
                payload.getTransferAmount(), payload.getTransferType(),
                payload.getContent(), payload.getReferenceCode());

        // ── 2. Only process incoming transfers ──────────────────────────────────
        if (!"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("Ignoring non-incoming transfer type: {}", payload.getTransferType());
            response.put("success", true);
            response.put("message", "Skipped (not an incoming transfer)");
            return ResponseEntity.ok(response);
        }

        // ── 3. Extract orderId from content ─────────────────────────────────────
        Integer orderId = extractOrderId(payload.getContent());
        if (orderId == null) {
            log.warn("No OMG-{{id}} pattern found in content: '{}'", payload.getContent());
            response.put("success", true);
            response.put("message", "No order code in content. Ignored.");
            return ResponseEntity.ok(response);
        }

        // ── 4. Load order ────────────────────────────────────────────────────────
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            log.warn("Order not found for orderId={}", orderId);
            // Push error to SSE in case the page is open
            sseEmitterRegistry.notify(orderId, "payment_error",
                    buildErrorPayload("Không tìm thấy đơn hàng OMG" + orderId + " trong hệ thống."));
            response.put("success", true);
            return ResponseEntity.ok(response);
        }
        Order order = orderOpt.get();

        // ── 5. Validate amount ───────────────────────────────────────────────────
        long expectedAmount = order.getFinalAmount() != null
                ? order.getFinalAmount().longValue() : 0L;

        long transferAmount = payload.getTransferAmount() != null ? payload.getTransferAmount() : 0L;
        
        // Accept exact match only
        if (transferAmount != expectedAmount) {
            log.warn("Amount mismatch for orderId={}: expected={} received={}",
                    orderId, expectedAmount, transferAmount);
            sseEmitterRegistry.notify(orderId, "payment_error",
                    buildErrorPayload("Sai số tiền (" + transferAmount + "đ / " + expectedAmount + "đ). " +
                            "Vui lòng nhắn tin với Shop để được hoàn tiền hoặc hỗ trợ xử lý đơn hàng."));
            response.put("success", true);
            return ResponseEntity.ok(response);
        }

        // ── 6. All correct → update order ────────────────────────────────────────
        order.setPaymentStatus("PAID");
        order.setStatus("PENDING");
        order.setPaymentConfirmedAt(LocalDateTime.now());
        order.setPaymentConfirmedBy("SePay-Webhook");
        orderRepository.save(order);
        log.info("Order {} marked PAID via SePay webhook. Ref={}", orderId, payload.getReferenceCode());

        // ── 7. Push success SSE event ────────────────────────────────────────────
        Map<String, Object> successData = new HashMap<>();
        successData.put("orderId",        orderId);
        successData.put("orderCode",      "OMG" + orderId);
        successData.put("amount",         payload.getTransferAmount());
        successData.put("content",        payload.getContent());
        successData.put("referenceCode",  payload.getReferenceCode() != null ? payload.getReferenceCode() : "");
        successData.put("transactionDate", payload.getTransactionDate() != null
                ? payload.getTransactionDate() : LocalDateTime.now().format(DISPLAY_FMT));
        sseEmitterRegistry.notify(orderId, "payment_success", successData);

        response.put("success", true);
        response.put("message", "Payment confirmed for order OMG" + orderId);
        return ResponseEntity.ok(response);
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private boolean isValidToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || sePayApiKey == null) return false;
        // Simply check if the header contains our secret key
        // This handles "Bearer <token>", "Apikey <token>", and our localtunnel bypass hack
        return authHeader.contains(sePayApiKey);
    }

    private Integer extractOrderId(String content) {
        if (content == null || content.isBlank()) return null;
        Matcher m = ORDER_CODE_PATTERN.matcher(content);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Map<String, Object> buildErrorPayload(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        return payload;
    }
}
