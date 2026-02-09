# Hướng Dẫn Vẽ 13 Use Case Diagrams Chi Tiết - ShopOMG

## 📋 TỔNG QUAN

Tài liệu này hướng dẫn chi tiết cách vẽ **13 Use Case Diagrams** cho dự án ShopOMG, dựa trên code thực tế và Use Case Diagram tổng quan.

### Actors trong hệ thống:
1. **Khách hàng** (Customer/User) - Người dùng thường
2. **Admin** - Quản trị viên hệ thống

---

## 📊 DANH SÁCH 13 USE CASE DIAGRAMS

### **Nhóm Khách Hàng (Customer):**
1. Đăng nhập (Login)
2. Đăng ký (Register)
3. Quên/Đặt lại mật khẩu (Password Reset)
4. Xác thực Email (Email Verification)
5. Tài khoản (Account Management)
6. Đơn hàng (Order Management)
7. Giỏ hàng (Shopping Cart)
8. Sản phẩm (Product Browsing)

### **Nhóm Admin:**
9. Quản lý Tài khoản (User Management)
10. Quản lý Đơn hàng (Order Management)
11. Quản lý Sản phẩm (Product Management)
12. Quản lý Danh mục (Category Management)
13. Dashboard (Admin Dashboard)

---

# PHẦN 1: USE CASES CHO KHÁCH HÀNG

---

## 1️⃣ USE CASE DIAGRAM: ĐĂNG NHẬP (LOGIN)

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Đăng nhập** (Login)
- **Mô tả:** Người dùng đăng nhập vào hệ thống
- **Precondition:** Đã có tài khoản
- **Postcondition:** Được chuyển đến trang chủ/dashboard
- **Main Flow:**
  1. Người dùng truy cập `/login`
  2. Nhập email và password
  3. Hệ thống kiểm tra thông tin
  4. Nếu đúng → Đăng nhập thành công
  5. Nếu sai → Tăng số lần đăng nhập sai

#### **2. Đăng nhập bằng Facebook** (Login with Facebook)
- **Mô tả:** Đăng nhập qua OAuth2 Facebook
- **Relationship:** `<<include>>` Đăng nhập
- **Main Flow:**
  1. Click "Đăng nhập bằng Facebook"
  2. Chuyển hướng đến Facebook
  3. Xác thực trên Facebook
  4. Quay lại hệ thống với thông tin user

#### **3. Đăng nhập bằng Google** (Login with Google)
- **Mô tả:** Đăng nhập qua OAuth2 Google
- **Relationship:** `<<include>>` Đăng nhập
- **Main Flow:** Tương tự Facebook

#### **4. Kiểm tra số lần đăng nhập sai** (Check Login Attempts)
- **Mô tả:** Theo dõi số lần đăng nhập thất bại
- **Relationship:** `<<include>>` Đăng nhập
- **Main Flow:**
  1. Mỗi lần đăng nhập sai → Tăng counter
  2. Nếu counter >= 5 → Khóa tài khoản 15 phút
  3. Đăng nhập thành công → Reset counter về 0

#### **5. Đăng xuất** (Logout)
- **Mô tả:** Người dùng đăng xuất khỏi hệ thống
- **Main Flow:**
  1. Click "Đăng xuất"
  2. Xóa session
  3. Chuyển về trang login

### **Relationships:**
```
Đăng nhập
  ├── <<include>> Kiểm tra số lần đăng nhập sai
  ├── <<extend>> Đăng nhập bằng Facebook
  └── <<extend>> Đăng nhập bằng Google
```

### **Code liên quan:**
- `AuthController.java` - `/login`
- `LoginAttemptService.java`
- `CustomOAuth2UserService.java`

---

## 2️⃣ USE CASE DIAGRAM: ĐĂNG KÝ (REGISTER)

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Đăng ký** (Register)
- **Mô tả:** Tạo tài khoản mới
- **Precondition:** Chưa có tài khoản
- **Postcondition:** Tài khoản được tạo, email verification được gửi
- **Main Flow:**
  1. Truy cập `/sign-up`
  2. Điền form: Email, Username, Password, Confirm Password
  3. Submit form
  4. Hệ thống validate dữ liệu
  5. Tạo tài khoản với `emailVerified = false`
  6. Gửi email verification
  7. Chuyển đến trang "Kiểm tra email"

#### **2. Kiểm tra mật khẩu mạnh** (Password Strength Validation)
- **Mô tả:** Validate độ mạnh của mật khẩu
- **Relationship:** `<<include>>` Đăng ký
- **Rules:**
  - Tối thiểu 8 ký tự
  - Có ít nhất 1 chữ hoa
  - Có ít nhất 1 chữ thường
  - Có ít nhất 1 số
  - Có ít nhất 1 ký tự đặc biệt

#### **3. Kiểm tra email/username trùng** (Check Duplicate)
- **Mô tả:** Kiểm tra email hoặc username đã tồn tại
- **Relationship:** `<<include>>` Đăng ký
- **Main Flow:**
  1. Query database kiểm tra email
  2. Query database kiểm tra username
  3. Nếu trùng → Báo lỗi

#### **4. Email Verification** (Xác thực Email)
- **Mô tả:** Gửi email xác thực sau khi đăng ký
- **Relationship:** `<<include>>` Đăng ký
- **Main Flow:**
  1. Tạo verification token (UUID)
  2. Lưu token vào database với thời hạn 24h
  3. Gửi email chứa link verification
  4. User click link → Xác thực thành công

### **Relationships:**
```
Đăng ký
  ├── <<include>> Kiểm tra mật khẩu mạnh
  ├── <<include>> Kiểm tra email/username trùng
  └── <<include>> Email Verification
```

### **Code liên quan:**
- `AccountController.java` - `/sign-up`
- `EmailVerificationService.java`
- `@StrongPassword` annotation

---

## 3️⃣ USE CASE DIAGRAM: QUÊN/ĐẶT LẠI MẬT KHẨU

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Quên mật khẩu** (Forgot Password)
- **Mô tả:** Yêu cầu reset mật khẩu
- **Precondition:** Đã có tài khoản
- **Main Flow:**
  1. Truy cập `/forgot-password`
  2. Nhập email
  3. Submit form
  4. Hệ thống tìm account theo email
  5. Tạo password reset token (thời hạn 1h)
  6. Gửi email chứa link reset
  7. Hiển thị thông báo "Kiểm tra email"

#### **2. Đặt lại mật khẩu** (Reset Password)
- **Mô tả:** Đặt mật khẩu mới sau khi nhận email
- **Precondition:** Đã nhận email reset password
- **Main Flow:**
  1. Click link trong email → `/reset-password?token=xxx`
  2. Hệ thống validate token:
     - Token tồn tại?
     - Token chưa hết hạn?
     - Token chưa được sử dụng?
  3. Hiển thị form nhập mật khẩu mới
  4. Nhập password mới + confirm password
  5. Validate password strength
  6. Hash password mới
  7. Cập nhật password trong database
  8. Đánh dấu token đã sử dụng
  9. Chuyển đến trang login

#### **3. Kiểm tra mật khẩu mạnh** (Password Strength Validation)
- **Relationship:** `<<include>>` Đặt lại mật khẩu
- **Rules:** Giống như ở Đăng ký

### **Relationships:**
```
Quên mật khẩu
  └── Gửi email reset password

Đặt lại mật khẩu
  ├── <<include>> Kiểm tra mật khẩu mạnh
  └── <<include>> Validate reset token
```

### **Code liên quan:**
- `PasswordResetController.java`
- `PasswordResetService.java`
- `PasswordResetToken` entity

---

## 4️⃣ USE CASE DIAGRAM: XÁC THỰC EMAIL

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Xác thực Email** (Verify Email)
- **Mô tả:** Xác thực email sau khi đăng ký
- **Precondition:** Đã đăng ký, nhận được email verification
- **Main Flow:**
  1. Click link trong email → `/verify-email?token=xxx`
  2. Hệ thống validate token:
     - Token tồn tại?
     - Token chưa hết hạn (24h)?
     - Token chưa được sử dụng?
  3. Cập nhật `emailVerified = true`
  4. Xóa token khỏi database
  5. Hiển thị trang "Xác thực thành công"
  6. Cho phép đăng nhập

#### **2. Gửi lại email xác thực** (Resend Verification Email)
- **Mô tả:** Gửi lại email nếu chưa nhận được hoặc hết hạn
- **Precondition:** Email chưa được verify
- **Main Flow:**
  1. Truy cập `/resend-verification`
  2. Nhập email
  3. Hệ thống kiểm tra:
     - Email tồn tại?
     - Email chưa được verify?
  4. Xóa token cũ (nếu có)
  5. Tạo token mới
  6. Gửi email mới
  7. Hiển thị "Email đã được gửi"

### **Relationships:**
```
Xác thực Email
  └── <<include>> Validate verification token

Gửi lại email xác thực
  └── Tạo token mới
```

### **Code liên quan:**
- `EmailVerificationController.java`
- `EmailVerificationService.java`
- `EmailVerificationToken` entity

---

## 5️⃣ USE CASE DIAGRAM: TÀI KHOẢN (ACCOUNT)

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Xem thông tin cá nhân** (View Profile)
- **Mô tả:** Xem thông tin tài khoản
- **Precondition:** Đã đăng nhập
- **Main Flow:**
  1. Truy cập `/account/profile`
  2. Load thông tin từ database
  3. Hiển thị: Avatar, Fullname, Email, Phone, Address

#### **2. Cập nhật thông tin cá nhân** (Update Profile)
- **Mô tả:** Chỉnh sửa thông tin tài khoản
- **Precondition:** Đã đăng nhập
- **Main Flow:**
  1. Truy cập `/account/profile`
  2. Click "Chỉnh sửa"
  3. Cập nhật: Fullname, Phone, Address
  4. Upload avatar (optional)
  5. Submit form
  6. Validate dữ liệu
  7. Lưu vào database
  8. Hiển thị thông báo thành công

#### **3. Upload Avatar** (Upload Avatar)
- **Mô tả:** Tải lên ảnh đại diện
- **Relationship:** `<<include>>` Cập nhật thông tin cá nhân
- **Main Flow:**
  1. Chọn file ảnh (JPG/PNG, < 2MB)
  2. Preview ảnh
  3. Upload lên server
  4. Lưu đường dẫn vào database
  5. Hiển thị avatar mới

#### **4. Xem đơn mua** (View My Orders)
- **Mô tả:** Xem danh sách đơn hàng của mình
- **Main Flow:**
  1. Truy cập `/account/orders`
  2. Load tất cả orders của user
  3. Hiển thị danh sách với: Order ID, Date, Status, Total

#### **5. Xem đánh giá sản phẩm** (View My Reviews)
- **Mô tả:** Xem các đánh giá đã viết
- **Main Flow:**
  1. Truy cập `/account/reviews`
  2. Load tất cả reviews của user
  3. Hiển thị: Product, Rating, Comment, Date

### **Relationships:**
```
Tài khoản
  ├── Xem thông tin cá nhân
  ├── Cập nhật thông tin cá nhân
  │   └── <<include>> Upload Avatar
  ├── Xem đơn mua
  └── Xem đánh giá sản phẩm
```

### **Code liên quan:**
- `AccountController.java` - `/profile`, `/update`

---

## 6️⃣ USE CASE DIAGRAM: ĐƠN HÀNG (ORDERS)

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Xem danh sách đơn của tôi** (View My Orders)
- **Mô tả:** Xem tất cả đơn hàng
- **Precondition:** Đã đăng nhập
- **Main Flow:**
  1. Truy cập `/account/orders`
  2. Load orders của user
  3. Sắp xếp theo ngày tạo (mới nhất trước)
  4. Hiển thị với tabs:
     - Tất cả
     - Chờ xác nhận (PENDING)
     - Đang giao (SHIPPING)
     - Đã giao (DELIVERED)
     - Đã hủy (CANCELLED)

#### **2. Xem chi tiết đơn của tôi** (View Order Details)
- **Mô tả:** Xem chi tiết một đơn hàng
- **Main Flow:**
  1. Click vào đơn hàng
  2. Load chi tiết order
  3. Hiển thị:
     - Thông tin sản phẩm (tên, giá, số lượng)
     - Địa chỉ giao hàng
     - Phương thức thanh toán
     - Trạng thái hiện tại
     - Timeline trạng thái

#### **3. Thanh toán/Đặt hàng** (Checkout)
- **Mô tả:** Tạo đơn hàng mới từ giỏ hàng
- **Precondition:** Đã đăng nhập, giỏ hàng có sản phẩm
- **Main Flow:**
  1. Từ giỏ hàng → Click "Thanh toán"
  2. Truy cập `/checkout`
  3. Điền thông tin:
     - Họ tên
     - Số điện thoại
     - Địa chỉ giao hàng
  4. Chọn phương thức thanh toán:
     - COD (Thanh toán khi nhận hàng)
     - VNPay
     - MoMo
  5. Xem lại đơn hàng
  6. Xác nhận đặt hàng
  7. Tạo order trong database
  8. Xóa giỏ hàng
  9. Gửi email xác nhận

#### **4. Hủy đơn hàng** (Cancel Order)
- **Mô tả:** Hủy đơn hàng đang chờ xác nhận
- **Precondition:** Order có status = PENDING
- **Main Flow:**
  1. Vào chi tiết đơn hàng
  2. Click "Hủy đơn hàng"
  3. Nhập lý do hủy
  4. Xác nhận hủy
  5. Cập nhật status = CANCELLED
  6. Hoàn lại số lượng tồn kho
  7. Gửi thông báo cho admin

#### **5. Theo dõi trạng thái đơn hàng** (Track Order Status)
- **Mô tả:** Xem timeline trạng thái đơn hàng
- **Main Flow:**
  1. Vào chi tiết đơn hàng
  2. Hiển thị timeline:
     - ✅ Đã đặt hàng (PENDING)
     - ✅ Đã xác nhận (CONFIRMED)
     - ⏳ Đang giao (SHIPPING)
     - ⏳ Đã giao (DELIVERED)
  3. Hiển thị thời gian của mỗi trạng thái

### **Relationships:**
```
Đơn hàng
  ├── Xem danh sách đơn của tôi
  ├── Xem chi tiết đơn của tôi
  │   ├── Theo dõi trạng thái đơn hàng
  │   └── Hủy đơn hàng (nếu PENDING)
  └── Thanh toán/Đặt hàng
      ├── <<include>> Nhập địa chỉ giao hàng
      └── <<include>> Chọn phương thức thanh toán
```

### **Code liên quan:**
- `AccountController.java` - `/orders`
- `HomeController.java` - `/checkout`

---

## 7️⃣ USE CASE DIAGRAM: GIỎ HÀNG (CART)

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Xem giỏ hàng** (View Cart)
- **Mô tả:** Xem tất cả sản phẩm trong giỏ
- **Main Flow:**
  1. Truy cập `/cart`
  2. Load cart từ session
  3. Hiển thị danh sách items:
     - Hình ảnh sản phẩm
     - Tên sản phẩm
     - Giá
     - Số lượng
     - Tổng tiền từng item
  4. Hiển thị tổng tiền toàn bộ giỏ hàng

#### **2. Thêm vào giỏ** (Add to Cart)
- **Mô tả:** Thêm sản phẩm vào giỏ hàng
- **Main Flow:**
  1. Từ trang sản phẩm → Click "Thêm vào giỏ"
  2. Chọn số lượng
  3. Kiểm tra session:
     - Nếu chưa có session → Tạo mới
     - Nếu đã có → Lấy cart hiện tại
  4. Kiểm tra sản phẩm đã có trong giỏ?
     - Có → Cập nhật số lượng
     - Không → Thêm item mới
  5. Tính lại tổng tiền
  6. Lưu cart vào session
  7. Cập nhật badge số lượng giỏ hàng
  8. Hiển thị thông báo "Đã thêm vào giỏ"

#### **3. Xóa item** (Remove from Cart)
- **Mô tả:** Xóa sản phẩm khỏi giỏ hàng
- **Main Flow:**
  1. Trong giỏ hàng → Click "Xóa"
  2. Xác nhận xóa
  3. Xóa item khỏi cart
  4. Tính lại tổng tiền
  5. Cập nhật session
  6. Làm mới trang giỏ hàng

#### **4. Cập nhật số lượng** (Update Cart Quantity)
- **Mô tả:** Thay đổi số lượng sản phẩm trong giỏ
- **Main Flow:**
  1. Thay đổi số lượng (input number hoặc +/-)
  2. Kiểm tra số lượng > 0
  3. Kiểm tra số lượng <= tồn kho
  4. Cập nhật số lượng trong cart
  5. Tính lại tổng tiền
  6. Cập nhật session
  7. Làm mới giá tiền hiển thị

#### **5. Thanh toán** (Proceed to Checkout)
- **Mô tả:** Chuyển đến trang thanh toán
- **Precondition:** Giỏ hàng có sản phẩm
- **Main Flow:**
  1. Click "Thanh toán"
  2. Kiểm tra đã đăng nhập?
     - Chưa → Chuyển đến `/login`
     - Đã → Chuyển đến `/checkout`

### **Relationships:**
```
Giỏ hàng
  ├── Xem giỏ hàng
  ├── Thêm vào giỏ
  ├── Xóa item
  ├── Cập nhật số lượng
  └── Thanh toán
      └── <<include>> Kiểm tra đăng nhập
```

### **Code liên quan:**
- `HomeController.java` - `/cart`
- Session management

---

## 8️⃣ USE CASE DIAGRAM: SẢN PHẨM (PRODUCTS)

### **Actor:** Khách hàng

### **Use Cases chính:**

#### **1. Xem danh sách sản phẩm** (Browse Products)
- **Mô tả:** Duyệt tất cả sản phẩm
- **Main Flow:**
  1. Truy cập `/products`
  2. Load sản phẩm từ database
  3. Hiển thị dạng lưới (grid)
  4. Mỗi sản phẩm hiển thị:
     - Hình ảnh
     - Tên
     - Giá
     - Nút "Thêm vào giỏ"

#### **2. Xem chi tiết sản phẩm** (View Product Details)
- **Mô tả:** Xem thông tin chi tiết sản phẩm
- **Main Flow:**
  1. Click vào sản phẩm
  2. Truy cập `/product/{id}`
  3. Load thông tin sản phẩm
  4. Load hình ảnh sản phẩm
  5. Load đánh giá sản phẩm
  6. Load sản phẩm liên quan
  7. Hiển thị:
     - Hình ảnh (có thể xem nhiều ảnh)
     - Tên, mô tả
     - Giá
     - Số lượng còn lại
     - Đánh giá trung bình
     - Danh sách đánh giá
     - Sản phẩm liên quan

#### **3. Tìm kiếm sản phẩm** (Search Products)
- **Mô tả:** Tìm kiếm sản phẩm theo từ khóa
- **Main Flow:**
  1. Nhập từ khóa vào ô tìm kiếm
  2. Submit search
  3. Query database:
     - `WHERE name LIKE '%keyword%'`
     - `OR description LIKE '%keyword%'`
  4. Hiển thị kết quả
  5. Highlight từ khóa tìm kiếm

#### **4. Lọc theo danh mục** (Filter by Category)
- **Mô tả:** Lọc sản phẩm theo danh mục
- **Main Flow:**
  1. Chọn danh mục từ menu/sidebar
  2. Query: `WHERE category_id = ?`
  3. Hiển thị sản phẩm của danh mục đó

#### **5. Lọc theo giá** (Filter by Price Range)
- **Mô tả:** Lọc sản phẩm theo khoảng giá
- **Main Flow:**
  1. Chọn khoảng giá (slider hoặc dropdown)
  2. Query: `WHERE price BETWEEN min AND max`
  3. Hiển thị kết quả

#### **6. Sắp xếp sản phẩm** (Sort Products)
- **Mô tả:** Sắp xếp sản phẩm theo tiêu chí
- **Main Flow:**
  1. Chọn tiêu chí sắp xếp:
     - Giá: Thấp → Cao
     - Giá: Cao → Thấp
     - Tên: A → Z
     - Mới nhất
  2. Query với ORDER BY
  3. Hiển thị kết quả đã sắp xếp

#### **7. Phân trang** (Pagination)
- **Mô tả:** Chia sản phẩm thành nhiều trang
- **Main Flow:**
  1. Hiển thị 20 sản phẩm/trang
  2. Hiển thị số trang
  3. Click trang → Load sản phẩm của trang đó

### **Relationships:**
```
Sản phẩm
  ├── Xem danh sách sản phẩm
  │   ├── <<include>> Phân trang
  │   ├── <<extend>> Tìm kiếm sản phẩm
  │   ├── <<extend>> Lọc theo danh mục
  │   ├── <<extend>> Lọc theo giá
  │   └── <<extend>> Sắp xếp sản phẩm
  └── Xem chi tiết sản phẩm
      └── Thêm vào giỏ hàng
```

### **Code liên quan:**
- `HomeController.java` - `/products`, `/product/{id}`

---

# PHẦN 2: USE CASES CHO ADMIN

---

## 9️⃣ USE CASE DIAGRAM: QUẢN LÝ TÀI KHOẢN (ADMIN)

### **Actor:** Admin

### **Use Cases chính:**

#### **1. Xem danh sách người dùng** (View Users)
- **Mô tả:** Xem tất cả tài khoản
- **Precondition:** Đã đăng nhập với role ADMIN
- **Main Flow:**
  1. Truy cập `/admin/accounts`
  2. Load tất cả accounts
  3. Hiển thị danh sách với:
     - Avatar
     - Username, Email
     - Role (USER/ADMIN)
     - Status (Active/Inactive)
     - Ngày tạo
     - Số lần đăng nhập sai
     - Thời gian khóa (nếu có)

#### **2. Tìm kiếm người dùng** (Search Users)
- **Mô tả:** Tìm kiếm theo tên/email/phone
- **Main Flow:**
  1. Nhập từ khóa
  2. Query: `WHERE username LIKE '%keyword%' OR email LIKE '%keyword%'`
  3. Hiển thị kết quả

#### **3. Xem chi tiết người dùng** (View User Details)
- **Mô tả:** Xem thông tin chi tiết user
- **Main Flow:**
  1. Click vào user
  2. Hiển thị:
     - Thông tin cá nhân
     - Lịch sử đơn hàng
     - Số lần đăng nhập thất bại
     - Thời gian khóa tài khoản

#### **4. Khóa tài khoản** (Lock Account)
- **Mô tả:** Khóa tài khoản người dùng
- **Main Flow:**
  1. Click "Khóa tài khoản"
  2. Xác nhận
  3. Cập nhật `isActive = false`
  4. Đăng xuất user khỏi hệ thống
  5. Ghi log hành động
  6. Gửi email thông báo cho user

#### **5. Mở khóa tài khoản** (Unlock Account)
- **Mô tả:** Mở khóa tài khoản đã bị khóa
- **Main Flow:**
  1. Click "Mở khóa"
  2. Xác nhận
  3. Cập nhật `isActive = true`
  4. Reset `failed_login_attempts = 0`
  5. Xóa `account_locked_until`
  6. Ghi log hành động
  7. Gửi email thông báo cho user

#### **6. Reset mật khẩu người dùng** (Reset User Password)
- **Mô tả:** Tạo mật khẩu mới cho user
- **Main Flow:**
  1. Click "Reset mật khẩu"
  2. Tạo mật khẩu tạm thời (random)
  3. Hash mật khẩu
  4. Cập nhật trong database
  5. Đặt cờ "Bắt buộc đổi mật khẩu"
  6. Ghi log
  7. Gửi email mật khẩu tạm thời cho user

#### **7. Xóa tài khoản** (Delete Account)
- **Mô tả:** Xóa tài khoản người dùng
- **Precondition:** Tài khoản không có đơn hàng
- **Main Flow:**
  1. Click "Xóa"
  2. Kiểm tra có đơn hàng?
     - Có → Báo lỗi, gợi ý khóa thay vì xóa
     - Không → Tiếp tục
  3. Xác nhận xóa
  4. Xóa tài khoản
  5. Xóa dữ liệu liên quan (reviews, tokens)
  6. Ghi log

### **Relationships:**
```
Quản lý Tài khoản
  ├── Xem danh sách người dùng
  │   └── <<extend>> Tìm kiếm người dùng
  ├── Xem chi tiết người dùng
  ├── Khóa tài khoản
  ├── Mở khóa tài khoản
  ├── Reset mật khẩu người dùng
  └── Xóa tài khoản
      └── <<include>> Kiểm tra có đơn hàng
```

### **Code liên quan:**
- `AdminController.java` - `/admin/accounts`

---

## 🔟 USE CASE DIAGRAM: QUẢN LÝ ĐƠN HÀNG (ADMIN)

### **Actor:** Admin

### **Use Cases chính:**

#### **1. Xem danh sách đơn hàng** (View Orders)
- **Mô tả:** Xem tất cả đơn hàng
- **Main Flow:**
  1. Truy cập `/admin/orders`
  2. Load tất cả orders
  3. Sắp xếp theo ngày tạo (mới nhất trước)
  4. Hiển thị:
     - Order ID
     - Khách hàng
     - Ngày đặt
     - Tổng tiền
     - Trạng thái
     - Phương thức thanh toán

#### **2. Lọc đơn hàng theo trạng thái** (Filter by Status)
- **Mô tả:** Lọc theo PENDING/CONFIRMED/SHIPPING/DELIVERED/CANCELLED
- **Main Flow:**
  1. Chọn tab trạng thái
  2. Query: `WHERE status = ?`
  3. Hiển thị kết quả

#### **3. Xem chi tiết đơn hàng** (View Order Details)
- **Mô tả:** Xem thông tin chi tiết đơn hàng
- **Main Flow:**
  1. Click vào đơn hàng
  2. Hiển thị:
     - Thông tin khách hàng
     - Địa chỉ giao hàng
     - Danh sách sản phẩm
     - Tổng tiền
     - Phương thức thanh toán
     - Trạng thái hiện tại
     - Timeline trạng thái

#### **4. Cập nhật trạng thái đơn hàng** (Update Order Status)
- **Mô tả:** Thay đổi trạng thái đơn hàng
- **Main Flow:**
  1. Trong chi tiết đơn hàng
  2. Chọn trạng thái mới:
     - PENDING → CONFIRMED
     - CONFIRMED → SHIPPING
     - SHIPPING → DELIVERED
  3. Xác nhận
  4. Cập nhật status trong database
  5. Ghi log thời gian thay đổi
  6. Gửi email thông báo cho khách hàng

#### **5. Hủy đơn hàng** (Cancel Order)
- **Mô tả:** Hủy đơn hàng (admin)
- **Main Flow:**
  1. Click "Hủy đơn hàng"
  2. Nhập lý do hủy
  3. Xác nhận
  4. Cập nhật status = CANCELLED
  5. Hoàn lại tồn kho
  6. Ghi log
  7. Gửi email thông báo cho khách hàng

#### **6. In hóa đơn** (Print Invoice)
- **Mô tả:** In hóa đơn đơn hàng
- **Main Flow:**
  1. Click "In hóa đơn"
  2. Tạo PDF hóa đơn
  3. Hiển thị preview
  4. In hoặc download

### **Relationships:**
```
Quản lý Đơn hàng
  ├── Xem danh sách đơn hàng
  │   └── <<extend>> Lọc đơn hàng theo trạng thái
  ├── Xem chi tiết đơn hàng
  ├── Cập nhật trạng thái đơn hàng
  │   └── <<include>> Gửi email thông báo
  ├── Hủy đơn hàng
  │   └── <<include>> Hoàn lại tồn kho
  └── In hóa đơn
```

### **Code liên quan:**
- `AdminController.java` - `/admin/orders`

---

## 1️⃣1️⃣ USE CASE DIAGRAM: QUẢN LÝ SẢN PHẨM (ADMIN)

### **Actor:** Admin

### **Use Cases chính:**

#### **1. Xem danh sách sản phẩm** (View Products)
- **Mô tả:** Xem tất cả sản phẩm
- **Main Flow:**
  1. Truy cập `/admin/products`
  2. Load tất cả products
  3. Hiển thị:
     - Hình ảnh
     - Tên sản phẩm
     - Danh mục
     - Giá
     - Số lượng tồn kho
     - Trạng thái (Active/Inactive)

#### **2. Tìm kiếm sản phẩm** (Search Products)
- **Mô tả:** Tìm kiếm theo tên/mô tả
- **Main Flow:**
  1. Nhập từ khóa
  2. Query database
  3. Hiển thị kết quả

#### **3. Tạo sản phẩm mới** (Create Product)
- **Mô tả:** Thêm sản phẩm mới
- **Main Flow:**
  1. Click "Thêm sản phẩm"
  2. Truy cập `/admin/products/create`
  3. Điền form:
     - Tên sản phẩm
     - Mô tả
     - Giá
     - Số lượng
     - Chọn danh mục
     - Upload hình ảnh (nhiều ảnh)
  4. Submit form
  5. Validate dữ liệu
  6. Upload hình ảnh lên server
  7. Lưu sản phẩm vào database
  8. Lưu thông tin hình ảnh vào `product_images`
  9. Hiển thị thông báo thành công

#### **4. Upload hình ảnh sản phẩm** (Upload Product Images)
- **Mô tả:** Tải lên nhiều hình ảnh cho sản phẩm
- **Relationship:** `<<include>>` Tạo sản phẩm mới, Cập nhật sản phẩm
- **Main Flow:**
  1. Chọn nhiều file ảnh (JPG/PNG, < 5MB mỗi ảnh)
  2. Validate file
  3. Preview ảnh
  4. Upload lên server
  5. Tạo đường dẫn cho từng ảnh
  6. Lưu vào `product_images` table

#### **5. Cập nhật sản phẩm** (Update Product)
- **Mô tả:** Chỉnh sửa thông tin sản phẩm
- **Main Flow:**
  1. Click "Sửa" trên sản phẩm
  2. Load thông tin hiện tại
  3. Hiển thị form với dữ liệu đã điền
  4. Chỉnh sửa thông tin
  5. Upload/xóa hình ảnh
  6. Submit
  7. Validate
  8. Cập nhật database
  9. Hiển thị thông báo

#### **6. Xóa sản phẩm** (Delete Product)
- **Mô tả:** Xóa sản phẩm khỏi hệ thống
- **Precondition:** Sản phẩm chưa có trong đơn hàng nào
- **Main Flow:**
  1. Click "Xóa"
  2. Kiểm tra sản phẩm có trong đơn hàng?
     - Có → Báo lỗi
     - Không → Tiếp tục
  3. Xác nhận xóa
  4. Xóa hình ảnh khỏi server
  5. Xóa records trong `product_images`
  6. Xóa sản phẩm
  7. Hiển thị thông báo

### **Relationships:**
```
Quản lý Sản phẩm
  ├── Xem danh sách sản phẩm
  │   └── <<extend>> Tìm kiếm sản phẩm
  ├── Tạo sản phẩm mới
  │   ├── <<include>> Upload hình ảnh sản phẩm
  │   └── <<include>> Chọn danh mục
  ├── Cập nhật sản phẩm
  │   └── <<include>> Upload hình ảnh sản phẩm
  └── Xóa sản phẩm
      └── <<include>> Kiểm tra có trong đơn hàng
```

### **Code liên quan:**
- `AdminController.java` - `/admin/products`

---

## 1️⃣2️⃣ USE CASE DIAGRAM: QUẢN LÝ DANH MỤC (ADMIN)

### **Actor:** Admin

### **Use Cases chính:**

#### **1. Xem danh sách danh mục** (View Categories)
- **Mô tả:** Xem tất cả danh mục
- **Main Flow:**
  1. Truy cập `/admin/categories`
  2. Load tất cả categories
  3. Sắp xếp theo tên
  4. Hiển thị:
     - Icon danh mục
     - Tên danh mục
     - Mô tả
     - Số sản phẩm
     - Ngày tạo

#### **2. Tạo danh mục mới** (Create Category)
- **Mô tả:** Thêm danh mục mới
- **Main Flow:**
  1. Click "Thêm danh mục"
  2. Điền form:
     - Tên danh mục
     - Mô tả
     - Icon (optional)
  3. Submit
  4. Validate:
     - Tên không trống
     - Tên chưa tồn tại
  5. Lưu vào database
  6. Hiển thị thông báo

#### **3. Cập nhật danh mục** (Update Category)
- **Mô tả:** Chỉnh sửa danh mục
- **Main Flow:**
  1. Click "Sửa"
  2. Load thông tin hiện tại
  3. Chỉnh sửa
  4. Submit
  5. Validate
  6. Cập nhật database

#### **4. Xóa danh mục** (Delete Category)
- **Mô tả:** Xóa danh mục
- **Precondition:** Danh mục không có sản phẩm
- **Main Flow:**
  1. Click "Xóa"
  2. Kiểm tra có sản phẩm?
     - Có → Báo lỗi "Không thể xóa danh mục có sản phẩm"
     - Không → Tiếp tục
  3. Xác nhận xóa
  4. Xóa danh mục
  5. Hiển thị thông báo

### **Relationships:**
```
Quản lý Danh mục
  ├── Xem danh sách danh mục
  ├── Tạo danh mục mới
  │   └── <<include>> Validate tên trùng
  ├── Cập nhật danh mục
  └── Xóa danh mục
      └── <<include>> Kiểm tra có sản phẩm
```

### **Code liên quan:**
- `AdminController.java` - `/admin/categories`

---

## 1️⃣3️⃣ USE CASE DIAGRAM: DASHBOARD ADMIN

### **Actor:** Admin

### **Use Cases chính:**

#### **1. Xem tổng quan thống kê** (View Statistics)
- **Mô tả:** Xem các số liệu thống kê tổng quan
- **Main Flow:**
  1. Truy cập `/admin/dashboard`
  2. Load và hiển thị:
     - **Tổng số sản phẩm** (COUNT products)
     - **Tổng số đơn hàng** (COUNT orders)
     - **Tổng số người dùng** (COUNT accounts)
     - **Tổng doanh thu** (SUM order total WHERE status = DELIVERED)
     - **Doanh thu tháng này**
     - **Số đơn hàng chờ xử lý** (WHERE status = PENDING)

#### **2. Xem đơn hàng gần đây** (View Recent Orders)
- **Mô tả:** Xem 10 đơn hàng mới nhất
- **Main Flow:**
  1. Query: `SELECT * FROM orders ORDER BY created_date DESC LIMIT 10`
  2. Hiển thị danh sách với:
     - Order ID
     - Khách hàng
     - Tổng tiền
     - Trạng thái
     - Thời gian

#### **3. Xem hoạt động gần đây** (View Recent Activities)
- **Mô tả:** Xem log các hoạt động gần đây
- **Main Flow:**
  1. Load activity logs
  2. Hiển thị:
     - Thời gian
     - Admin thực hiện
     - Hành động (Create/Update/Delete)
     - Đối tượng (Product/Order/User)

#### **4. Xem biểu đồ doanh thu** (View Revenue Chart)
- **Mô tả:** Xem biểu đồ doanh thu theo thời gian
- **Main Flow:**
  1. Chọn khoảng thời gian (7 ngày/30 ngày/12 tháng)
  2. Query doanh thu theo ngày/tháng
  3. Hiển thị biểu đồ line chart

#### **5. Xem sản phẩm bán chạy** (View Top Products)
- **Mô tả:** Xem top 10 sản phẩm bán chạy nhất
- **Main Flow:**
  1. Query: `SELECT product_id, SUM(quantity) FROM order_items GROUP BY product_id ORDER BY SUM DESC LIMIT 10`
  2. Hiển thị danh sách

### **Relationships:**
```
Dashboard
  ├── Xem tổng quan thống kê
  ├── Xem đơn hàng gần đây
  ├── Xem hoạt động gần đây
  ├── Xem biểu đồ doanh thu
  └── Xem sản phẩm bán chạy
```

### **Code liên quan:**
- `AdminController.java` - `/admin/dashboard`

---

## 📝 HƯỚNG DẪN VẼ USE CASE DIAGRAM

### Bước 1: Chuẩn bị
1. Sử dụng công cụ: Draw.io, Lucidchart, hoặc PlantUML
2. Tạo 13 diagrams riêng biệt hoặc 1 file với 13 pages

### Bước 2: Vẽ từng Diagram

#### Các thành phần cần vẽ:
1. **Actor** (hình người que):
   - Khách hàng (Customer)
   - Admin

2. **Use Case** (hình oval):
   - Tên use case bên trong oval
   - Đặt tên rõ ràng, ngắn gọn

3. **System Boundary** (hình chữ nhật):
   - Bao quanh tất cả use cases
   - Đặt tên hệ thống ở góc trên: "ShopOMG System"

4. **Relationships** (các mũi tên):
   - **Association** (nét liền): Actor → Use Case
   - **Include** (nét đứt + `<<include>>`): Use Case A → Use Case B (A bắt buộc gọi B)
   - **Extend** (nét đứt + `<<extend>>`): Use Case B → Use Case A (B mở rộng A, không bắt buộc)

### Bước 3: Màu sắc & Layout
- **Actor:** Màu xanh dương
- **Use Case chính:** Màu xanh lá nhạt
- **Use Case phụ (include/extend):** Màu vàng nhạt
- **System Boundary:** Màu xám nhạt

### Bước 4: Kiểm tra
- ✅ Tất cả use cases đều có actor liên kết
- ✅ Include/Extend relationships đúng hướng
- ✅ Tên use cases rõ ràng, dễ hiểu
- ✅ Không có use case nào bị thiếu

---

## 🎯 CHECKLIST HOÀN THÀNH

### Khách hàng (8 diagrams):
- [ ] 1. Đăng nhập
- [ ] 2. Đăng ký
- [ ] 3. Quên/Đặt lại mật khẩu
- [ ] 4. Xác thực Email
- [ ] 5. Tài khoản
- [ ] 6. Đơn hàng
- [ ] 7. Giỏ hàng
- [ ] 8. Sản phẩm

### Admin (5 diagrams):
- [ ] 9. Quản lý Tài khoản
- [ ] 10. Quản lý Đơn hàng
- [ ] 11. Quản lý Sản phẩm
- [ ] 12. Quản lý Danh mục
- [ ] 13. Dashboard

---

## 💡 MẸO VẼ NHANH

1. **Vẽ Use Case chính trước**, sau đó mới vẽ include/extend
2. **Sắp xếp layout** theo chiều dọc hoặc ngang, tránh chồng chéo
3. **Nhóm các use cases liên quan** gần nhau
4. **Sử dụng màu sắc** để phân biệt use case chính và phụ
5. **Đặt tên rõ ràng**, tránh tên quá dài

---

## 📚 TÀI LIỆU THAM KHẢO

- Use Case Diagram tổng quan: Xem ảnh đã upload
- Code thực tế: `d:\UDPM_SpringBoot_PRO2113\src\main\java\poly\edu\controller\`
- Activity Diagrams: `ACTIVITY_DIAGRAMS_MERMAID.md`

---

**Chúc bạn vẽ thành công! 🎨**
