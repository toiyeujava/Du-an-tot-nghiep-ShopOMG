package poly.edu.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import poly.edu.entity.ChatMessage;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 1. User gửi tin nhắn cho Admin
    @MessageMapping("/chat.sendToAdmin")
    public void sendToAdmin(@Payload ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            System.out.println("Lỗi: Người gửi chưa đăng nhập!");
            return;
        }

        String senderName = principal.getName();
        System.out.println("DEBUG: User [" + senderName + "] đang gửi tin: " + chatMessage.getContent());

        // Set thông tin chuẩn
        chatMessage.setSender(senderName);
        chatMessage.setRecipient("Admin");

        // --- CHIẾN THUẬT GỬI BAO VÂY (Gửi cho cả 2 khả năng) ---
        
        // Khả năng 1: Admin tên là "admin"
        messagingTemplate.convertAndSendToUser("admin", "/queue/messages", chatMessage);
        
        // Khả năng 2: Admin tên là email "admin@shopomg.com"
        messagingTemplate.convertAndSendToUser("admin@shopomg.com", "/queue/messages", chatMessage);
        
        System.out.println("DEBUG: Đã gửi tin nhắn đến hộp thư Admin.");
    }

    // 2. Admin trả lời lại cho User
    @MessageMapping("/chat.sendToUser")
    public void sendToUser(@Payload ChatMessage chatMessage, Principal principal) {
        if(principal != null) {
            System.out.println("DEBUG: Admin đang trả lời cho user: " + chatMessage.getRecipient());
            
            chatMessage.setSender("Admin"); 
            messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipient(), 
                "/queue/messages", 
                chatMessage
            );
        }
    }
}