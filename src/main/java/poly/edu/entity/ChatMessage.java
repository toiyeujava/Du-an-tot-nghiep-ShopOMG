package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ChatMessages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;
    
    @Column(name = "media_url")
    private String mediaUrl;   // URL ảnh/video đã upload
 
    @Column(name = "media_type")
    private String mediaType;  // "image" hoặc "video"

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) sentAt = LocalDateTime.now();
    }

    // Constructor tiện dụng cho WebSocket (không cần id + sentAt)
    public ChatMessage(String sender, String recipient, String content) {
        this.sender    = sender;
        this.recipient = recipient;
        this.content   = content;
    }
}