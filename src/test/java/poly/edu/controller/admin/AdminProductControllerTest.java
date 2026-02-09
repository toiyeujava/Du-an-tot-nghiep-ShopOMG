package poly.edu.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import poly.edu.entity.Category;
import poly.edu.entity.Product;
import poly.edu.entity.ProductVariant;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;
import poly.edu.service.ProductVariantService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminProductControllerTest - Unit tests for product management.
 * 
 * Test Strategy:
 * 1. Use @WebMvcTest for isolated controller testing
 * 2. Mock all dependencies (ProductService, CategoryService,
 * ProductVariantService)
 * 3. Use @WithMockUser to simulate authenticated admin
 * 4. Test happy path and error cases
 * 
 * Naming Convention:
 * methodName_stateUnderTest_expectedBehavior
 */
@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ProductVariantService productVariantService;

    private Product testProduct;
    private Category testCategory;
    private List<Product> productList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("T-Shirts");

        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("Basic T-Shirt");
        testProduct.setPrice(199000.0);
        testProduct.setCategoryId(testCategory.getId());
        testProduct.setIsActive(true);

        Product product2 = new Product();
        product2.setId(2);
        product2.setName("Premium T-Shirt");
        product2.setPrice(299000.0);
        product2.setCategoryId(testCategory.getId());
        product2.setIsActive(true);

        productList = Arrays.asList(testProduct, product2);
    }

    // ==================== GET /admin/products ====================

    @Nested
    @DisplayName("GET /admin/products - List Products")
    class ListProducts {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return products page with pagination")
        void products_withValidRequest_returnsProductsPage() throws Exception {
            // Arrange
            Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 2);
            when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

            // Act & Assert
            mockMvc.perform(get("/admin/products")
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/products"))
                    .andExpect(model().attributeExists("products"))
                    .andExpect(model().attribute("currentPage", 0))
                    .andExpect(model().attribute("totalPages", 1))
                    .andExpect(model().attribute("totalItems", 2L));

            verify(productService, times(1)).getAllProducts(any(PageRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should use default pagination when not provided")
        void products_withoutPagination_usesDefaults() throws Exception {
            // Arrange
            Page<Product> emptyPage = new PageImpl<>(List.of());
            when(productService.getAllProducts(any(PageRequest.class))).thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/admin/products"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 0));

            verify(productService)
                    .getAllProducts(argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
        }
    }

    // ==================== GET /admin/products/create ====================

    @Nested
    @DisplayName("GET /admin/products/create - Create Form")
    class CreateForm {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return create form with categories")
        void createProductForm_returnsFormWithCategories() throws Exception {
            // Arrange
            when(categoryService.getAllCategories()).thenReturn(List.of(testCategory));

            // Act & Assert
            mockMvc.perform(get("/admin/products/create"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/product-form"))
                    .andExpect(model().attributeExists("product"))
                    .andExpect(model().attributeExists("categories"));

            verify(categoryService, times(1)).getAllCategories();
        }
    }

    // ==================== POST /admin/products ====================

    @Nested
    @DisplayName("POST /admin/products - Create Product")
    class CreateProduct {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create product and redirect with success message")
        void createProduct_withValidData_redirectsWithSuccess() throws Exception {
            // Arrange
            when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

            // Act & Assert
            mockMvc.perform(post("/admin/products")
                    .with(csrf())
                    .param("name", "New Product")
                    .param("price", "199000"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(productService, times(1)).createProduct(any(Product.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle creation error and show error message")
        void createProduct_withError_redirectsWithError() throws Exception {
            // Arrange
            when(productService.createProduct(any(Product.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            mockMvc.perform(post("/admin/products")
                    .with(csrf())
                    .param("name", "New Product"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    // ==================== POST /admin/products/{id}/delete ====================

    @Nested
    @DisplayName("POST /admin/products/{id}/delete - Delete Product")
    class DeleteProduct {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete product and redirect with success")
        void deleteProduct_withNoActiveOrders_success() throws Exception {
            // Arrange
            doNothing().when(productService).deleteProduct(1);

            // Act & Assert
            mockMvc.perform(post("/admin/products/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products"))
                    .andExpect(flash().attribute("successMessage", "Xóa sản phẩm thành công!"));

            verify(productService, times(1)).deleteProduct(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should show error when product has active orders")
        void deleteProduct_withActiveOrders_showsError() throws Exception {
            // Arrange
            doThrow(new IllegalStateException("Sản phẩm có đơn hàng đang xử lý"))
                    .when(productService).deleteProduct(1);

            // Act & Assert
            mockMvc.perform(post("/admin/products/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    // ==================== Variant Management ====================

    @Nested
    @DisplayName("Product Variants")
    class VariantTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should list variants for a product")
        void productVariants_returnsVariantsList() throws Exception {
            // Arrange
            ProductVariant variant = new ProductVariant();
            variant.setId(1);
            variant.setSize("M");
            variant.setColor("Red");
            variant.setQuantity(10);

            when(productService.getProductById(1)).thenReturn(Optional.of(testProduct));
            when(productVariantService.getVariantsByProduct(1)).thenReturn(List.of(variant));

            // Act & Assert
            mockMvc.perform(get("/admin/products/1/variants"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/product-variants"))
                    .andExpect(model().attributeExists("product"))
                    .andExpect(model().attributeExists("variants"))
                    .andExpect(model().attributeExists("newVariant"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should throw when product not found for variants")
        void productVariants_productNotFound_throwsException() throws Exception {
            // Arrange
            when(productService.getProductById(999)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/admin/products/999/variants"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create variant and redirect")
        void addVariant_withValidData_redirectsWithSuccess() throws Exception {
            // Arrange
            ProductVariant variant = new ProductVariant();
            variant.setId(1);
            when(productVariantService.createVariant(eq(1), any(ProductVariant.class)))
                    .thenReturn(variant);

            // Act & Assert
            mockMvc.perform(post("/admin/products/1/variants")
                    .with(csrf())
                    .param("size", "L")
                    .param("color", "Blue")
                    .param("quantity", "20"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products/1/variants"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete variant and redirect")
        void deleteVariant_withNoActiveOrders_success() throws Exception {
            // Arrange
            doNothing().when(productVariantService).deleteVariant(1);

            // Act & Assert
            mockMvc.perform(post("/admin/products/1/variants/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/products/1/variants"))
                    .andExpect(flash().attributeExists("successMessage"));
        }
    }
}
