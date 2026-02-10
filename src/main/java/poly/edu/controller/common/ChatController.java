package poly.edu.controller.common;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import poly.edu.entity.ChatMessage;

/**
 * ChatController - WebSocket endpoint for real-time chat.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why is this in common/?"
 * Chat is accessible to all authenticated users, regardless of role.
 * It uses STOMP protocol over WebSocket, not HTTP - so it's fundamentally
 * different from other controllers.
 */
@Controller
public class ChatController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        return message;
    }
}
