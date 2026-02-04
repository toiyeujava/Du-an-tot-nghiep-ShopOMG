package poly.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Entity
@Table(name = "ProductImages")
public class ProductImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url") // Khớp với image_url trong SQL
    private String image; // Field này là 'image' nên HTML dùng img.image

    @ManyToOne
    @JoinColumn(name = "product_id") // Khớp với product_id trong SQL
    private Product product;
}