package poly.edu.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SseEmitterRegistry - Manages one SSE emitter per payment order.
 *
 * CRITICAL FIX: data is serialized to JSON string via Jackson before sending.
 * SseEmitter.event().data(Map) calls Map.toString() which produces {key=value}
 * format — NOT valid JSON. Frontend JSON.parse() would silently fail → no popup.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    private static final long TIMEOUT_MS = 10 * 60 * 1_000L; // 10 minutes

    private final Map<Integer, SseEmitter> registry = new ConcurrentHashMap<>();


    /** Creates + stores an emitter for the given orderId. */
    public SseEmitter register(Integer orderId) {
        SseEmitter existing = registry.remove(orderId);
        if (existing != null) {
            try { existing.complete(); } catch (Exception ignored) {}
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        registry.put(orderId, emitter);

        emitter.onTimeout(()    -> { log.info("SSE timeout for orderId={}", orderId); registry.remove(orderId); });
        emitter.onError(e       -> registry.remove(orderId));
        emitter.onCompletion(() -> registry.remove(orderId));

        log.info("SSE emitter registered for orderId={}", orderId);
        return emitter;
    }

    /**
     * Converts a simple Map to a JSON string.
     * Bypasses ObjectMapper to avoid IDE/Classpath missing dependency errors.
     */
    private String toJson(Object obj) {
        if (!(obj instanceof Map)) return "{}";
        
        Map<?, ?> map = (Map<?, ?>) obj;
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object val = entry.getValue();
            if (val instanceof Number || val instanceof Boolean) {
                sb.append(val);
            } else {
                sb.append("\"").append(val.toString().replace("\"", "\\\"")).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Push a named SSE event with proper JSON-serialized data, then close emitter.
     */
    public void notify(Integer orderId, String eventName, Object data) {
        SseEmitter emitter = registry.remove(orderId);
        if (emitter == null) {
            log.warn("No SSE emitter found for orderId={}. Webhook arrived but no browser tab listening.", orderId);
            return;
        }
        try {
            // Serialize manually to avoid Map.toString() bug & ObjectMapper crashes
            String jsonData = toJson(data);
            log.info("SSE push event='{}' orderId={} data={}", eventName, orderId, jsonData);

            emitter.send(
                SseEmitter.event()
                    .name(eventName)
                    .data(jsonData) // Do not supply MediaType, otherwise Spring double-serializes the String
            );
            emitter.complete();
        } catch (IOException e) {
            log.warn("SSE send failed for orderId={}: {}", orderId, e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
