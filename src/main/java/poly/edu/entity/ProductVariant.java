package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore; // 1. Import JsonIgnore
import lombok.ToString; // 2. Import ToString

import java.io.Serializable;

@Data
@Entity
@Table(name = "ProductVariants")
public class ProductVariant implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- SỬA ĐOẠN NÀY ---
    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore // <--- 3. QUAN TRỌNG: Cắt đứt vòng lặp khi chuyển sang JSON cho JavaScript
    @ToString.Exclude // <--- 4. Ngăn lỗi log
    private Product product;
    // --------------------

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "sku")
    private String sku;
}