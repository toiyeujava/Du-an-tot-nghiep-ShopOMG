# HƯỚNG DẪN VẼ USE CASE DIAGRAMS - SHOPOMG (16 USE CASES)

## 📋 TỔNG QUAN

Tài liệu này hướng dẫn chi tiết cách vẽ **Use Case Diagram Tổng** và **16 Use Case Diagrams Chi tiết** cho dự án ShopOMG với 5 tác nhân.

### **5 Tác nhân trong hệ thống:**
1. **Admin** - Quản trị viên (toàn quyền)
2. **Nhân viên Bán hàng** (Sales Staff) - Xử lý đơn hàng
3. **Nhân viên Kho** (Warehouse Staff) - Quản lý tồn kho
4. **Khách hàng** (Customer) - Người mua hàng
5. **Khách vãng lai** (Guest) - Xem sản phẩm

### **16 Use Cases:**
- **UC-01 đến UC-08:** Khách hàng
- **UC-09 đến UC-13:** Admin
- **UC-14:** Nhân viên Bán hàng, Admin
- **UC-15:** Nhân viên Kho, Admin
- **UC-16:** Khách vãng lai

---

# PHẦN 1: USE CASE DIAGRAM TỔNG

## Mô tả

Sơ đồ tổng quan hiển thị **5 tác nhân** và **16 use cases chính** trong một hệ thống.

## Layout Đề xuất

```
                    [System Boundary: ShopOMG]
                    
    Admin ────────────┐                    ┌────────── Khách hàng
                      │                    │
    Sales Staff ──────┤   16 Use Cases    ├────────── Khách vãng lai
                      │                    │
    Warehouse ────────┘                    └────────── (empty)
```

## Các Use Cases Chính

### **Nhóm Xác thực & Tài khoản (UC-01 đến UC-05):**
1. **UC-01:** Đăng nhập
2. **UC-02:** Đăng ký  
3. **UC-03:** Quên/Đặt lại mật khẩu
4. **UC-04:** Xác thực Email
5. **UC-05:** Tài khoản cá nhân

### **Nhóm Mua sắm (UC-06 đến UC-08):**
6. **UC-06:** Đơn hàng của tôi
7. **UC-07:** Giỏ hàng
8. **UC-08:** Sản phẩm

### **Nhóm Quản trị (UC-09 đến UC-13):**
9. **UC-09:** Quản lý Tài khoản người dùng
10. **UC-10:** Quản lý Đơn hàng (Admin)
11. **UC-11:** Quản lý Sản phẩm
12. **UC-12:** Quản lý Danh mục
13. **UC-13:** Dashboard Admin

### **Nhóm Mới (UC-14 đến UC-16):**
14. **UC-14:** Hỗ trợ Khách hàng
15. **UC-15:** Quản lý Tồn kho
16. **UC-16:** Duyệt Sản phẩm (Guest)

## Ma trận Tác nhân - Use Case

| Use Case | Admin | Sales | Warehouse | Customer | Guest |
|----------|:-----:|:-----:|:---------:|:--------:|:-----:|
| UC-01: Đăng nhập | ✅ | ✅ | ✅ | ✅ | - |
| UC-02: Đăng ký | - | - | - | ✅ | ✅ |
| UC-03: Quên/Đặt lại MK | ✅ | ✅ | ✅ | ✅ | - |
| UC-04: Xác thực Email | - | - | - | ✅ | - |
| UC-05: Tài khoản | ✅ | ✅ | ✅ | ✅ | - |
| UC-06: Đơn hàng của tôi | - | - | - | ✅ | - |
| UC-07: Giỏ hàng | - | - | - | ✅ | - |
| UC-08: Sản phẩm | ✅ | ✅ | ✅ | ✅ | ✅ |
| UC-09: QL Tài khoản | ✅ | - | - | - | - |
| UC-10: QL Đơn hàng | ✅ | ✅ | - | - | - |
| UC-11: QL Sản phẩm | ✅ | - | - | - | - |
| UC-12: QL Danh mục | ✅ | - | - | - | - |
| UC-13: Dashboard | ✅ | - | - | - | - |
| UC-14: Hỗ trợ KH | ✅ | ✅ | - | - | - |
| UC-15: QL Tồn kho | ✅ | - | ✅ | - | - |
| UC-16: Duyệt SP | - | - | - | - | ✅ |

## Hướng dẫn vẽ trên Draw.io

### Bước 1: Tạo System Boundary
1. Vẽ hình chữ nhật lớn (Rectangle)
2. Label: "ShopOMG System"
3. Style: Đường viền đậm, nền trắng

### Bước 2: Vẽ 5 Actors
1. Kéo "Actor" từ thư viện UML
2. Đặt tên:
   - Bên trái: Admin, Sales Staff, Warehouse Staff
   - Bên phải: Khách hàng, Khách vãng lai
3. Style: Stick figure, font 12pt

### Bước 3: Vẽ 16 Use Cases
1. Kéo "Use Case" (oval) từ thư viện
2. Sắp xếp theo nhóm chức năng
3. Label: Tên use case (tiếng Việt)
4. Style: Oval, font 11pt, căn giữa

### Bước 4: Vẽ Associations
1. Kéo đường thẳng từ Actor đến Use Case
2. Không có mũi tên
3. Style: Solid line, không label

---

# PHẦN 2: 16 USE CASE DIAGRAMS CHI TIẾT

---

## UC-01: ĐĂNG NHẬP (LOGIN)

### **Actors:** Admin, Sales Staff, Warehouse Staff, Khách hàng

### **Use Cases:**

#### **1. Đăng nhập** (Main)
- Người dùng đăng nhập vào hệ thống

#### **2. Kiểm tra số lần đăng nhập sai**
- **Relationship:** `<<include>>` Đăng nhập
- Bắt buộc kiểm tra mỗi lần đăng nhập

#### **3. Đăng nhập bằng Facebook**
- **Relationship:** `<<extend>>` Đăng nhập
- Tùy chọn, thay thế đăng nhập thường

#### **4. Đăng nhập bằng Google**
- **Relationship:** `<<extend>>` Đăng nhập
- Tùy chọn, thay thế đăng nhập thường

#### **5. Đăng xuất**
- Use case độc lập

### **Diagram Structure:**
```
Actor ──────> [Đăng nhập]
                  │
                  ├──<<include>>──> [Kiểm tra số lần đăng nhập sai]
                  │
                  ├──<<extend>>──── [Đăng nhập bằng Facebook]
                  │
                  └──<<extend>>──── [Đăng nhập bằng Google]

Actor ──────> [Đăng xuất]
```

### **Hướng dẫn vẽ:**
1. Vẽ oval "Đăng nhập" ở giữa
2. Vẽ oval "Kiểm tra số lần đăng nhập sai" bên phải
3. Vẽ mũi tên nét đứt từ "Đăng nhập" → "Kiểm tra..."
4. Label: `<<include>>`
5. Vẽ 2 oval "Đăng nhập Facebook/Google" bên dưới
6. Vẽ mũi tên nét đứt từ "Đăng nhập Facebook/Google" → "Đăng nhập"
7. Label: `<<extend>>`

---

## UC-02: ĐĂNG KÝ (REGISTER)

### **Actors:** Khách hàng, Khách vãng lai

### **Use Cases:**

#### **1. Đăng ký** (Main)
- Tạo tài khoản mới

#### **2. Kiểm tra mật khẩu mạnh**
- **Relationship:** `<<include>>` Đăng ký
- Bắt buộc validate mật khẩu

#### **3. Kiểm tra email/username trùng**
- **Relationship:** `<<include>>` Đăng ký
- Bắt buộc kiểm tra trùng lặp

#### **4. Email Verification**
- **Relationship:** `<<include>>` Đăng ký
- Bắt buộc gửi email xác thực

### **Diagram Structure:**
```
Actor ──────> [Đăng ký]
                  │
                  ├──<<include>>──> [Kiểm tra mật khẩu mạnh]
                  │
                  ├──<<include>>──> [Kiểm tra email/username trùng]
                  │
                  └──<<include>>──> [Email Verification]
```

### **Hướng dẫn vẽ:**
1. Vẽ oval "Đăng ký" ở giữa
2. Vẽ 3 oval bên phải cho các chức năng include
3. Vẽ mũi tên nét đứt từ "Đăng ký" → mỗi chức năng
4. Label tất cả: `<<include>>`

---

## UC-03: QUÊN/ĐẶT LẠI MẬT KHẨU

### **Actors:** Admin, Sales Staff, Warehouse Staff, Khách hàng

### **Use Cases:**

#### **1. Quên mật khẩu** (Main)
- Yêu cầu reset mật khẩu

#### **2. Gửi email reset password**
- **Relationship:** `<<include>>` Quên mật khẩu
- Bắt buộc gửi email

#### **3. Đặt lại mật khẩu** (Main)
- Đặt mật khẩu mới

#### **4. Validate reset token**
- **Relationship:** `<<include>>` Đặt lại mật khẩu
- Bắt buộc kiểm tra token

#### **5. Kiểm tra mật khẩu mạnh**
- **Relationship:** `<<include>>` Đặt lại mật khẩu
- Bắt buộc validate mật khẩu mới

### **Diagram Structure:**
```
Actor ──────> [Quên mật khẩu]
                  │
                  └──<<include>>──> [Gửi email reset password]

Actor ──────> [Đặt lại mật khẩu]
                  │
                  ├──<<include>>──> [Validate reset token]
                  │
                  └──<<include>>──> [Kiểm tra mật khẩu mạnh]
```

---

## UC-04: XÁC THỰC EMAIL

### **Actors:** Khách hàng

### **Use Cases:**

#### **1. Xác thực Email** (Main)
- Xác thực email sau đăng ký

#### **2. Validate verification token**
- **Relationship:** `<<include>>` Xác thực Email
- Bắt buộc kiểm tra token

#### **3. Gửi lại email xác thực** (Main)
- Gửi lại email nếu hết hạn

#### **4. Tạo token mới**
- **Relationship:** `<<include>>` Gửi lại email
- Bắt buộc tạo token mới

### **Diagram Structure:**
```
Actor ──────> [Xác thực Email]
                  │
                  └──<<include>>──> [Validate verification token]

Actor ──────> [Gửi lại email xác thực]
                  │
                  └──<<include>>──> [Tạo token mới]
```

---

## UC-05: TÀI KHOẢN CÁ NHÂN

### **Actors:** Admin, Sales Staff, Warehouse Staff, Khách hàng

### **Use Cases:**

#### **1. Xem thông tin cá nhân** (Main)
- Xem profile

#### **2. Cập nhật thông tin cá nhân** (Main)
- Chỉnh sửa thông tin

#### **3. Upload Avatar**
- **Relationship:** `<<extend>>` Cập nhật thông tin
- Tùy chọn upload ảnh

#### **4. Xem đơn mua** (Main - chỉ Customer)
- Xem danh sách đơn hàng

#### **5. Xem đánh giá** (Main - chỉ Customer)
- Xem reviews đã viết

### **Diagram Structure:**
```
Actor ──────> [Xem thông tin cá nhân]

Actor ──────> [Cập nhật thông tin cá nhân]
                  │
                  └──<<extend>>──── [Upload Avatar]

Customer ───> [Xem đơn mua]

Customer ───> [Xem đánh giá]
```

---

## UC-06: ĐƠN HÀNG CỦA TÔI

### **Actors:** Khách hàng

### **Use Cases:**

#### **1. Xem danh sách đơn của tôi** (Main)
- Xem tất cả đơn hàng

#### **2. Lọc theo trạng thái**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn lọc

#### **3. Xem chi tiết đơn của tôi** (Main)
- Xem chi tiết 1 đơn

#### **4. Theo dõi trạng thái**
- **Relationship:** `<<include>>` Xem chi tiết
- Bắt buộc hiển thị timeline

#### **5. Hủy đơn hàng**
- **Relationship:** `<<extend>>` Xem chi tiết
- Chỉ khi PENDING

#### **6. Viết đánh giá sản phẩm**
- **Relationship:** `<<extend>>` Xem chi tiết
- Chỉ khi DELIVERED

### **Diagram Structure:**
```
Actor ──────> [Xem danh sách đơn của tôi]
                  │
                  └──<<extend>>──── [Lọc theo trạng thái]

Actor ──────> [Xem chi tiết đơn của tôi]
                  │
                  ├──<<include>>──> [Theo dõi trạng thái]
                  │
                  ├──<<extend>>──── [Hủy đơn hàng]
                  │
                  └──<<extend>>──── [Viết đánh giá sản phẩm]
```

---

## UC-07: GIỎ HÀNG

### **Actors:** Khách hàng

### **Use Cases:**

#### **1. Xem giỏ hàng** (Main)
- Xem tất cả items

#### **2. Tính tổng tiền**
- **Relationship:** `<<include>>` Xem giỏ hàng
- Bắt buộc tính tổng

#### **3. Thêm vào giỏ** (Main)
- Thêm sản phẩm

#### **4. Cập nhật số lượng** (Main)
- Thay đổi số lượng

#### **5. Xóa item** (Main)
- Xóa sản phẩm

#### **6. Thanh toán** (Main)
- Chuyển đến checkout

#### **7. Kiểm tra đăng nhập**
- **Relationship:** `<<include>>` Thanh toán
- Bắt buộc đăng nhập

### **Diagram Structure:**
```
Actor ──────> [Xem giỏ hàng]
                  │
                  └──<<include>>──> [Tính tổng tiền]

Actor ──────> [Thêm vào giỏ]

Actor ──────> [Cập nhật số lượng]

Actor ──────> [Xóa item]

Actor ──────> [Thanh toán]
                  │
                  └──<<include>>──> [Kiểm tra đăng nhập]
```

---

## UC-08: SẢN PHẨM

### **Actors:** Admin, Sales Staff, Warehouse Staff, Khách hàng, Khách vãng lai

### **Use Cases:**

#### **1. Xem danh sách sản phẩm** (Main)
- Duyệt sản phẩm

#### **2. Phân trang**
- **Relationship:** `<<include>>` Xem danh sách
- Bắt buộc phân trang

#### **3. Tìm kiếm sản phẩm**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn tìm kiếm

#### **4. Lọc theo danh mục**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn lọc

#### **5. Lọc theo giá**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn lọc giá

#### **6. Sắp xếp sản phẩm**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn sắp xếp

#### **7. Xem chi tiết sản phẩm** (Main)
- Xem thông tin chi tiết

#### **8. Xem đánh giá sản phẩm**
- **Relationship:** `<<include>>` Xem chi tiết
- Bắt buộc hiển thị reviews

### **Diagram Structure:**
```
Actor ──────> [Xem danh sách sản phẩm]
                  │
                  ├──<<include>>──> [Phân trang]
                  │
                  ├──<<extend>>──── [Tìm kiếm sản phẩm]
                  │
                  ├──<<extend>>──── [Lọc theo danh mục]
                  │
                  ├──<<extend>>──── [Lọc theo giá]
                  │
                  └──<<extend>>──── [Sắp xếp sản phẩm]

Actor ──────> [Xem chi tiết sản phẩm]
                  │
                  └──<<include>>──> [Xem đánh giá sản phẩm]
```

---

## UC-09: QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG

### **Actors:** Admin

### **Use Cases:**

#### **1. Xem danh sách người dùng** (Main)
- Xem tất cả users

#### **2. Tìm kiếm người dùng**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn tìm kiếm

#### **3. Xem chi tiết người dùng** (Main)
- Xem thông tin user

#### **4. Khóa tài khoản** (Main)
- Khóa user

#### **5. Gửi email thông báo**
- **Relationship:** `<<include>>` Khóa tài khoản
- Bắt buộc gửi email

#### **6. Mở khóa tài khoản** (Main)
- Mở khóa user

#### **7. Reset mật khẩu người dùng** (Main)
- Reset password

#### **8. Xóa tài khoản** (Main)
- Xóa user

#### **9. Kiểm tra có đơn hàng**
- **Relationship:** `<<include>>` Xóa tài khoản
- Bắt buộc kiểm tra

### **Diagram Structure:**
```
Admin ──────> [Xem danh sách người dùng]
                  │
                  └──<<extend>>──── [Tìm kiếm người dùng]

Admin ──────> [Xem chi tiết người dùng]

Admin ──────> [Khóa tài khoản]
                  │
                  └──<<include>>──> [Gửi email thông báo]

Admin ──────> [Mở khóa tài khoản]

Admin ──────> [Reset mật khẩu người dùng]

Admin ──────> [Xóa tài khoản]
                  │
                  └──<<include>>──> [Kiểm tra có đơn hàng]
```

---

## UC-10: QUẢN LÝ ĐƠN HÀNG (ADMIN)

### **Actors:** Admin, Sales Staff

### **Use Cases:**

#### **1. Xem danh sách đơn hàng** (Main)
- Xem tất cả orders

#### **2. Lọc đơn hàng theo trạng thái**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn lọc

#### **3. Xem chi tiết đơn hàng** (Main)
- Xem thông tin đơn

#### **4. Cập nhật trạng thái đơn hàng** (Main)
- Thay đổi status

#### **5. Gửi email thông báo**
- **Relationship:** `<<include>>` Cập nhật trạng thái
- Bắt buộc gửi email

#### **6. Hủy đơn hàng** (Main)
- Hủy order

#### **7. Hoàn lại tồn kho**
- **Relationship:** `<<include>>` Hủy đơn hàng
- Bắt buộc hoàn kho

### **Diagram Structure:**
```
Actor ──────> [Xem danh sách đơn hàng]
                  │
                  └──<<extend>>──── [Lọc đơn hàng theo trạng thái]

Actor ──────> [Xem chi tiết đơn hàng]

Actor ──────> [Cập nhật trạng thái đơn hàng]
                  │
                  └──<<include>>──> [Gửi email thông báo]

Actor ──────> [Hủy đơn hàng]
                  │
                  └──<<include>>──> [Hoàn lại tồn kho]
```

**Lưu ý:** Sales Staff có quyền hạn giới hạn (không xóa đơn, không xem báo cáo)

---

## UC-11: QUẢN LÝ SẢN PHẨM

### **Actors:** Admin

### **Use Cases:**

#### **1. Xem danh sách sản phẩm** (Main)
- Xem tất cả products

#### **2. Tìm kiếm sản phẩm**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn tìm kiếm

#### **3. Thêm sản phẩm** (Main)
- Tạo product mới

#### **4. Upload hình ảnh**
- **Relationship:** `<<include>>` Thêm sản phẩm
- Bắt buộc upload ảnh

#### **5. Cập nhật sản phẩm** (Main)
- Chỉnh sửa product

#### **6. Xóa sản phẩm** (Main)
- Xóa product

#### **7. Kiểm tra có trong đơn hàng**
- **Relationship:** `<<include>>` Xóa sản phẩm
- Bắt buộc kiểm tra

### **Diagram Structure:**
```
Admin ──────> [Xem danh sách sản phẩm]
                  │
                  └──<<extend>>──── [Tìm kiếm sản phẩm]

Admin ──────> [Thêm sản phẩm]
                  │
                  └──<<include>>──> [Upload hình ảnh]

Admin ──────> [Cập nhật sản phẩm]

Admin ──────> [Xóa sản phẩm]
                  │
                  └──<<include>>──> [Kiểm tra có trong đơn hàng]
```

---

## UC-12: QUẢN LÝ DANH MỤC

### **Actors:** Admin

### **Use Cases:**

#### **1. Xem danh sách danh mục** (Main)
- Xem tất cả categories

#### **2. Thêm danh mục** (Main)
- Tạo category mới

#### **3. Validate tên trùng**
- **Relationship:** `<<include>>` Thêm danh mục
- Bắt buộc kiểm tra trùng

#### **4. Cập nhật danh mục** (Main)
- Chỉnh sửa category

#### **5. Xóa danh mục** (Main)
- Xóa category

#### **6. Kiểm tra có sản phẩm**
- **Relationship:** `<<include>>` Xóa danh mục
- Bắt buộc kiểm tra

### **Diagram Structure:**
```
Admin ──────> [Xem danh sách danh mục]

Admin ──────> [Thêm danh mục]
                  │
                  └──<<include>>──> [Validate tên trùng]

Admin ──────> [Cập nhật danh mục]

Admin ──────> [Xóa danh mục]
                  │
                  └──<<include>>──> [Kiểm tra có sản phẩm]
```

---

## UC-13: DASHBOARD ADMIN

### **Actors:** Admin

### **Use Cases:**

#### **1. Xem Dashboard** (Main)
- Xem tổng quan

#### **2. Xem thống kê tổng quan**
- **Relationship:** `<<include>>` Xem Dashboard
- Bắt buộc hiển thị stats

#### **3. Xem biểu đồ doanh thu**
- **Relationship:** `<<include>>` Xem Dashboard
- Bắt buộc hiển thị chart

#### **4. Xem đơn hàng gần đây**
- **Relationship:** `<<include>>` Xem Dashboard
- Bắt buộc hiển thị recent orders

#### **5. Xem sản phẩm bán chạy**
- **Relationship:** `<<include>>` Xem Dashboard
- Bắt buộc hiển thị best sellers

### **Diagram Structure:**
```
Admin ──────> [Xem Dashboard]
                  │
                  ├──<<include>>──> [Xem thống kê tổng quan]
                  │
                  ├──<<include>>──> [Xem biểu đồ doanh thu]
                  │
                  ├──<<include>>──> [Xem đơn hàng gần đây]
                  │
                  └──<<include>>──> [Xem sản phẩm bán chạy]
```

---

## UC-14: HỖ TRỢ KHÁCH HÀNG (MỚI)

### **Actors:** Sales Staff, Admin

### **Use Cases:**

#### **1. Tìm kiếm khách hàng** (Main)
- Tìm theo tên/email/SĐT

#### **2. Xem thông tin khách hàng** (Main)
- Xem profile khách (read-only)

#### **3. Xem lịch sử đơn hàng của khách** (Main)
- Xem orders của khách

#### **4. Xem chi tiết đơn hàng**
- **Relationship:** `<<include>>` Xem lịch sử
- Bắt buộc có thể xem chi tiết

#### **5. Tìm kiếm đơn hàng theo mã**
- **Relationship:** `<<extend>>` Xem lịch sử
- Tùy chọn tìm nhanh

### **Diagram Structure:**
```
Actor ──────> [Tìm kiếm khách hàng]

Actor ──────> [Xem thông tin khách hàng]

Actor ──────> [Xem lịch sử đơn hàng của khách]
                  │
                  ├──<<include>>──> [Xem chi tiết đơn hàng]
                  │
                  └──<<extend>>──── [Tìm kiếm đơn hàng theo mã]
```

---

## UC-15: QUẢN LÝ TỒN KHO (MỚI)

### **Actors:** Warehouse Staff, Admin

### **Use Cases:**

#### **1. Xem danh sách tồn kho** (Main)
- Xem tất cả products và variants

#### **2. Cảnh báo sắp hết hàng**
- **Relationship:** `<<include>>` Xem danh sách
- Bắt buộc highlight sản phẩm < 10

#### **3. Tìm kiếm sản phẩm theo SKU**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn tìm kiếm

#### **4. Cập nhật số lượng tồn kho** (Main)
- Thay đổi quantity

#### **5. Ghi log nhập/xuất**
- **Relationship:** `<<include>>` Cập nhật số lượng
- Bắt buộc ghi log

#### **6. Xem lịch sử nhập/xuất kho** (Main)
- Xem log history

#### **7. Xem đơn hàng cần chuẩn bị** (Main)
- Xem orders PENDING/CONFIRMED

### **Diagram Structure:**
```
Actor ──────> [Xem danh sách tồn kho]
                  │
                  ├──<<include>>──> [Cảnh báo sắp hết hàng]
                  │
                  └──<<extend>>──── [Tìm kiếm sản phẩm theo SKU]

Actor ──────> [Cập nhật số lượng tồn kho]
                  │
                  └──<<include>>──> [Ghi log nhập/xuất]

Actor ──────> [Xem lịch sử nhập/xuất kho]

Actor ──────> [Xem đơn hàng cần chuẩn bị]
```

---

## UC-16: DUYỆT SẢN PHẨM - GUEST (MỚI)

### **Actors:** Khách vãng lai

### **Use Cases:**

#### **1. Xem danh sách sản phẩm** (Main)
- Duyệt sản phẩm (chỉ đọc)

#### **2. Phân trang**
- **Relationship:** `<<include>>` Xem danh sách
- Bắt buộc phân trang

#### **3. Tìm kiếm sản phẩm**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn tìm kiếm

#### **4. Lọc sản phẩm**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn lọc (danh mục, giá)

#### **5. Sắp xếp sản phẩm**
- **Relationship:** `<<extend>>` Xem danh sách
- Tùy chọn sắp xếp

#### **6. Xem chi tiết sản phẩm** (Main)
- Xem thông tin chi tiết

#### **7. Xem đánh giá sản phẩm**
- **Relationship:** `<<include>>` Xem chi tiết
- Bắt buộc hiển thị reviews

#### **8. Đăng ký tài khoản** (Main)
- Chuyển từ Guest → Customer

### **Diagram Structure:**
```
Guest ──────> [Xem danh sách sản phẩm]
                  │
                  ├──<<include>>──> [Phân trang]
                  │
                  ├──<<extend>>──── [Tìm kiếm sản phẩm]
                  │
                  ├──<<extend>>──── [Lọc sản phẩm]
                  │
                  └──<<extend>>──── [Sắp xếp sản phẩm]

Guest ──────> [Xem chi tiết sản phẩm]
                  │
                  └──<<include>>──> [Xem đánh giá sản phẩm]

Guest ──────> [Đăng ký tài khoản]
```

**Lưu ý:** Guest KHÔNG thể thêm vào giỏ hàng hoặc đặt hàng

---

# PHẦN 3: QUY TẮC VẼ INCLUDE VÀ EXTEND

## Quy tắc `<<include>>`

**Sử dụng khi:** Chức năng con BẮT BUỘC phải thực hiện

**Cách vẽ:**
1. Vẽ mũi tên **nét đứt** (dashed arrow)
2. Hướng: Từ **Use Case chính** → **Use Case con**
3. Label: `<<include>>`
4. Style: Mũi tên mở (open arrow)

**Ví dụ:**
```
[Đăng ký] ──<<include>>──> [Kiểm tra mật khẩu mạnh]
```

## Quy tắc `<<extend>>`

**Sử dụng khi:** Chức năng con TÙY CHỌN, không bắt buộc

**Cách vẽ:**
1. Vẽ mũi tên **nét đứt** (dashed arrow)
2. Hướng: Từ **Use Case con** → **Use Case chính** (NGƯỢC LẠI)
3. Label: `<<extend>>`
4. Style: Mũi tên mở (open arrow)

**Ví dụ:**
```
[Đăng nhập bằng Facebook] ──<<extend>>──> [Đăng nhập]
```

## So sánh Include vs Extend

| Tiêu chí | `<<include>>` | `<<extend>>` |
|----------|---------------|--------------|
| **Tính chất** | Bắt buộc | Tùy chọn |
| **Hướng mũi tên** | Chính → Con | Con → Chính |
| **Khi nào dùng** | Chức năng luôn thực hiện | Chức năng có thể có hoặc không |
| **Ví dụ** | Đăng ký → Kiểm tra mật khẩu | Đăng nhập ← OAuth2 |

---

# PHẦN 4: CHECKLIST VẼ USE CASE DIAGRAMS

## Use Case Diagram Tổng

- [ ] Vẽ System Boundary "ShopOMG"
- [ ] Vẽ 5 Actors (Admin, Sales, Warehouse, Customer, Guest)
- [ ] Vẽ 16 Use Cases chính
- [ ] Vẽ associations (Actor → Use Case)
- [ ] Sắp xếp layout rõ ràng, dễ đọc
- [ ] Kiểm tra tất cả actors đã kết nối đúng use cases

## 16 Use Case Diagrams Chi tiết

### UC-01: Đăng nhập
- [ ] Vẽ use case chính "Đăng nhập"
- [ ] Vẽ `<<include>>` Kiểm tra số lần đăng nhập sai
- [ ] Vẽ `<<extend>>` Đăng nhập Facebook
- [ ] Vẽ `<<extend>>` Đăng nhập Google
- [ ] Vẽ use case "Đăng xuất"

### UC-02: Đăng ký
- [ ] Vẽ use case chính "Đăng ký"
- [ ] Vẽ `<<include>>` Kiểm tra mật khẩu mạnh
- [ ] Vẽ `<<include>>` Kiểm tra trùng
- [ ] Vẽ `<<include>>` Email Verification

### UC-03: Quên/Đặt lại mật khẩu
- [ ] Vẽ "Quên mật khẩu" + `<<include>>` Gửi email
- [ ] Vẽ "Đặt lại mật khẩu" + `<<include>>` Validate token
- [ ] Vẽ `<<include>>` Kiểm tra mật khẩu mạnh

### UC-04: Xác thực Email
- [ ] Vẽ "Xác thực Email" + `<<include>>` Validate token
- [ ] Vẽ "Gửi lại email" + `<<include>>` Tạo token mới

### UC-05: Tài khoản cá nhân
- [ ] Vẽ "Xem thông tin"
- [ ] Vẽ "Cập nhật thông tin" + `<<extend>>` Upload Avatar
- [ ] Vẽ "Xem đơn mua" (chỉ Customer)
- [ ] Vẽ "Xem đánh giá" (chỉ Customer)

### UC-06: Đơn hàng của tôi
- [ ] Vẽ "Xem danh sách" + `<<extend>>` Lọc trạng thái
- [ ] Vẽ "Xem chi tiết" + `<<include>>` Theo dõi trạng thái
- [ ] Vẽ `<<extend>>` Hủy đơn hàng
- [ ] Vẽ `<<extend>>` Viết đánh giá

### UC-07: Giỏ hàng
- [ ] Vẽ "Xem giỏ hàng" + `<<include>>` Tính tổng tiền
- [ ] Vẽ "Thêm vào giỏ", "Cập nhật số lượng", "Xóa item"
- [ ] Vẽ "Thanh toán" + `<<include>>` Kiểm tra đăng nhập

### UC-08: Sản phẩm
- [ ] Vẽ "Xem danh sách" + `<<include>>` Phân trang
- [ ] Vẽ `<<extend>>` Tìm kiếm, Lọc danh mục, Lọc giá, Sắp xếp
- [ ] Vẽ "Xem chi tiết" + `<<include>>` Xem đánh giá

### UC-09: Quản lý Tài khoản
- [ ] Vẽ "Xem danh sách" + `<<extend>>` Tìm kiếm
- [ ] Vẽ "Khóa tài khoản" + `<<include>>` Gửi email
- [ ] Vẽ "Xóa tài khoản" + `<<include>>` Kiểm tra đơn hàng

### UC-10: Quản lý Đơn hàng (Admin)
- [ ] Vẽ "Xem danh sách" + `<<extend>>` Lọc trạng thái
- [ ] Vẽ "Cập nhật trạng thái" + `<<include>>` Gửi email
- [ ] Vẽ "Hủy đơn" + `<<include>>` Hoàn tồn kho

### UC-11: Quản lý Sản phẩm
- [ ] Vẽ "Xem danh sách" + `<<extend>>` Tìm kiếm
- [ ] Vẽ "Thêm sản phẩm" + `<<include>>` Upload hình ảnh
- [ ] Vẽ "Xóa sản phẩm" + `<<include>>` Kiểm tra đơn hàng

### UC-12: Quản lý Danh mục
- [ ] Vẽ "Thêm danh mục" + `<<include>>` Validate trùng
- [ ] Vẽ "Xóa danh mục" + `<<include>>` Kiểm tra sản phẩm

### UC-13: Dashboard
- [ ] Vẽ "Xem Dashboard"
- [ ] Vẽ 4 `<<include>>`: Thống kê, Biểu đồ, Đơn gần đây, SP bán chạy

### UC-14: Hỗ trợ Khách hàng (MỚI)
- [ ] Vẽ "Tìm kiếm khách hàng"
- [ ] Vẽ "Xem lịch sử đơn" + `<<include>>` Xem chi tiết
- [ ] Vẽ `<<extend>>` Tìm kiếm đơn theo mã

### UC-15: Quản lý Tồn kho (MỚI)
- [ ] Vẽ "Xem danh sách" + `<<include>>` Cảnh báo hết hàng
- [ ] Vẽ `<<extend>>` Tìm kiếm SKU
- [ ] Vẽ "Cập nhật số lượng" + `<<include>>` Ghi log

### UC-16: Duyệt Sản phẩm - Guest (MỚI)
- [ ] Vẽ "Xem danh sách" + `<<include>>` Phân trang
- [ ] Vẽ `<<extend>>` Tìm kiếm, Lọc, Sắp xếp
- [ ] Vẽ "Xem chi tiết" + `<<include>>` Xem đánh giá
- [ ] Vẽ "Đăng ký tài khoản"

---

# PHẦN 5: TIPS VẼ CHUYÊN NGHIỆP

## Layout Tips

1. **Sắp xếp Actors:**
   - Admin, Sales, Warehouse: Bên trái
   - Customer, Guest: Bên phải
   - Khoảng cách đều nhau

2. **Sắp xếp Use Cases:**
   - Nhóm theo chức năng
   - Use case chính ở giữa
   - Include/Extend ở xung quanh

3. **Đường kết nối:**
   - Tránh chéo nhau
   - Đường thẳng, ngắn nhất
   - Label rõ ràng

## Style Tips

1. **Font:**
   - Actor: 12pt, Bold
   - Use Case: 11pt, Regular
   - Label: 9pt, Italic

2. **Màu sắc:**
   - Actor: Đen
   - Use Case chính: Xanh dương nhạt
   - Include: Xanh lá nhạt
   - Extend: Vàng nhạt

3. **Kích thước:**
   - Oval use case: 120x60px
   - Actor: 40x80px
   - Khoảng cách: 50px

## Lỗi thường gặp

❌ **SAI:** Vẽ mũi tên include từ con → chính
✅ **ĐÚNG:** Vẽ mũi tên include từ chính → con

❌ **SAI:** Vẽ mũi tên extend từ chính → con
✅ **ĐÚNG:** Vẽ mũi tên extend từ con → chính

❌ **SAI:** Dùng include cho chức năng tùy chọn
✅ **ĐÚNG:** Dùng extend cho chức năng tùy chọn

❌ **SAI:** Quá nhiều mối quan hệ, diagram rối
✅ **ĐÚNG:** Chỉ vẽ mối quan hệ quan trọng

---

**Chúc bạn vẽ Use Case Diagrams thành công! 🎨**
