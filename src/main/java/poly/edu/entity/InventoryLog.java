package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "InventoryLogs", indexes = {
        @Index(name = "idx_invlog_timestamp", columnList = "timestamp"),
        @Index(name = "idx_invlog_variant", columnList = "variant_id")
})
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "type", nullable = false, length = 10)
    private String type; // "in", "out", "adj"

    @Column(name = "old_quantity", nullable = false)
    private Integer oldQuantity;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
