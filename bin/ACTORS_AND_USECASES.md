# DANH SÁCH TÁC NHÂN VÀ USE CASE - DỰ ÁN SHOPOMG

## 2.3 Danh sách Tác nhân

| **STT** | **Tên tác nhân** | **Mô tả** |
|---------|------------------|-----------|
| **1** | **Admin** | **Mô tả:** Người quản trị có quyền cao nhất trong hệ thống.<br><br>**Chức năng:**<br>• **Xác thực:** Đăng nhập vào trang quản trị với ROLE ADMIN<br>• **Dashboard:** Xem tổng quan thống kê (tổng sản phẩm, đơn hàng, người dùng, doanh thu), xem biểu đồ doanh thu theo thời gian, xem đơn hàng gần đây, xem top sản phẩm bán chạy<br>• **Quản lý Danh mục:** CRUD danh mục sản phẩm, kiểm tra danh mục có sản phẩm trước khi xóa<br>• **Quản lý Sản phẩm:** CRUD sản phẩm, upload nhiều hình ảnh cho sản phẩm, tìm kiếm theo từ khóa, kiểm tra sản phẩm có trong đơn hàng trước khi xóa<br>• **Quản lý Đơn hàng:** Xem danh sách/chi tiết đơn hàng, lọc theo trạng thái, cập nhật trạng thái đơn hàng, hủy đơn hàng, gửi email thông báo, hoàn lại tồn kho<br>• **Quản lý Tài khoản:** Tìm kiếm người dùng, xem chi tiết và lịch sử đơn hàng, khóa/mở khóa tài khoản, reset mật khẩu, xóa tài khoản<br>• **Quản lý Tồn kho:** Toàn quyền quản lý số lượng tồn kho, xem lịch sử nhập/xuất<br>• **Truy cập:** Toàn bộ hệ thống |
| **2** | **Nhân viên Bán hàng** | **Mô tả:** Nhân viên hỗ trợ khách hàng và xử lý đơn hàng.<br><br>**Chức năng:**<br>• **Xác thực:** Đăng nhập với ROLE SALES_STAFF<br>• **Quản lý Đơn hàng (giới hạn):**<br>&nbsp;&nbsp;- Xem danh sách đơn hàng<br>&nbsp;&nbsp;- Xem chi tiết đơn hàng<br>&nbsp;&nbsp;- Cập nhật trạng thái đơn hàng (PENDING → CONFIRMED → SHIPPING)<br>&nbsp;&nbsp;- Hủy đơn hàng với lý do<br>&nbsp;&nbsp;- Gửi email thông báo cho khách hàng<br>• **Hỗ trợ Khách hàng:**<br>&nbsp;&nbsp;- Xem thông tin khách hàng (chỉ đọc)<br>&nbsp;&nbsp;- Xem lịch sử đơn hàng của khách<br>&nbsp;&nbsp;- Tìm kiếm đơn hàng theo mã, tên, SĐT<br>• **Xem Sản phẩm:** Xem danh sách và chi tiết sản phẩm (chỉ đọc)<br>• **KHÔNG được:** Xóa đơn hàng, quản lý sản phẩm, quản lý danh mục, xem báo cáo doanh thu, quản lý tài khoản<br>• **Truy cập:** `/sales/orders`, `/sales/customers` |
| **3** | **Nhân viên Kho** | **Mô tả:** Nhân viên quản lý tồn kho và cập nhật số lượng sản phẩm.<br><br>**Chức năng:**<br>• **Xác thực:** Đăng nhập với ROLE WAREHOUSE_STAFF<br>• **Quản lý Tồn kho:**<br>&nbsp;&nbsp;- Xem danh sách sản phẩm và biến thể<br>&nbsp;&nbsp;- Xem số lượng tồn kho (ProductVariants)<br>&nbsp;&nbsp;- Cập nhật số lượng tồn kho<br>&nbsp;&nbsp;- Xem lịch sử nhập/xuất kho<br>&nbsp;&nbsp;- Tìm kiếm sản phẩm theo SKU, tên<br>• **Xem Đơn hàng:** Xem danh sách đơn hàng để chuẩn bị hàng (chỉ đọc)<br>• **KHÔNG được:** Thêm/xóa sản phẩm, thay đổi giá, quản lý danh mục, cập nhật trạng thái đơn hàng<br>• **Truy cập:** `/warehouse/inventory`, `/warehouse/products` |
| **4** | **Khách hàng** | **Mô tả:** Người dùng đã đăng ký và xác thực trong hệ thống.<br><br>**Chức năng:**<br>• **Xác thực & Bảo mật:**<br>&nbsp;&nbsp;- Đăng ký tài khoản mới với xác thực email<br>&nbsp;&nbsp;- Đăng nhập bằng email/username và mật khẩu<br>&nbsp;&nbsp;- Đăng nhập bằng Facebook hoặc Google (OAuth2)<br>&nbsp;&nbsp;- Xác thực email sau khi đăng ký<br>&nbsp;&nbsp;- Gửi lại email xác thực nếu chưa nhận được<br>&nbsp;&nbsp;- Quên mật khẩu và đặt lại mật khẩu qua email<br>&nbsp;&nbsp;- Đăng xuất tài khoản<br>• **Quản lý Tài khoản:**<br>&nbsp;&nbsp;- Xem/cập nhật thông tin cá nhân<br>&nbsp;&nbsp;- Upload ảnh đại diện<br>&nbsp;&nbsp;- Xem danh sách đơn hàng và đánh giá<br>• **Mua sắm:**<br>&nbsp;&nbsp;- Xem, tìm kiếm, lọc, sắp xếp sản phẩm<br>&nbsp;&nbsp;- Xem chi tiết sản phẩm và đánh giá<br>&nbsp;&nbsp;- Thêm vào giỏ hàng, thanh toán<br>• **Quản lý Đơn hàng:**<br>&nbsp;&nbsp;- Xem danh sách và chi tiết đơn hàng<br>&nbsp;&nbsp;- Theo dõi trạng thái đơn hàng<br>&nbsp;&nbsp;- Hủy đơn hàng (nếu PENDING)<br>• **Đánh giá Sản phẩm:**<br>&nbsp;&nbsp;- Viết đánh giá sản phẩm đã mua (rating, comment, upload ảnh)<br>&nbsp;&nbsp;- Xem danh sách đánh giá của tôi |
| **5** | **Khách vãng lai** | **Mô tả:** Người dùng chưa đăng ký/đăng nhập vào hệ thống.<br><br>**Chức năng:**<br>• **Xem Sản phẩm (chỉ đọc):**<br>&nbsp;&nbsp;- Xem danh sách sản phẩm<br>&nbsp;&nbsp;- Xem chi tiết sản phẩm<br>&nbsp;&nbsp;- Tìm kiếm sản phẩm theo từ khóa<br>&nbsp;&nbsp;- Lọc sản phẩm theo danh mục, giá<br>&nbsp;&nbsp;- Sắp xếp sản phẩm<br>&nbsp;&nbsp;- Xem đánh giá sản phẩm<br>• **Xác thực:**<br>&nbsp;&nbsp;- Đăng ký tài khoản mới<br>&nbsp;&nbsp;- Đăng nhập (chuyển thành Khách hàng)<br>• **KHÔNG được:** Thêm vào giỏ hàng, đặt hàng, viết đánh giá, xem thông tin cá nhân<br>• **Truy cập:** `/`, `/products`, `/product/{id}`, `/login`, `/register` |

**Bảng 1. Danh sách Tác nhân**

---

## 2.4 Danh sách Use Case

| **STT** | **Mã UC** | **Tên UC** | **Tác nhân** | **Mục đích** |
|---------|-----------|------------|--------------|--------------|
| **1** | UC-01 | Đăng nhập | Khách hàng, Admin, Nhân viên Bán hàng, Nhân viên Kho | Xác thực người dùng vào hệ thống bằng email/username và mật khẩu, hoặc qua OAuth2 (Facebook/Google). Kiểm tra số lần đăng nhập sai và khóa tài khoản nếu vượt quá 5 lần |
| **2** | UC-02 | Đăng ký | Khách hàng, Khách vãng lai | Tạo tài khoản mới với xác thực email. Kiểm tra email/username trùng, validate mật khẩu mạnh, gửi email verification |
| **3** | UC-03 | Quên/Đặt lại mật khẩu | Khách hàng, Admin, Nhân viên Bán hàng, Nhân viên Kho | Yêu cầu đặt lại mật khẩu qua email. Tạo reset token (thời hạn 1h), gửi email chứa link reset, validate token và cập nhật mật khẩu mới |
| **4** | UC-04 | Xác thực Email | Khách hàng | Xác thực email sau khi đăng ký hoặc gửi lại email xác thực. Validate verification token (thời hạn 24h), cập nhật emailVerified = true |
| **5** | UC-05 | Tài khoản cá nhân | Khách hàng, Admin, Nhân viên Bán hàng, Nhân viên Kho | Quản lý thông tin cá nhân: xem/cập nhật profile (tên, sđt, địa chỉ, ngày sinh), upload avatar, xem đơn mua (chỉ Khách hàng), xem đánh giá (chỉ Khách hàng) |
| **6** | UC-06 | Đơn hàng của tôi | Khách hàng | Xem danh sách đơn hàng, lọc theo trạng thái, xem chi tiết đơn hàng, theo dõi timeline trạng thái, hủy đơn hàng (nếu PENDING), viết đánh giá sản phẩm đã mua |
| **7** | UC-07 | Giỏ hàng | Khách hàng | Quản lý giỏ hàng: thêm sản phẩm, xem giỏ, cập nhật số lượng, xóa item. Thanh toán: nhập thông tin giao hàng, chọn phương thức thanh toán (COD/VNPay/MoMo), tạo đơn hàng |
| **8** | UC-08 | Sản phẩm | Khách hàng, Khách vãng lai | Xem danh sách sản phẩm, tìm kiếm theo từ khóa, lọc theo danh mục/giá, sắp xếp, phân trang. Xem chi tiết sản phẩm với nhiều hình ảnh, mô tả, giá, đánh giá. Khách vãng lai chỉ xem, không thêm vào giỏ |
| **9** | UC-09 | Quản lý Tài khoản người dùng | Admin | Xem danh sách người dùng, tìm kiếm theo tên/email, xem chi tiết và lịch sử đơn hàng, khóa/mở khóa tài khoản, reset mật khẩu, xóa tài khoản (nếu không có đơn hàng) |
| **10** | UC-10 | Quản lý Đơn hàng (Admin) | Admin, Nhân viên Bán hàng | Xem danh sách/chi tiết đơn hàng, lọc theo trạng thái (PENDING/CONFIRMED/SHIPPING/DELIVERED/CANCELLED), cập nhật trạng thái đơn hàng, hủy đơn, gửi email thông báo, hoàn lại tồn kho. Nhân viên Bán hàng có quyền hạn giới hạn (không xóa đơn, không xem báo cáo) |
| **11** | UC-11 | Quản lý Sản phẩm | Admin | CRUD sản phẩm, upload nhiều hình ảnh cho sản phẩm, tìm kiếm theo từ khóa, kiểm tra sản phẩm có trong đơn hàng trước khi xóa |
| **12** | UC-12 | Quản lý Danh mục | Admin | CRUD danh mục sản phẩm, validate tên trùng, kiểm tra danh mục có sản phẩm trước khi xóa |
| **13** | UC-13 | Dashboard Admin | Admin | Xem tổng quan thống kê (tổng sản phẩm, đơn hàng, người dùng, doanh thu), xem biểu đồ doanh thu theo thời gian (7 ngày/30 ngày/12 tháng), xem đơn hàng gần đây, xem top sản phẩm bán chạy |
| **14** | UC-14 | Hỗ trợ Khách hàng | Nhân viên Bán hàng, Admin | Xem thông tin khách hàng (chỉ đọc), xem lịch sử đơn hàng của khách, tìm kiếm đơn hàng theo mã/tên/SĐT, hỗ trợ giải đáp thắc mắc |
| **15** | UC-15 | Quản lý Tồn kho | Nhân viên Kho, Admin | Xem danh sách sản phẩm và biến thể, xem/cập nhật số lượng tồn kho (ProductVariants), xem lịch sử nhập/xuất kho, tìm kiếm sản phẩm theo SKU |
| **16** | UC-16 | Duyệt Sản phẩm (Guest) | Khách vãng lai | Xem danh sách sản phẩm, tìm kiếm, lọc, sắp xếp, xem chi tiết sản phẩm và đánh giá. Chỉ đọc, không thể thêm vào giỏ hàng hoặc đặt hàng. Khuyến khích đăng ký để mua hàng |



**Bảng 2. Danh sách Use Case**

---

## Phân loại Use Case

### **Nhóm Khách hàng (8 Use Cases):**

#### **Xác thực & Bảo mật (4 UCs):**
- UC-01: Đăng nhập (chung với Admin, Nhân viên)
- UC-02: Đăng ký (chung với Khách vãng lai)
- UC-03: Quên/Đặt lại mật khẩu (chung với Admin, Nhân viên)
- UC-04: Xác thực Email

#### **Quản lý Tài khoản & Mua sắp (4 UCs):**
- UC-05: Tài khoản cá nhân (chung với Admin, Nhân viên)
- UC-06: Đơn hàng của tôi
- UC-07: Giỏ hàng
- UC-08: Sản phẩm (chung với tất cả tác nhân)

### **Nhóm Admin (10 Use Cases):**

#### **Quản lý Hệ thống (5 UCs):**
- UC-09: Quản lý Tài khoản người dùng
- UC-10: Quản lý Đơn hàng (chung với Nhân viên Bán hàng)
- UC-11: Quản lý Sản phẩm
- UC-12: Quản lý Danh mục
- UC-13: Dashboard Admin

#### **Hỗ trợ & Tồn kho (2 UCs):**
- UC-14: Hỗ trợ Khách hàng (chung với Nhân viên Bán hàng)
- UC-15: Quản lý Tồn kho (chung với Nhân viên Kho)

#### **Chung với tác nhân khác (3 UCs):**
- UC-01: Đăng nhập
- UC-03: Quên/Đặt lại mật khẩu
- UC-05: Tài khoản cá nhân

### **Nhóm Nhân viên Bán hàng (5 Use Cases):**
- UC-01: Đăng nhập
- UC-03: Quên/Đặt lại mật khẩu
- UC-05: Tài khoản cá nhân
- UC-10: Quản lý Đơn hàng (giới hạn)
- UC-14: Hỗ trợ Khách hàng

### **Nhóm Nhân viên Kho (4 Use Cases):**
- UC-01: Đăng nhập
- UC-03: Quên/Đặt lại mật khẩu
- UC-05: Tài khoản cá nhân
- UC-15: Quản lý Tồn kho

### **Nhóm Khách vãng lai (2 Use Cases):**
- UC-02: Đăng ký
- UC-16: Duyệt Sản phẩm (Guest)



---

## Ma trận Use Case - Tác nhân

| **Use Case** | **Admin** | **Nhân viên Bán hàng** | **Nhân viên Kho** | **Khách hàng** | **Khách vãng lai** |
|--------------|:---------:|:----------------------:|:-----------------:|:--------------:|:------------------:|
| UC-01: Đăng nhập | ✅ | ✅ | ✅ | ✅ | - |
| UC-02: Đăng ký | - | - | - | ✅ | ✅ |
| UC-03: Quên/Đặt lại mật khẩu | ✅ | ✅ | ✅ | ✅ | - |
| UC-04: Xác thực Email | - | - | - | ✅ | - |
| UC-05: Tài khoản cá nhân | ✅ | ✅ | ✅ | ✅ | - |
| UC-06: Đơn hàng của tôi | - | - | - | ✅ | - |
| UC-07: Giỏ hàng | - | - | - | ✅ | - |
| UC-08: Sản phẩm | ✅ | ✅ (đọc) | ✅ (đọc) | ✅ | ✅ (đọc) |
| UC-09: Quản lý Tài khoản người dùng | ✅ | - | - | - | - |
| UC-10: Quản lý Đơn hàng (Admin) | ✅ | ✅ (giới hạn) | - | - | - |
| UC-11: Quản lý Sản phẩm | ✅ | - | - | - | - |
| UC-12: Quản lý Danh mục | ✅ | - | - | - | - |
| UC-13: Dashboard Admin | ✅ | - | - | - | - |
| UC-14: Hỗ trợ Khách hàng | ✅ | ✅ | - | - | - |
| UC-15: Quản lý Tồn kho | ✅ | - | ✅ | - | - |
| UC-16: Duyệt Sản phẩm (Guest) | - | - | - | - | ✅ |



**Bảng 3. Ma trận Use Case - Tác nhân**

---

## Tổng kết

- **Tổng số Tác nhân:** 5 Tác nhân (Admin, Nhân viên Bán hàng, Nhân viên Kho, Khách hàng, Khách vãng lai)
- **Tổng số Use Cases:** 16 Use Cases
- **Use Cases cho Khách hàng:** 8 Use Cases (50%)
- **Use Cases cho Nhân viên Bán hàng:** 5 Use Cases (31%)
- **Use Cases cho Nhân viên Kho:** 4 Use Cases (25%)
- **Use Cases cho Admin:** 10 Use Cases (63%)
- **Use Cases cho Khách vãng lai:** 2 Use Cases (13%)
- **Use Cases chung (nhiều tác nhân):** 5 Use Cases (Đăng nhập, Quên mật khẩu, Tài khoản cá nhân, Sản phẩm, Quản lý Đơn hàng)
- **Độ ưu tiên cao:** 13 Use Cases (81%)
- **Độ ưu tiên trung bình:** 3 Use Cases (19%)



---

## Ghi chú

- Tất cả Use Cases đã được triển khai đầy đủ trong dự án ShopOMG
- Mỗi Use Case có đặc tả chi tiết trong file `USE_CASE_SPECIFICATIONS.md`
- Phù hợp cho báo cáo đồ án tốt nghiệp Giai đoạn 1
- Hệ thống có 5 tác nhân với phân quyền rõ ràng:
  - **Admin:** Toàn quyền hệ thống
  - **Nhân viên Bán hàng:** Xử lý đơn hàng và hỗ trợ khách hàng
  - **Nhân viên Kho:** Quản lý tồn kho
  - **Khách hàng:** Mua sắm và quản lý tài khoản
  - **Khách vãng lai:** Xem sản phẩm (chỉ đọc)
- Không có module quản lý "thương hiệu" hay "thông số kỹ thuật chi tiết" riêng

