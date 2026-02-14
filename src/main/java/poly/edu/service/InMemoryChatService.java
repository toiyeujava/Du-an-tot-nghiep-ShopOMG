package poly.edu.service;

import org.springframework.stereotype.Service;
import poly.edu.entity.ChatMessage; // Sử dụng Entity của bạn

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryChatService {

    // Key: Tên User (khách hàng), Value: List tin nhắn của user đó với Admin
    private final Map<String, List<ChatMessage>> chatStore = new ConcurrentHashMap<>();

    // 1. Lưu tin nhắn
    public void saveMessage(String userKey, ChatMessage message) {
        // Nếu chưa có user này trong danh sách thì tạo mới list
        chatStore.computeIfAbsent(userKey, k -> new ArrayList<>()).add(message);
    }

    // 2. Lấy lịch sử chat của 1 user cụ thể (Cho cả User f5 và Admin click vào)
    public List<ChatMessage> getHistory(String userKey) {
        return chatStore.getOrDefault(userKey, new ArrayList<>());
    }

    // 3. Lấy danh sách những user đã nhắn tin (Cho Admin hiển thị list bên trái)
    public Set<String> getActiveUsers() {
        return chatStore.keySet();
    }
}