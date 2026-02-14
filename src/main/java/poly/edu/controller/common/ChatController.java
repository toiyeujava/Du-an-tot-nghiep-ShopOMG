package poly.edu.controller.common;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.edu.entity.ChatMessage;
import poly.edu.repository.AccountRepository;
import poly.edu.service.InMemoryChatService;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private InMemoryChatService chatService; 
    
    @Autowired
    private AccountRepository accountRepository;

    // --- PHẦN WEBSOCKET (REAL-TIME) ---

    // 1. User gửi tin cho Admin
    @MessageMapping("/chat.sendToAdmin")
    public void sendToAdmin(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            // Lấy tên người gửi (nếu login thì lấy Principal, không thì lấy từ tin nhắn)
            String senderName = (principal != null) ? principal.getName() : chatMessage.getSender();
            
            chatMessage.setSender(senderName);
            chatMessage.setRecipient("Admin");

            // Lưu vào RAM (để F5 còn thấy)
            if (chatService != null) {
                chatService.saveMessage(senderName, chatMessage);
            }

            // --- QUAN TRỌNG: Gửi về đúng email Admin đang đăng nhập ---
            // Sửa "admin" thành "admin@shopomg.com" (hoặc email bạn dùng đăng nhập)
            messagingTemplate.convertAndSendToUser("admin@shopomg.com", "/queue/messages", chatMessage);
            
        } catch (Exception e) {
            System.err.println("Lỗi gửi tin cho Admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 2. Admin trả lời User
    @MessageMapping("/chat.sendToUser")
    public void sendToUser(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            chatMessage.setSender("Admin");
            String userKey = chatMessage.getRecipient(); 

            System.out.println("Admin đang gửi cho: " + userKey);

            // Lưu vào RAM
            if (chatService != null) {
                chatService.saveMessage(userKey, chatMessage);
            }

            // Gửi cho User đích danh
            messagingTemplate.convertAndSendToUser(userKey, "/queue/messages", chatMessage);
            
        } catch (Exception e) {
            // Bắt lỗi để không bị ngắt kết nối Socket
            System.err.println("Lỗi Admin gửi tin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // --- PHẦN REST API ---
    
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@RequestParam("user") String user) {
        return chatService.getHistory(user);
    }
    
    @GetMapping("/api/chat/users")
    @ResponseBody
    public Set<String> getActiveUsers() {
        // Lấy user từ DB
        List<String> allUsers = accountRepository.findAllUsernames();
        // Lấy user đang chat trong RAM
        Set<String> activeChatters = chatService.getActiveUsers();
        
        // Gộp lại
        Set<String> result = new HashSet<>();
        if (allUsers != null) result.addAll(allUsers);
        if (activeChatters != null) result.addAll(activeChatters);
        
        return result;
    }
}