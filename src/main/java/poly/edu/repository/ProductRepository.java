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
                        "AND (:sale IS NULL OR :sale = false OR p.discount > 0) " +
                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",

                        countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
                                        "LEFT JOIN p.variants pv " +
                                        "WHERE (:gender IS NULL OR :gender = '' OR p.gender = :gender) " +
                                        "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
                                        "AND (:color IS NULL OR :color = '' OR pv.color = :color) " +
                                        "AND (:sale IS NULL OR :sale = false OR p.discount > 0) " +
                                        "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                                        "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
        Page<Product> filterProducts(@Param("gender") String gender,
                        @Param("categoryId") Integer categoryId,
                        @Param("color") String color,
                        @Param("sale") Boolean sale,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        Pageable pageable);
}