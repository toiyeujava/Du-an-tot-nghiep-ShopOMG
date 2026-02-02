package poly.edu.config; // Đổi package theo project của bạn

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Nơi tin nhắn được gửi về (Client đăng ký lắng nghe ở đây)
        config.enableSimpleBroker("/topic"); 
        // Tiền tố khi Client gửi tin lên Server
        config.setApplicationDestinationPrefixes("/app"); 
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối WebSocket
        registry.addEndpoint("/ws-chat").withSockJS(); 
    }
}