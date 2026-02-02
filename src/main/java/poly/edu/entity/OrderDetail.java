package poly.edu.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 18, scale = 2)
    private BigDecimal total;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (price != null && quantity != null) {
            total = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
