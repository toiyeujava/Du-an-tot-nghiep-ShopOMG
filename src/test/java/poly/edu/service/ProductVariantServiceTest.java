package poly.edu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.ProductVariantRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProductVariantServiceTest - Unit tests for variant service.
 * 
 * Test Coverage:
 * - CRUD operations
 * - SKU generation and uniqueness
 * - Stock calculation
 * - Constraint validation
 */
@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductVariantService variantService;

    private Product testProduct;
    private ProductVariant testVariant;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("Basic T-Shirt");

        testVariant = new ProductVariant();
        testVariant.setId(1);
        testVariant.setProduct(testProduct);
        testVariant.setSize("M");
        testVariant.setColor("Red");
        testVariant.setQuantity(10);
        testVariant.setSku("BAS-M-RED");
    }

    // ==================== getVariantsByProduct ====================

    @Nested
    @DisplayName("getVariantsByProduct")
    class GetVariantsByProduct {

        @Test
        @DisplayName("Should return all variants for a product")
        void getVariantsByProduct_existingProduct_returnsVariants() {
            // Arrange
            ProductVariant variant2 = new ProductVariant();
            variant2.setId(2);
            variant2.setSize("L");
            variant2.setColor("Blue");
            variant2.setQuantity(15);

            when(variantRepository.findByProductId(1))
                    .thenReturn(Arrays.asList(testVariant, variant2));

            // Act
            List<ProductVariant> result = variantService.getVariantsByProduct(1);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).extracting("size").containsExactly("M", "L");
        }

        @Test
        @DisplayName("Should return empty list when no variants")
        void getVariantsByProduct_noVariants_returnsEmptyList() {
            // Arrange
            when(variantRepository.findByProductId(999)).thenReturn(List.of());

            // Act
            List<ProductVariant> result = variantService.getVariantsByProduct(999);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ==================== createVariant ====================

    @Nested
    @DisplayName("createVariant")
    class CreateVariant {

        @Test
        @DisplayName("Should create variant with generated SKU")
        void createVariant_withoutSku_generatesSku() {
            // Arrange
            ProductVariant newVariant = new ProductVariant();
            newVariant.setSize("L");
            newVariant.setColor("Blue");
            newVariant.setQuantity(20);

            when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
            when(variantRepository.existsByProductIdAndSizeAndColor(1, "L", "Blue"))
                    .thenReturn(false);
            when(variantRepository.save(any(ProductVariant.class)))
                    .thenAnswer(invocation -> {
                        ProductVariant saved = invocation.getArgument(0);
                        saved.setId(2);
                        return saved;
                    });

            // Act
            ProductVariant result = variantService.createVariant(1, newVariant);

            // Assert
            assertThat(result.getId()).isEqualTo(2);
            assertThat(result.getSku()).isNotNull();
            assertThat(result.getSku()).contains("BAS"); // Product name initials
        }

        @Test
        @DisplayName("Should throw when product not found")
        void createVariant_productNotFound_throws() {
            // Arrange
            when(productRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> variantService.createVariant(999, testVariant))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should throw when size/color combination exists")
        void createVariant_duplicateSizeColor_throws() {
            // Arrange
            when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
            when(variantRepository.existsByProductIdAndSizeAndColor(1, "M", "Red"))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> variantService.createVariant(1, testVariant))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should use provided SKU if given")
        void createVariant_withSku_usesProvidedSku() {
            // Arrange
            ProductVariant newVariant = new ProductVariant();
            newVariant.setSize("XL");
            newVariant.setColor("Black");
            newVariant.setQuantity(5);
            newVariant.setSku("CUSTOM-SKU-001");

            when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
            when(variantRepository.existsByProductIdAndSizeAndColor(1, "XL", "Black"))
                    .thenReturn(false);
            when(variantRepository.save(any(ProductVariant.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ProductVariant result = variantService.createVariant(1, newVariant);

            // Assert
            assertThat(result.getSku()).isEqualTo("CUSTOM-SKU-001");
        }

        @Test
        @DisplayName("Should set default quantity to 0 if null")
        void createVariant_nullQuantity_setsToZero() {
            // Arrange
            ProductVariant newVariant = new ProductVariant();
            newVariant.setSize("S");
            newVariant.setColor("White");
            newVariant.setQuantity(null);

            when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
            when(variantRepository.existsByProductIdAndSizeAndColor(1, "S", "White"))
                    .thenReturn(false);
            when(variantRepository.save(any(ProductVariant.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ProductVariant result = variantService.createVariant(1, newVariant);

            // Assert
            assertThat(result.getQuantity()).isEqualTo(0);
        }
    }

    // ==================== updateVariant ====================

    @Nested
    @DisplayName("updateVariant")
    class UpdateVariant {

        @Test
        @DisplayName("Should update variant fields")
        void updateVariant_validData_updatesFields() {
            // Arrange
            ProductVariant updates = new ProductVariant();
            updates.setSize("L");
            updates.setQuantity(25);

            when(variantRepository.findById(1)).thenReturn(Optional.of(testVariant));
            when(variantRepository.save(any(ProductVariant.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ProductVariant result = variantService.updateVariant(1, updates);

            // Assert
            assertThat(result.getSize()).isEqualTo("L");
            assertThat(result.getQuantity()).isEqualTo(25);
            assertThat(result.getColor()).isEqualTo("Red"); // Unchanged
        }

        @Test
        @DisplayName("Should throw when variant not found")
        void updateVariant_variantNotFound_throws() {
            // Arrange
            when(variantRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> variantService.updateVariant(999, testVariant))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Variant not found");
        }
    }

    // ==================== deleteVariant ====================

    @Nested
    @DisplayName("deleteVariant")
    class DeleteVariant {

        @Test
        @DisplayName("Should delete variant with no active orders")
        void deleteVariant_noActiveOrders_success() {
            // Arrange
            when(variantRepository.findById(1)).thenReturn(Optional.of(testVariant));
            when(variantRepository.hasActiveOrders(1)).thenReturn(false);

            // Act
            variantService.deleteVariant(1);

            // Assert
            verify(variantRepository).delete(testVariant);
        }

        @Test
        @DisplayName("Should throw when variant has active orders")
        void deleteVariant_hasActiveOrders_throws() {
            // Arrange
            when(variantRepository.findById(1)).thenReturn(Optional.of(testVariant));
            when(variantRepository.hasActiveOrders(1)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> variantService.deleteVariant(1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("active orders");

            verify(variantRepository, never()).delete(any());
        }
    }

    // ==================== getTotalStock ====================

    @Nested
    @DisplayName("getTotalStock")
    class GetTotalStock {

        @Test
        @DisplayName("Should sum all variant quantities")
        void getTotalStock_multipleVariants_sumsQuantities() {
            // Arrange
            ProductVariant v1 = new ProductVariant();
            v1.setQuantity(10);
            ProductVariant v2 = new ProductVariant();
            v2.setQuantity(20);
            ProductVariant v3 = new ProductVariant();
            v3.setQuantity(15);

            when(variantRepository.findByProductId(1)).thenReturn(Arrays.asList(v1, v2, v3));

            // Act
            int result = variantService.getTotalStock(1);

            // Assert
            assertThat(result).isEqualTo(45);
        }

        @Test
        @DisplayName("Should handle null quantities")
        void getTotalStock_nullQuantity_treatsAsZero() {
            // Arrange
            ProductVariant v1 = new ProductVariant();
            v1.setQuantity(10);
            ProductVariant v2 = new ProductVariant();
            v2.setQuantity(null);

            when(variantRepository.findByProductId(1)).thenReturn(Arrays.asList(v1, v2));

            // Act
            int result = variantService.getTotalStock(1);

            // Assert
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return 0 for product with no variants")
        void getTotalStock_noVariants_returnsZero() {
            // Arrange
            when(variantRepository.findByProductId(1)).thenReturn(List.of());

            // Act
            int result = variantService.getTotalStock(1);

            // Assert
            assertThat(result).isEqualTo(0);
        }
    }
}
