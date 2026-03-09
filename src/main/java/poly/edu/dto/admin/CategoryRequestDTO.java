package poly.edu.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import poly.edu.entity.Category;

@Data
public class CategoryRequestDTO {

    private Integer id;

    @NotBlank(message = "Tên loại không được để trống")
    @Length(max = 100, message = "Tên loại tối đa 100 ký tự")
    private String name;

    private String image;

    private Boolean isActive;

    public Category toEntity(Category existingCategory) {
        Category c = (existingCategory != null) ? existingCategory : new Category();
        c.setName(this.name);
        c.setImage(this.image);
        if (this.isActive != null) {
            c.setIsActive(this.isActive);
        }
        return c;
    }
}
