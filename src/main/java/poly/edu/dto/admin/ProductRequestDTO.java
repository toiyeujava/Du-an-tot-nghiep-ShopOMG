package poly.edu.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import poly.edu.entity.Product;

@Data
public class ProductRequestDTO {

    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Length(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    private String slug;

    private String description;

    private String material;

    private String origin;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer categoryId;

    private String image;

    private String gender;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private Double price;

    @NotNull(message = "Giảm giá không được để trống")
    @Min(value = 0, message = "Giảm giá tối thiểu là 0%")
    private Integer discount;

    private Boolean isActive;

    public Product toEntity(Product existingProduct) {
        Product p = (existingProduct != null) ? existingProduct : new Product();
        p.setName(this.name);
        p.setSlug(this.slug);
        p.setDescription(this.description);
        p.setMaterial(this.material);
        p.setOrigin(this.origin);
        p.setCategoryId(this.categoryId);
        p.setImage(this.image);
        p.setGender(this.gender);
        p.setPrice(this.price);
        p.setDiscount(this.discount != null ? this.discount : 0);
        p.setIsActive(this.isActive != null ? this.isActive : false);
        return p;
    }
}
