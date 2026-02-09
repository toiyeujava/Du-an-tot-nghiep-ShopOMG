# 📊 PHÂN TÍCH TOÀN DIỆN DỰ ÁN SHOPOMG - CHỨC NĂNG ADMIN

## 1. TỔNG QUAN DỰ ÁN

### 1.1 Mô tả
**ShopOMG** là một ứng dụng thương mại điện tử (E-Commerce) được xây dựng trên nền tảng **Spring Boot**. Dự án cung cấp đầy đủ các chức năng cho cả khách hàng và quản trị viên (admin).

### 1.2 Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| **Backend Framework** | Spring Boot 3.x |
| **Template Engine** | Thymeleaf |
| **ORM** | JPA/Hibernate |
| **Database** | SQL Server |
| **Security** | Spring Security + OAuth2 (Google) |
| **Build Tool** | Maven |
| **WebSocket** | Spring WebSocket (Chat real-time) |
| **Frontend** | Bootstrap, Chart.js |

### 1.3 Kiến trúc dự án

```
src/main/java/poly/edu/
├── config/           # Cấu hình (Security, WebMvc, WebSocket)
├── controller/       # 9 Controllers xử lý request
├── dto/              # 7 Data Transfer Objects
├── entity/           # 13 Entities (JPA)
├── exception/        # 3 Exception handlers
├── init/             # Khởi tạo dữ liệu ban đầu
├── listener/         # Event listeners
├── repository/       # 12 JPA Repositories
├── security/         # OAuth2 User Service
├── service/          # 15 Business Services
└── validation/       # Custom validators
```

---

## 2. CẤU TRÚC DỮ LIỆU (DATA STRUCTURES)

### 2.1 Entity Relationship Diagram (Tóm tắt)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    Role     │────→│   Account   │←────│   Address   │
│ (id, name)  │ 1:n │             │ 1:n │             │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │ 1:n
                    ┌──────┴──────┐
                    ↓             ↓
              ┌─────────┐   ┌─────────┐
              │  Order  │   │  Cart   │
              └────┬────┘   └────┬────┘
                   │ 1:n         │ n:1
              ┌────┴────┐   ┌────┴────┐
              │OrderDtl │   │Product  │
              └────┬────┘   │Variant  │
                   │ n:1    └────┬────┘
              ┌────┴──────────────┴────┐
              │       Product          │
              │ (variants, images)     │
              └───────────┬────────────┘
                          │ n:1
                    ┌─────┴─────┐
                    │ Category  │
                    └───────────┘
```

### 2.2 Chi tiết các Entity

#### **Account** (Tài khoản người dùng)
```java
Fields:
- id: Integer (PK, auto-increment)
- username: String (unique, not null)
- password: String (BCrypt hashed)
- fullName: String
- email: String (unique)
- phone: String
- avatar: String
- role: Role (FK)
- birthDate: LocalDate
- gender: String (MALE/FEMALE/OTHER)
- isActive: Boolean (khóa/mở tài khoản)
- emailVerified: Boolean
- failedLoginAttempts: Integer (chống brute-force)
- accountLockedUntil: LocalDateTime
- lastLogin: LocalDateTime
- createdAt, updatedAt: LocalDateTime
```

#### **Product** (Sản phẩm)
```java
Fields:
- id: Integer (PK)
- name: String
- slug: String
- description: String
- material: String
- origin: String
- categoryId: Integer (FK)
- image: String
- gender: String
- price: Double
- discount: Integer (%)
- viewCount: Integer
- isActive: Boolean (soft delete)
- createdAt: Date
- variants: List<ProductVariant> (1:n)
- productImages: List<ProductImage> (1:n)
```

#### **Order** (Đơn hàng)
```java
Fields:
- id: Integer (PK)
- account: Account (FK)
- orderDate: LocalDateTime
- status: String (PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED)
- totalAmount: BigDecimal
- shippingFee: BigDecimal
- discountAmount: BigDecimal
- finalAmount: BigDecimal
- paymentMethod: String
- shippingAddress: String
- receiverName, receiverPhone: String
- note: String
- orderDetails: List<OrderDetail>
```

#### **Category** (Danh mục)
```java
Fields:
- id: Integer (PK)
- name: String
- slug: String
- image: String
- isActive: Boolean
```

#### **Role** (Vai trò)
```java
Fields:
- id: Integer (PK)
- name: String (ADMIN/USER)
```

---

## 3. THUẬT TOÁN VÀ CẤU TRÚC DỮ LIỆU SỬ DỤNG

### 3.1 Thuật toán chính

| Thuật toán | Mô tả | Áp dụng tại |
|------------|-------|-------------|
| **Pagination (Phân trang)** | Sử dụng Spring Data `Pageable` | Tất cả danh sách (products, orders, accounts) |
| **State Machine** | Trạng thái đơn hàng: PENDING → CONFIRMED → SHIPPING → COMPLETED | `OrderService` |
| **Soft Delete** | Đánh dấu `isActive = false` thay vì xóa cứng | Product, Account |
| **Constraint Checking** | Kiểm tra ràng buộc trước khi xóa | Category, Account |
| **BCrypt Hashing** | Hash password với salt | Account password |
| **UUID Generation** | Tạo random password | `AdminAccountService.generateRandomPassword()` |
| **Stream Filter** | Lọc dữ liệu trong memory | Search, count functions |
| **SQL Aggregation** | SUM, COUNT, GROUP BY | Dashboard statistics |

### 3.2 Data Structures sử dụng

| Data Structure | Mục đích | Ví dụ |
|----------------|----------|-------|
| **Page<T>** | Phân trang với metadata | `Page<Product>`, `Page<Order>` |
| **List<T>** | Danh sách động | Categories, OrderDetails |
| **Map<String, Object>** | Key-value cho chart data | Revenue chart, Dashboard stats |
| **Optional<T>** | Xử lý null-safety | `getById()` methods |
| **Stream API** | Xử lý collection theo kiểu functional | Filter, count operations |

---

## 4. CHỨC NĂNG ADMIN

### 4.1 Dashboard (Tổng quan)

**Controller:** `AdminController.dashboard()`
**Service:** `DashboardService`

| Metric | Mô tả | Thuật toán |
|--------|-------|------------|
| Monthly Revenue | Doanh thu tháng hiện tại | `SUM(final_amount) WHERE status='COMPLETED'` |
| Pending Orders | Số đơn chờ xử lý | `COUNT WHERE status='PENDING'` |
| Total Customers | Tổng khách hàng | `COUNT WHERE role='USER'` |
| Total Products | Tổng sản phẩm active | `COUNT WHERE isActive=true` |
| Revenue Chart | Biểu đồ doanh thu 6 tháng | Loop + Monthly aggregation |
| Top Products | Sản phẩm bán chạy | `GROUP BY + ORDER BY + LIMIT` |

### 4.2 Quản lý Sản phẩm (Products)

**Endpoints:**
- `GET /admin/products` - Danh sách (phân trang)
- `GET /admin/products/new` - Form tạo mới
- `POST /admin/products` - Tạo sản phẩm
- `GET /admin/products/edit/{id}` - Form sửa
- `POST /admin/products/update/{id}` - Cập nhật
- `POST /admin/products/delete/{id}` - Xóa (soft delete)

**Thuật toán:**
```
createProduct():
1. Validate required fields (name, price, categoryId)
2. Set defaults (isActive=true, discount=0, viewCount=0)
3. Save to database
Time: O(1)

deleteProduct():
1. Check active orders (PENDING/CONFIRMED/SHIPPING)
2. If has active orders → throw exception
3. Soft delete (isActive = false)
Time: O(1)
```

### 4.3 Quản lý Đơn hàng (Orders)

**Endpoints:**
- `GET /admin/orders` - Danh sách (filter by status)
- `GET /admin/orders/{id}` - Chi tiết đơn
- `POST /admin/orders/approve/{id}` - Duyệt đơn
- `POST /admin/orders/ship/{id}` - Chuyển sang giao hàng
- `POST /admin/orders/complete/{id}` - Hoàn thành
- `POST /admin/orders/cancel/{id}` - Hủy đơn

**State Machine:**
```
PENDING ──approve──→ CONFIRMED ──ship──→ SHIPPING ──complete──→ COMPLETED
   │                      │                  │
   └──────────cancel──────┴──────cancel──────┘
                          ↓
                     CANCELLED
```

**Thuật toán hủy đơn (quan trọng):**
```
cancelOrder():
1. Load order with orderDetails (eager fetch)
2. For each orderDetail:
   a. Get productVariant
   b. Restore quantity: variant.quantity += orderDetail.quantity
   c. Save variant
3. Set order.status = 'CANCELLED'
4. Save order
Time: O(n) where n = number of order items
Transaction: ACID đảm bảo atomicity
```

### 4.4 Quản lý Danh mục (Categories)

**Endpoints:**
- `GET /admin/categories` - Danh sách
- `POST /admin/categories` - Tạo mới (AJAX/JSON)
- `PUT /admin/categories/{id}` - Cập nhật
- `DELETE /admin/categories/{id}` - Xóa

**Thuật toán xóa:**
```
deleteCategory():
1. Count products in category
2. If count > 0 → throw exception với thông báo số sản phẩm
3. If count = 0 → delete category
Time: O(1)
```

### 4.5 Quản lý Tài khoản (Accounts)

**Endpoints:**
- `GET /admin/accounts` - Danh sách (phân trang)
- `GET /admin/accounts/{id}` - Chi tiết + lịch sử mua
- `POST /admin/accounts/lock/{id}` - Khóa tài khoản
- `POST /admin/accounts/unlock/{id}` - Mở khóa
- `POST /admin/accounts/reset-password/{id}` - Reset mật khẩu
- `POST /admin/accounts/delete/{id}` - Xóa tài khoản

**Thuật toán:**
```
lockAccount():
1. Find account by ID
2. Check if ADMIN → throw exception (không khóa admin)
3. Set isActive = false
4. Set accountLockedUntil = now + 100 years (effectively permanent)
Time: O(1)

resetPassword():
1. Generate random password (UUID first 8 chars)
2. Hash with BCrypt
3. Update account
4. Reset failedLoginAttempts = 0
5. Return plain password (để gửi email cho user)
Time: O(1)

deleteAccount():
1. Check if ADMIN → throw exception
2. Check active orders (PENDING/CONFIRMED/SHIPPING)
3. If has active orders → throw exception
4. Hard delete account
Time: O(1)
```

---

## 5. PHÂN TÍCH TIME & SPACE COMPLEXITY

### 5.1 Service Methods Complexity

| Method | Time Complexity | Space Complexity | Ghi chú |
|--------|-----------------|------------------|---------|
| `getAllUsers(pageable)` | O(n) | O(n) | n = page size |
| `getUserById(id)` | O(1) | O(1) | Primary key lookup |
| `lockAccount(id)` | O(1) | O(1) | Single update |
| `unlockAccount(id)` | O(1) | O(1) | Single update |
| `resetPassword(id)` | O(1) | O(1) | UUID + BCrypt |
| `deleteAccount(id)` | O(m) | O(1) | m = user's orders |
| `searchUsers(keyword)` | O(n) | O(k) | n = all users, k = results |
| `getMonthlyRevenue()` | O(n) | O(1) | n = orders in month |
| `getRevenueChartData(months)` | O(m×n) | O(m) | m months, n orders/month |
| `getTopProducts(limit)` | O(n log n) | O(k) | Sort + limit |
| `cancelOrder(id)` | O(n) | O(1) | n = order items |
| `deleteCategory(id)` | O(n) | O(1) | n = products to count |

### 5.2 Vấn đề hiệu suất cần lưu ý

> [!WARNING]
> **Các method sau sử dụng `findAll().stream()` - không tối ưu cho dữ liệu lớn:**

```java
// DashboardService.getTotalCustomers()
accountRepository.findAll().stream()
    .filter(account -> "USER".equals(account.getRole().getName()))
    .count();

// CategoryService.deleteCategory()
productRepository.findAll().stream()
    .filter(p -> p.getCategoryId().equals(id))
    .count();

// AdminAccountService.searchUsers()
accountRepository.findAll().stream()
    .filter(...)
    .toList();
```

**Đề xuất cải thiện:** Sử dụng **native query** hoặc **JPQL** với filtering tại database.

---

## 6. CHỨC NĂNG ADMIN ĐANG THIẾU

### 6.1 ⚠️ Thiếu nghiêm trọng

| Chức năng | Mô tả | Ưu tiên |
|-----------|-------|---------|
| **Quản lý Variants** | Không có UI quản lý size/color của sản phẩm | 🔴 Cao |
| **Quản lý Product Images** | Không có chức năng thêm/xóa ảnh sản phẩm | 🔴 Cao |
| **Export/Import dữ liệu** | Xuất/nhập Excel cho products, orders | 🔴 Cao |
| **Quản lý Khuyến mãi/Voucher** | Không có entity và chức năng quản lý mã giảm giá | 🔴 Cao |

### 6.2 🟡 Thiếu quan trọng

| Chức năng | Mô tả | Ưu tiên |
|-----------|-------|---------|
| **Search/Filter nâng cao** | Tìm kiếm sản phẩm, đơn hàng theo nhiều tiêu chí | 🟡 Trung bình |
| **Báo cáo chi tiết** | Báo cáo theo ngày, tuần, tháng; So sánh kỳ | 🟡 Trung bình |
| **Audit Log** | Lưu lại lịch sử thao tác của admin | 🟡 Trung bình |
| **Quản lý Banner/Slider** | Quản lý hình ảnh quảng cáo trên trang chủ | 🟡 Trung bình |
| **Bulk Operations** | Xóa/cập nhật nhiều sản phẩm cùng lúc | 🟡 Trung bình |
| **Notification System** | Thông báo khi có đơn mới | 🟡 Trung bình |

### 6.3 🟢 Có thể bổ sung

| Chức năng | Mô tả | Ưu tiên |
|-----------|-------|---------|
| **Quản lý Reviews/Ratings** | Duyệt đánh giá sản phẩm | 🟢 Thấp |
| **SEO Management** | Quản lý meta tags, sitemap | 🟢 Thấp |
| **Email Templates** | Quản lý mẫu email | 🟢 Thấp |
| **Settings/Configuration** | Cấu hình hệ thống (shipping fee, etc.) | 🟢 Thấp |
| **Multi-admin roles** | Phân quyền chi tiết (Super Admin, Staff) | 🟢 Thấp |

---

## 7. ĐỀ XUẤT CẢI TIẾN

### 7.1 Performance Optimization

```java
// Thay thế:
accountRepository.findAll().stream().filter(...).count();

// Bằng:
@Query("SELECT COUNT(a) FROM Account a WHERE a.role.name = :roleName")
long countByRoleName(@Param("roleName") String roleName);
```

### 7.2 Thêm chức năng Variant Management

```java
// Thêm endpoints trong AdminController:
@GetMapping("/products/{id}/variants")
@PostMapping("/products/{id}/variants")
@PutMapping("/products/{productId}/variants/{variantId}")
@DeleteMapping("/products/{productId}/variants/{variantId}")
```

### 7.3 Thêm Audit Logging

```java
@Entity
public class AuditLog {
    private Integer id;
    private Account admin;
    private String action; // CREATE, UPDATE, DELETE
    private String entityType; // Product, Order, Account
    private Integer entityId;
    private String details;
    private LocalDateTime timestamp;
}
```

### 7.4 Thêm Dashboard Realtime

Sử dụng WebSocket đã có để cập nhật dashboard realtime khi có đơn hàng mới.

---

## 8. TÓM TẮT

### Điểm mạnh ✅
- Kiến trúc MVC rõ ràng, dễ bảo trì
- Sử dụng Spring Security với OAuth2
- State machine cho order workflow
- Soft delete cho data retention
- Constraint checking trước khi xóa
- Đã có documentation tốt trong code (javadoc với complexity)

### Điểm cần cải thiện ❌
- Không có quản lý variants/images
- Một số query không tối ưu (stream filter thay vì SQL)
- Chưa có export/import data
- Chưa có audit logging
- Dashboard data còn hardcode (chưa dynamic hoàn toàn)

---

*Tài liệu được tạo: 2026-02-09*
*Phiên bản: 1.0*
