package poly.edu.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import poly.edu.entity.ChatMessage;

@Controller
public class ChatController {

    // Khi client gửi tới /app/sendMessage
    @MessageMapping("/sendMessage")
    // Server sẽ đẩy tin nhắn về tất cả ai đang lắng nghe ở /topic/public
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        return message;
    }
}