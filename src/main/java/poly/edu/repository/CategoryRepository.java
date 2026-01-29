package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.dto.CategoryCountDTO;
import poly.edu.entity.Category;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // --- QUERY ĐẾM ĐÃ NÂNG CẤP (GENDER + SALE + COLOR) ---
    @Query("SELECT c.id as id, c.name as name, " +
           // Logic đếm thông minh:
           // 1. Nếu không chọn màu (:color NULL) -> Đếm tất cả sản phẩm thỏa mãn Gender/Sale
           // 2. Nếu có chọn màu -> Chỉ đếm sản phẩm có biến thể (pv) khớp màu đó
           "COUNT(DISTINCT CASE WHEN (:color IS NULL OR :color = '' OR pv.id IS NOT NULL) THEN p.id ELSE NULL END) as count " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.id = p.categoryId " +
           "AND (:gender IS NULL OR :gender = '' OR p.gender = :gender) " +
           "AND (:sale IS NULL OR :sale = false OR p.discount > 0) " +
           // Join với bảng biến thể để check màu
           "LEFT JOIN p.variants pv " +
           "WITH (:color IS NULL OR :color = '' OR pv.color = :color) " +
           "GROUP BY c.id, c.name")
    List<CategoryCountDTO> getCategoryCounts(@Param("gender") String gender, 
                                             @Param("sale") Boolean sale,
                                             @Param("color") String color); // Thêm tham số color
}