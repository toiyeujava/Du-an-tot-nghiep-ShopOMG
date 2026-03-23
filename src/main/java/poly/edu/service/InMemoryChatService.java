package poly.edu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.entity.ChatMessage;
import poly.edu.repository.AccountRepository;
import poly.edu.repository.ChatMessageRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ChatMessageService - Thay thế InMemoryChatService
 * Lưu tin nhắn vào DB thay vì RAM → không mất khi restart server
 */
@Service
@RequiredArgsConstructor
public class InMemoryChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;

    // 1. Lưu tin nhắn vào DB
    public void saveMessage(String userKey, ChatMessage message) {
        chatMessageRepository.save(message);
    }

    // 2. Lấy lịch sử chat của 1 user
    public List<ChatMessage> getHistory(String userKey) {
        return chatMessageRepository.findHistory(userKey);
    }

    // 3. Lấy danh sách user đã nhắn tin + tất cả user trong DB
    public Set<String> getActiveUsers() {
        Set<String> result = new HashSet<>();

        // User đã từng chat
        result.addAll(chatMessageRepository.findActiveUsers());

        // Tất cả user trong DB (email)
        List<String> allUsers = accountRepository.findAllUsernames();
        if (allUsers != null) result.addAll(allUsers);

        return result;
    }
}