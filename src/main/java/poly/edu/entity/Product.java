package poly.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString; // Import ToString

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Products")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "description")
    private String description;

    @Column(name = "material")
    private String material;

    @Column(name = "origin")
    private String origin;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "image")
    private String image;

    @Column(name = "gender")
    private String gender;

    @Column(name = "price")
    private Double price;

    @Column(name = "discount")
    private Integer discount = 0;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // --- SỬA ĐOẠN NÀY ---
    // 1. Thêm fetch = FetchType.EAGER để bắt buộc tải danh sách biến thể ngay lập tức
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    // 2. Thêm @ToString.Exclude để tránh lỗi tràn bộ nhớ khi in log
    @ToString.Exclude 
    private List<ProductVariant> variants;
}