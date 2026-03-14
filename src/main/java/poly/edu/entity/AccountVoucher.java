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
@Table(name = "Account_Voucher", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"account_id", "voucher_id"})
})
public class AccountVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Builder.Default
    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt = LocalDateTime.now();
}
