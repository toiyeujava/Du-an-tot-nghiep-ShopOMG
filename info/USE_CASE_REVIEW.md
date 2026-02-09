# Đánh Giá Use Case Diagrams - Dự Án ShopOMG

## Tổng Quan

Tôi đã phân tích chi tiết Use Case Diagrams của bạn và so sánh với code thực tế trong dự án ShopOMG. Dưới đây là đánh giá toàn diện.

---

## ✅ ĐIỂM MẠNH - CÁC USE CASE ĐÃ VẼ ĐÚNG

### 1. **Nhóm Đăng Nhập/Đăng Ký** ✅
Bạn đã vẽ đầy đủ và chính xác:
- ✅ **Đăng nhập** (Login)
- ✅ **Đăng xuất** (Logout)
- ✅ **Đăng nhập bằng Facebook** (Sử dụng mạo Facebook)
- ✅ **Đăng nhập bằng Google** (Sử dụng mạo Google)
- ✅ **Giới hạn đăng nhập sai** (Login attempt limiting)

**Khớp với code:**
- `AuthController.java` - `/login`, `/register`
- `CustomOAuth2UserService.java` - OAuth2 integration
- `LoginAttemptService.java` - Login attempt tracking

### 2. **Nhóm Quản Lý Tài Khoản** ✅
- ✅ **Xem chi tiết cá nhân** (Xem chi tiết cá nhân)
- ✅ **Xem đơn hàng của tôi** (Xem đơn hàng của tôi)
- ✅ **Xem đánh giá sản phẩm** (Xem đánh giá sản phẩm)
- ✅ **Tìm kiếm thông tin/sản phẩm** (Tìm kiếm thông tin/sản phẩm)

**Khớp với code:**
- `AccountController.java` - `/profile`, `/orders`, `/reviews`

### 3. **Nhóm Email Verification** ✅
- ✅ **Email Verification** 
- ✅ **Tăng cường validation** (Password strength, etc.)
- ✅ **Đăng ký** (Registration)

**Khớp với code:**
- `EmailVerificationController.java` - `/verify-email`, `/resend-verification`

### 4. **Nhóm Quản Lý Admin** ✅
Bạn đã vẽ đầy đủ các chức năng admin:
- ✅ **Quản lý Tài khoản** (Quản lí - Tài khoản)
- ✅ **Quản lý Đơn hàng** (Quản lí - Đơn hàng)
- ✅ **Quản lý Sản phẩm** (Quản lí - Sản phẩm)
- ✅ **Quản lý Danh mục** (Quản lí - Danh mục)
- ✅ **Dashboard** (Quản lí - Dashboard)
- ✅ **CRUD sản phẩm** (CRUD sản phẩm)
- ✅ **CRUD danh mục** (CRUD danh mục)

**Khớp với code:**
- `AdminController.java` - `/admin/dashboard`, `/admin/products`, `/admin/orders`, `/admin/categories`, `/admin/accounts`

### 5. **Nhóm Giỏ Hàng & Thanh Toán** ✅
- ✅ **Giỏ hàng** (Giỏ hàng)
- ✅ **Xóa item** (Xóa item)
- ✅ **Xem giỏ** (Xem giỏ)
- ✅ **Sản phẩm đã thêm** (Sản phẩm đã thêm)
- ✅ **Thêm vào giỏ** (Thêm vào giỏ)
- ✅ **Xóa khỏi giỏ** (Xóa khỏi giỏ)

**Khớp với code:**
- `HomeController.java` - `/cart`, `/checkout`

### 6. **Nhóm Xử Lý** ✅
- ✅ **Xác thực** (Xác Thực)
- ✅ **Xem chi tiết sản phẩm** (Xem chi tiết sản phẩm)
- ✅ **Quản/Cập nhật hồ sơ cá nhân** (Quản/Cập nhật hồ sơ cá nhân)

### 7. **Nhóm Tài Khoản** ✅
- ✅ **Tài khoản** (Tài khoản)
- ✅ **Đơn mua** (Đơn mua)
- ✅ **Đánh giá sản phẩm** (Đánh giá sản phẩm)

---

## ⚠️ VẤN ĐỀ CẦN CHỈNH SỬA

### 1. **Thiếu Use Case: Password Reset** ❌

**Vấn đề:** Bạn CHƯA vẽ use case cho chức năng **Quên mật khẩu / Đặt lại mật khẩu**

**Code thực tế có:**
```java
// PasswordResetController.java
@GetMapping("/forgot-password")    // Hiển thị form quên mật khẩu
@PostMapping("/forgot-password")   // Xử lý yêu cầu reset
@GetMapping("/reset-password")     // Hiển thị form đặt lại mật khẩu
@PostMapping("/reset-password")    // Xử lý đặt lại mật khẩu
```

**Cần thêm:**
- Use case: **"Quên mật khẩu"** (Forgot Password)
- Use case: **"Đặt lại mật khẩu"** (Reset Password)
- Actor: **Khách hàng** (Customer/User)

### 2. **Thiếu Use Case: Resend Verification Email** ❌

**Vấn đề:** Bạn có "Email Verification" nhưng thiếu **"Gửi lại email xác thực"**

**Code thực tế có:**
```java
// EmailVerificationController.java
@GetMapping("/resend-verification")
@PostMapping("/resend-verification")
```

**Cần thêm:**
- Use case: **"Gửi lại email xác thực"** (Resend Verification Email)

### 3. **Thiếu Use Case: Cập Nhật Thông Tin Cá Nhân** ❌

**Vấn đề:** Bạn có "Xem chi tiết cá nhân" nhưng thiếu **"Cập nhật thông tin cá nhân"**

**Code thực tế có:**
```java
// AccountController.java
@PostMapping("/update")  // Cập nhật profile với avatar upload
```

**Cần thêm:**
- Use case: **"Cập nhật thông tin cá nhân"** (Update Profile)
- Include: **"Upload Avatar"** (Upload ảnh đại diện)

### 4. **Thiếu Use Case: Xem Chi Tiết Đơn Hàng** ❌

**Vấn đề:** Bạn có "Xem đơn hàng của tôi" nhưng thiếu **"Xem chi tiết đơn hàng"** và **"Theo dõi trạng thái đơn hàng"**

**Cần thêm:**
- Use case: **"Xem chi tiết đơn hàng"** (View Order Details)
- Use case: **"Theo dõi trạng thái đơn hàng"** (Track Order Status)
- Use case: **"Hủy đơn hàng"** (Cancel Order) - nếu trạng thái PENDING

### 5. **Thiếu Use Case: Thanh Toán** ❌

**Vấn đề:** Bạn có "Giỏ hàng" nhưng thiếu luồng **Thanh toán**

**Code thực tế có:**
```java
// HomeController.java
@GetMapping("/checkout")  // Trang thanh toán
```

**Cần thêm:**
- Use case: **"Thanh toán đơn hàng"** (Checkout)
- Include: **"Chọn phương thức thanh toán"** (COD, VNPay, MoMo)
- Include: **"Nhập địa chỉ giao hàng"** (Enter Shipping Address)

### 6. **Thiếu Use Case: Tìm Kiếm & Lọc Sản Phẩm** ❌

**Vấn đề:** Bạn có "Tìm kiếm sản phẩm" nhưng thiếu **"Lọc sản phẩm"**

**Cần thêm:**
- Use case: **"Lọc sản phẩm theo danh mục"** (Filter by Category)
- Use case: **"Lọc sản phẩm theo giá"** (Filter by Price Range)
- Use case: **"Sắp xếp sản phẩm"** (Sort Products)

### 7. **Thiếu Use Case Admin: Quản Lý Người Dùng Chi Tiết** ❌

**Vấn đề:** Bạn có "Quản lý Tài khoản" nhưng thiếu các thao tác cụ thể

**Cần thêm:**
- Use case: **"Khóa/Mở khóa tài khoản"** (Lock/Unlock Account)
- Use case: **"Reset mật khẩu người dùng"** (Reset User Password)
- Use case: **"Xem lịch sử đơn hàng của người dùng"** (View User Order History)

### 8. **Thiếu Use Case: Đánh Giá Sản Phẩm (User)** ❌

**Vấn đề:** Bạn có "Xem đánh giá sản phẩm" nhưng thiếu **"Viết đánh giá sản phẩm"**

**Cần thêm:**
- Use case: **"Viết đánh giá sản phẩm"** (Write Product Review)
- Include: **"Upload ảnh đánh giá"** (Upload Review Photos)
- Precondition: Phải đã mua sản phẩm

---

## 🔧 KHUYẾN NGHỊ CHỈNH SỬA

### A. Cấu Trúc Use Case Diagram

#### **Diagram 1: Xác Thực & Bảo Mật** (Authentication & Security)
**Actors:** Khách hàng, Admin

**Use Cases:**
1. Đăng nhập (Login)
   - Include: Kiểm tra số lần đăng nhập sai
   - Include: Khóa tài khoản sau 5 lần sai
2. Đăng nhập bằng Facebook
3. Đăng nhập bằng Google
4. Đăng ký (Register)
   - Include: Email Verification
5. **Quên mật khẩu** (Forgot Password) - **THÊM MỚI**
6. **Đặt lại mật khẩu** (Reset Password) - **THÊM MỚI**
7. **Gửi lại email xác thực** (Resend Verification) - **THÊM MỚI**
8. Đăng xuất (Logout)

---

#### **Diagram 2: Quản Lý Tài Khoản** (Account Management)
**Actor:** Khách hàng

**Use Cases:**
1. Xem thông tin cá nhân (View Profile)
2. **Cập nhật thông tin cá nhân** (Update Profile) - **THÊM MỚI**
   - Include: Upload Avatar
3. Xem đơn hàng của tôi (My Orders)
4. **Xem chi tiết đơn hàng** (View Order Details) - **THÊM MỚI**
5. **Theo dõi trạng thái đơn hàng** (Track Order) - **THÊM MỚI**
6. **Hủy đơn hàng** (Cancel Order) - **THÊM MỚI**
7. Xem đánh giá của tôi (My Reviews)

---

#### **Diagram 3: Mua Sắm** (Shopping)
**Actor:** Khách hàng

**Use Cases:**
1. Duyệt sản phẩm (Browse Products)
2. Xem chi tiết sản phẩm (View Product Details)
3. **Tìm kiếm sản phẩm** (Search Products) - **ĐÃ CÓ**
4. **Lọc sản phẩm theo danh mục** (Filter by Category) - **THÊM MỚI**
5. **Lọc sản phẩm theo giá** (Filter by Price) - **THÊM MỚI**
6. **Sắp xếp sản phẩm** (Sort Products) - **THÊM MỚI**
7. Thêm vào giỏ hàng (Add to Cart)
8. Xem giỏ hàng (View Cart)
9. Cập nhật số lượng trong giỏ (Update Cart Quantity)
10. Xóa khỏi giỏ hàng (Remove from Cart)
11. **Thanh toán** (Checkout) - **THÊM MỚI**
    - Include: Nhập địa chỉ giao hàng
    - Include: Chọn phương thức thanh toán (COD/VNPay/MoMo)

---

#### **Diagram 4: Đánh Giá Sản Phẩm** (Product Reviews)
**Actor:** Khách hàng

**Use Cases:**
1. Xem đánh giá sản phẩm (View Reviews)
2. **Viết đánh giá sản phẩm** (Write Review) - **THÊM MỚI**
   - Include: Chọn số sao (1-5)
   - Include: Upload ảnh đánh giá
   - Precondition: Đã mua sản phẩm
3. **Sửa đánh giá** (Edit Review) - **THÊM MỚI**

---

#### **Diagram 5: Quản Lý Admin - Sản Phẩm** (Admin - Products)
**Actor:** Admin

**Use Cases:**
1. Xem danh sách sản phẩm (View Products)
2. Tạo sản phẩm mới (Create Product)
   - Include: Upload hình ảnh sản phẩm
   - Include: Chọn danh mục
3. Cập nhật sản phẩm (Update Product)
4. Xóa sản phẩm (Delete Product)
5. Tìm kiếm sản phẩm (Search Products)

---

#### **Diagram 6: Quản Lý Admin - Đơn Hàng** (Admin - Orders)
**Actor:** Admin

**Use Cases:**
1. Xem danh sách đơn hàng (View Orders)
2. Xem chi tiết đơn hàng (View Order Details)
3. **Cập nhật trạng thái đơn hàng** (Update Order Status) - **THÊM MỚI**
   - PENDING → CONFIRMED → SHIPPING → DELIVERED
4. **Hủy đơn hàng** (Cancel Order) - **THÊM MỚI**

---

#### **Diagram 7: Quản Lý Admin - Danh Mục** (Admin - Categories)
**Actor:** Admin

**Use Cases:**
1. Xem danh sách danh mục (View Categories)
2. Tạo danh mục mới (Create Category)
3. Cập nhật danh mục (Update Category)
4. Xóa danh mục (Delete Category)

---

#### **Diagram 8: Quản Lý Admin - Người Dùng** (Admin - Users)
**Actor:** Admin

**Use Cases:**
1. Xem danh sách người dùng (View Users)
2. Xem chi tiết người dùng (View User Details)
3. **Khóa tài khoản** (Lock Account) - **THÊM MỚI**
4. **Mở khóa tài khoản** (Unlock Account) - **THÊM MỚI**
5. **Reset mật khẩu người dùng** (Reset User Password) - **THÊM MỚI**
6. **Xem lịch sử đơn hàng của người dùng** (View User Orders) - **THÊM MỚI**
7. Tìm kiếm người dùng (Search Users)

---

#### **Diagram 9: Dashboard Admin** (Admin Dashboard)
**Actor:** Admin

**Use Cases:**
1. Xem tổng quan thống kê (View Statistics)
   - Tổng số sản phẩm
   - Tổng số đơn hàng
   - Tổng số người dùng
   - Tổng doanh thu
2. Xem đơn hàng gần đây (View Recent Orders)
3. Xem hoạt động gần đây (View Recent Activities)

---

## 📊 SO SÁNH: ĐÃ CÓ vs CẦN THÊM

| **Chức năng** | **Bạn đã vẽ** | **Cần thêm** | **Mức độ quan trọng** |
|---------------|---------------|--------------|------------------------|
| Đăng nhập | ✅ | - | ⭐⭐⭐⭐⭐ |
| Đăng ký | ✅ | - | ⭐⭐⭐⭐⭐ |
| OAuth2 Login | ✅ | - | ⭐⭐⭐⭐ |
| **Quên mật khẩu** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| **Đặt lại mật khẩu** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| Email Verification | ✅ | - | ⭐⭐⭐⭐⭐ |
| **Gửi lại email xác thực** | ❌ | ✅ | ⭐⭐⭐⭐ |
| Xem profile | ✅ | - | ⭐⭐⭐⭐ |
| **Cập nhật profile** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| **Upload Avatar** | ❌ | ✅ | ⭐⭐⭐ |
| Xem đơn hàng | ✅ | - | ⭐⭐⭐⭐ |
| **Xem chi tiết đơn hàng** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| **Theo dõi đơn hàng** | ❌ | ✅ | ⭐⭐⭐⭐ |
| **Hủy đơn hàng** | ❌ | ✅ | ⭐⭐⭐⭐ |
| Giỏ hàng | ✅ | - | ⭐⭐⭐⭐⭐ |
| **Thanh toán** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| **Chọn phương thức thanh toán** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| Tìm kiếm sản phẩm | ✅ | - | ⭐⭐⭐⭐ |
| **Lọc sản phẩm** | ❌ | ✅ | ⭐⭐⭐⭐ |
| **Sắp xếp sản phẩm** | ❌ | ✅ | ⭐⭐⭐ |
| Xem đánh giá | ✅ | - | ⭐⭐⭐ |
| **Viết đánh giá** | ❌ | ✅ | ⭐⭐⭐⭐ |
| **Upload ảnh đánh giá** | ❌ | ✅ | ⭐⭐⭐ |
| Admin - CRUD Sản phẩm | ✅ | - | ⭐⭐⭐⭐⭐ |
| **Admin - Upload hình ảnh SP** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| Admin - CRUD Danh mục | ✅ | - | ⭐⭐⭐⭐⭐ |
| Admin - Xem đơn hàng | ✅ | - | ⭐⭐⭐⭐⭐ |
| **Admin - Cập nhật trạng thái đơn** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| Admin - Quản lý tài khoản | ✅ | - | ⭐⭐⭐⭐ |
| **Admin - Khóa/Mở khóa tài khoản** | ❌ | ✅ | ⭐⭐⭐⭐⭐ |
| **Admin - Reset mật khẩu user** | ❌ | ✅ | ⭐⭐⭐⭐ |
| Admin - Dashboard | ✅ | - | ⭐⭐⭐⭐ |

---

## 🎯 KẾT LUẬN & ĐÁNH GIÁ TỔNG QUAN

### Điểm Mạnh:
✅ Bạn đã vẽ được **khoảng 60-70%** các use case chính của dự án  
✅ Cấu trúc phân chia Actor (Admin, Khách hàng) rõ ràng  
✅ Các use case cơ bản đều có (Login, Register, CRUD, Cart)  
✅ Có include relationship cho OAuth2 login  

### Điểm Cần Cải Thiện:
❌ Thiếu **15-20 use cases quan trọng** (đánh dấu ⭐⭐⭐⭐⭐ ở trên)  
❌ Thiếu luồng **Password Reset** (rất quan trọng cho UX)  
❌ Thiếu luồng **Checkout/Payment** (thiếu sót lớn)  
❌ Thiếu các use case **Update** (chỉ có View, không có Update)  
❌ Thiếu các use case **Admin quản lý chi tiết** (Lock/Unlock, Update Status)  

### Mức Độ Hoàn Thiện:
- **Tổng thể:** 65/100 điểm
- **Xác thực:** 75/100 (thiếu Password Reset)
- **Mua sắm:** 60/100 (thiếu Checkout, Filter)
- **Quản lý tài khoản:** 50/100 (thiếu Update, Track Order)
- **Admin:** 70/100 (thiếu các thao tác chi tiết)

---

## 📝 HÀNH ĐỘNG TIẾP THEO

### Ưu tiên cao (⭐⭐⭐⭐⭐):
1. ✅ Thêm use case **"Quên mật khẩu"** và **"Đặt lại mật khẩu"**
2. ✅ Thêm use case **"Thanh toán đơn hàng"** với các include
3. ✅ Thêm use case **"Cập nhật thông tin cá nhân"**
4. ✅ Thêm use case **"Xem chi tiết đơn hàng"**
5. ✅ Thêm use case **"Admin - Cập nhật trạng thái đơn hàng"**
6. ✅ Thêm use case **"Admin - Khóa/Mở khóa tài khoản"**
7. ✅ Thêm use case **"Admin - Upload hình ảnh sản phẩm"**

### Ưu tiên trung bình (⭐⭐⭐⭐):
8. ✅ Thêm use case **"Viết đánh giá sản phẩm"**
9. ✅ Thêm use case **"Lọc sản phẩm"**
10. ✅ Thêm use case **"Theo dõi đơn hàng"**
11. ✅ Thêm use case **"Gửi lại email xác thực"**

---

## 💡 GỢI Ý VẼ LẠI

Tôi khuyến nghị bạn:
1. **Giữ nguyên** diagram tổng quan hiện tại (hình 1)
2. **Vẽ lại** các diagram chi tiết (hình 2) theo 9 diagrams tôi đề xuất ở trên
3. **Thêm** các use case còn thiếu vào đúng diagram
4. **Sử dụng** include/extend relationship cho các use case phức tạp

Bạn có muốn tôi vẽ lại Use Case Diagrams hoàn chỉnh cho bạn không?
