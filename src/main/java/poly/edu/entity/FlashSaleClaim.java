package poly.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "FlashSaleClaims", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "voucher_id"})
})
public class FlashSaleClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "voucher_id", nullable = false)
    private Integer voucherId;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;
}
