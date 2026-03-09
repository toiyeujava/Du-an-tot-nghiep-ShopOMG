package poly.edu.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import poly.edu.entity.ProductVariant;

@Data
public class ProductVariantRequestDTO {

    private Integer id;

    @NotBlank(message = "Màu sắc không được để trống")
    @Length(max = 50, message = "Màu sắc tối đa 50 ký tự")
    private String color;

    @NotBlank(message = "Kích thước không được để trống")
    @Length(max = 50, message = "Kích thước tối đa 50 ký tự")
    private String size;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    @Length(max = 100, message = "SKU tối đa 100 ký tự")
    private String sku;

    public ProductVariant toEntity(ProductVariant existingVariant) {
        ProductVariant v = (existingVariant != null) ? existingVariant : new ProductVariant();
        v.setColor(this.color);
        v.setSize(this.size);
        v.setQuantity(this.quantity);
        if (this.sku != null && !this.sku.trim().isEmpty()) {
            v.setSku(this.sku);
        }
        return v;
    }
}
