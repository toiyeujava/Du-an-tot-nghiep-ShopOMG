package poly.edu.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import poly.edu.service.SseEmitterRegistry;

/**
 * SseController - Opens a Server-Sent Events stream for the QR payment page.
 *
 * The client (qr-payment.html) calls GET /api/payment-events/{orderId} once
 * after the page loads.  Spring keeps the response open as a text/event-stream
 * until either:
 *   a) The webhook fires and SseEmitterRegistry pushes a payment_success or
 *      payment_error event (normal case).
 *   b) The 6-minute timeout expires (user didn't pay in time).
 *   c) The user navigates away and the browser closes the SSE connection.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * GET /api/payment-events/{orderId}
     *
     * Requirements:
     * - User must be authenticated (Spring Security enforces this via SecurityConfig).
     * - Returns SseEmitter — Spring serialises the response headers automatically.
     */
    @GetMapping(value = "/api/payment-events/{orderId}", produces = "text/event-stream")
    public SseEmitter paymentEvents(@PathVariable Integer orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Return a completed (immediately closed) emitter for anonymous users
            SseEmitter rejected = new SseEmitter(0L);
            rejected.complete();
            return rejected;
        }

        log.info("SSE connection opened for orderId={} by user={}", orderId, auth.getName());
        return sseEmitterRegistry.register(orderId);
    }
}
