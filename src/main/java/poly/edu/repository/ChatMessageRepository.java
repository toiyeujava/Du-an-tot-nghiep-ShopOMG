package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.ChatMessage;

import java.util.List;
import java.util.Set;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    // Lấy lịch sử chat của 1 user với Admin (cả 2 chiều)
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.sender = :user AND m.recipient = 'Admin') OR " +
           "(m.sender = 'Admin' AND m.recipient = :user) " +
           "ORDER BY m.sentAt ASC")
    List<ChatMessage> findHistory(@Param("user") String user);

    // Lấy danh sách user đã từng nhắn tin (không trùng)
    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.recipient = 'Admin'")
    Set<String> findActiveUsers();
}