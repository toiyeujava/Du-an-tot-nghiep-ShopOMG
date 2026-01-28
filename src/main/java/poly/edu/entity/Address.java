package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    // Audit fields (Commented out because columns do not exist in DB yet)
    /*
     * @Column(name = "created_at", updatable = false)
     * private LocalDateTime createdAt;
     * 
     * @Column(name = "updated_at")
     * private LocalDateTime updatedAt;
     */

    @PrePersist
    public void onCreate() {
        // createdAt = LocalDateTime.now();
        // updatedAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        // updatedAt = LocalDateTime.now();
    }
}
