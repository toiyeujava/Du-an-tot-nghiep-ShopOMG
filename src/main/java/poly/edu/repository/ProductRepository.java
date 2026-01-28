package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    List<Product> findByGender(String gender);

    // --- QUERY CẬP NHẬT (THÊM LOGIC SALE) ---
    @Query(value = "SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.variants pv " + 
           "WHERE (:gender IS NULL OR :gender = '' OR p.gender = :gender) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND (:color IS NULL OR :color = '' OR pv.color = :color) " +
           // Logic Sale: Nếu sale=true thì bắt buộc discount > 0. Nếu sale=null hoặc false thì bỏ qua.
           "AND (:sale IS NULL OR :sale = false OR p.discount > 0)", 
           
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "LEFT JOIN p.variants pv " +
                        "WHERE (:gender IS NULL OR :gender = '' OR p.gender = :gender) " +
                        "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
                        "AND (:color IS NULL OR :color = '' OR pv.color = :color) " +
                        "AND (:sale IS NULL OR :sale = false OR p.discount > 0)")
    Page<Product> filterProducts(@Param("gender") String gender, 
                                 @Param("categoryId") Integer categoryId, 
                                 @Param("color") String color,
                                 @Param("sale") Boolean sale, // <--- Thêm tham số này
                                 Pageable pageable);
}