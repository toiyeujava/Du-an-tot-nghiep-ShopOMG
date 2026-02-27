# Phân Tích Cấu Trúc MVC - ShopOMG

## 1. Cấu Trúc Thư Mục Hiện Tại

```
src/main/java/poly/edu/
├── UdpmSpringBootPro2113Application.java   ← Entry point
│
├── config/                     ← Cấu hình hệ thống
│   ├── SecurityConfig.java          (Spring Security + OAuth2)
│   ├── WebMvcConfig.java            (Static resources, Upload path)
│   ├── WebSocketConfig.java         (WebSocket/STOMP)
│   ├── CurrentUserAdvice.java       (@ControllerAdvice - inject user vào model)
│   └── GlobalModelAttributes.java   (@ControllerAdvice - cart count, categories)
│
├── controller/                 ← CONTROLLER LAYER (View Layer)
│   ├── admin/                      ← Quản trị viên
│   │   ├── AdminAccountController.java
│   │   ├── AdminCategoryController.java
│   │   ├── AdminChatController.java
│   │   ├── AdminDashboardController.java
│   │   ├── AdminOrderController.java
│   │   ├── AdminProductController.java
│   │   └── AdminProductVariantController.java
│   ├── user/                       ← Khách hàng
│   │   ├── AccountAuthController.java
│   │   ├── AccountProfileController.java
│   │   ├── AddressController.java
│   │   ├── CartController.java
│   │   ├── CheckoutController.java
│   │   ├── HomeController.java
│   │   ├── PasswordResetController.java
│   │   └── ShopController.java
│   └── common/                     ← Dùng chung (Auth, Chat, Email)
│       ├── AuthController.java
│       ├── ChatController.java
│       └── EmailVerificationController.java
│
├── service/                    ← SERVICE LAYER (Business Logic)
│   ├── AccountService.java          ← ⚠ Concrete class (không có interface)
│   ├── AddressService.java          ← ✅ Interface
│   ├── AddressServiceImpl.java      ← ✅ Implementation
│   ├── AdminAccountService.java     ← ⚠ Concrete class
│   ├── AuditLogService.java         ← ⚠ Concrete class
│   ├── CartService.java             ← ✅ Interface
│   ├── CartServiceImpl.java         ← ✅ Implementation
│   ├── CategoryService.java         ← ⚠ Concrete class
│   ├── DashboardService.java        ← ⚠ Concrete class
│   ├── EmailService.java            ← ⚠ Concrete class
│   ├── EmailVerificationService.java← ⚠ Concrete class
│   ├── ExcelExportService.java      ← ⚠ Concrete class
│   ├── FileService.java             ← ⚠ Concrete class
│   ├── InMemoryChatService.java     ← ⚠ Concrete class
│   ├── LoginAttemptService.java     ← ⚠ Concrete class
│   ├── OrderCommandService.java     ← ⚠ Concrete class (CQRS pattern)
│   ├── OrderQueryService.java       ← ⚠ Concrete class (CQRS pattern)
│   ├── OrderService.java            ← ⚠ Concrete class
│   ├── PasswordResetService.java    ← ⚠ Concrete class
│   ├── ProductService.java          ← ⚠ Concrete class
│   └── ProductVariantService.java   ← ⚠ Concrete class
│
├── repository/                 ← REPOSITORY LAYER (Data Access)
│   ├── AccountRepository.java
│   ├── AddressRepository.java
│   ├── AuditLogRepository.java
│   ├── CartRepository.java
│   ├── CategoryRepository.java
│   ├── EmailVerificationTokenRepository.java
│   ├── OrderDetailRepository.java
│   ├── OrderRepository.java
│   ├── PasswordResetTokenRepository.java
│   ├── ProductImageRepository.java
│   ├── ProductRepository.java
│   ├── ProductVariantRepository.java
│   └── RoleRepository.java
│
├── entity/                     ← MODEL LAYER (Domain Objects)
│   ├── Account.java
│   ├── Address.java
│   ├── AuditLog.java
│   ├── Cart.java
│   ├── Category.java
│   ├── ChatMessage.java
│   ├── EmailVerificationToken.java
│   ├── Order.java
│   ├── OrderDetail.java
│   ├── PasswordResetToken.java
│   ├── Product.java
│   ├── ProductImage.java
│   ├── ProductVariant.java
│   └── Role.java
│
├── dto/                        ← Data Transfer Objects
│   ├── AddressDTO.java
│   ├── AddressRequest.java
│   ├── CategoryCountDTO.java
│   ├── ForgotPasswordForm.java
│   ├── ProfileForm.java
│   ├── ResetPasswordForm.java
│   └── SignUpForm.java
│
├── exception/                  ← Exception Handling
│   ├── AddressNotFoundException.java
│   ├── CannotDeleteDefaultAddressException.java
│   ├── GlobalExceptionHandler.java
│   └── UnauthorizedAccessException.java
│
├── security/                   ← Security Layer
│   ├── CustomOAuth2UserService.java
│   └── CustomUserDetailsService.java
│
├── validation/                 ← Custom Validators
│   ├── PasswordStrengthValidator.java
│   └── StrongPassword.java
│
├── init/                       ← Database Initializer
│   └── DatabaseInitializer.java
│
└── listener/                   ← Event Listeners
    └── AuthenticationEventListener.java
```

---

## 2. Đánh Giá Tuân Thủ MVC

### ✅ Điểm mạnh (Đã làm tốt)

| Tiêu chí | Đánh giá |
|---|---|
| **Controller tổ chức rõ ràng** | ✅ Phân chia `admin/`, `user/`, `common/` rất chuyên nghiệp |
| **Entity (Model) đầy đủ** | ✅ 14 entity với Lombok, đầy đủ quan hệ |
| **Repository layer** | ✅ 13 repository sử dụng Spring Data JPA chuẩn |
| **DTO pattern** | ✅ Có tách DTO cho form binding & data transfer |
| **Exception handling** | ✅ Có `GlobalExceptionHandler` + custom exceptions |
| **View layer** | ✅ Templates phân chia `admin/`, `user/`, `fragments/` logic |
| **Security** | ✅ Spring Security + OAuth2 (Google, Facebook) + Remember Me |

### ⚠ Vấn đề cần cải thiện

| # | Vấn đề | Mức độ | Chi tiết |
|---|--------|--------|----------|
| 1 | **Service layer không nhất quán** | 🔴 Cao | Chỉ 2/19 service có interface (`CartService`, `AddressService`), 17 service còn lại là concrete class |
| 2 | **Controller inject Repository trực tiếp** | 🟡 Trung bình | `HomeController` inject `ProductRepository` và `CategoryRepository` thay vì qua service |
| 3 | **Thiếu tách biệt service interface/impl** | 🔴 Cao | Vi phạm nguyên tắc Dependency Inversion (SOLID) |
| 4 | **OrderService có 3 class** | 🟡 Trung bình | `OrderService` + `OrderCommandService` + `OrderQueryService` - thiếu interface thống nhất |
| 5 | **Config chứa ControllerAdvice** | 🟢 Nhẹ | `CurrentUserAdvice.java` và `GlobalModelAttributes.java` nên nằm trong package riêng |

---

## 3. Đề Xuất Cải Tiến Cấu Trúc MVC

### Cấu trúc đề xuất (lý tưởng):

```
src/main/java/poly/edu/
├── config/                     ← Cấu hình thuần túy
│   ├── SecurityConfig.java
│   ├── WebMvcConfig.java
│   └── WebSocketConfig.java
│
├── controller/                 ← Giữ nguyên (đã tốt)
│   ├── admin/
│   ├── user/
│   └── common/
│
├── advice/                     ← ★ MỚI: ControllerAdvice tách riêng
│   ├── CurrentUserAdvice.java
│   └── GlobalModelAttributes.java
│
├── service/                    ← ★ CẢI TIẾN: Interface + Impl
│   ├── interfaces/             ← Tất cả Service Interface
│   │   ├── AccountService.java
│   │   ├── CartService.java
│   │   ├── OrderService.java
│   │   ├── ProductService.java
│   │   ├── CategoryService.java
│   │   └── ...
│   └── impl/                   ← Tất cả Implementation
│       ├── AccountServiceImpl.java
│       ├── CartServiceImpl.java
│       ├── OrderServiceImpl.java
│       ├── ProductServiceImpl.java
│       ├── CategoryServiceImpl.java
│       └── ...
│
├── repository/                 ← Giữ nguyên (đã tốt)
├── entity/                     ← Giữ nguyên (đã tốt)
├── dto/                        ← Giữ nguyên (đã tốt)
├── exception/                  ← Giữ nguyên (đã tốt)
├── security/                   ← Giữ nguyên (đã tốt)
├── validation/                 ← Giữ nguyên (đã tốt)
├── init/                       ← Giữ nguyên
└── listener/                   ← Giữ nguyên
```

### Ưu tiên sửa:

> [!IMPORTANT]
> **Lưu ý**: Cấu trúc hiện tại đã **80% chuẩn MVC**. Controller và Repository đã tổ chức rất tốt. Chỉ cần cải thiện Service layer để đạt 100%.

1. **P1 - Bắt buộc**: Tạo interface cho tất cả service class
2. **P2 - Nên làm**: Tách `CurrentUserAdvice`, `GlobalModelAttributes` ra khỏi `config/`
3. **P3 - Tốt hơn**: Sửa `HomeController` dùng service thay vì repository trực tiếp

---

## 4. Technology Stack

| Thành phần | Công nghệ |
|---|---|
| **Framework** | Spring Boot 4.0.1 |
| **Template Engine** | Thymeleaf + Thymeleaf Security |
| **ORM** | Spring Data JPA + Hibernate |
| **Database** | SQL Server (MSSQL) |
| **Security** | Spring Security 6 + OAuth2 (Google, Facebook) |
| **Validation** | Jakarta Bean Validation |
| **Email** | Spring Boot Mail (Gmail SMTP) |
| **WebSocket** | Spring WebSocket + STOMP |
| **Export** | Apache POI (Excel) |
| **Build** | Maven |
| **Java** | 17 |
| **CSS/JS** | Static files (vanilla) |
