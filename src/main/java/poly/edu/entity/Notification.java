package poly.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountId")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId")
    private Order order;

    @Column(name = "Type", length = 50)
    private String type; // e.g. ORDER_PLACED, ORDER_SHIPPING

    @Column(name = "Title", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "Link", length = 255)
    private String link;

    @Column(name = "ImageUrls", columnDefinition = "NVARCHAR(MAX)")
    private String imageUrls;

    @Column(name = "IsRead")
    private boolean isRead = false;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();
}
