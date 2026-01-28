# ĐẶC TẢ USE CASE - DỰ ÁN SHOPOMG

## PHẦN 1: USE CASES CHO ADMIN

---

### **UC-01: ĐĂNG NHẬP (LOGIN)**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-01 |
| **Tên Use Case** | Đăng nhập |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng, Admin |
| **Mô tả** | Người dùng đăng nhập vào hệ thống bằng email/username và mật khẩu, hoặc qua OAuth2 (Facebook/Google) |
| **Tiền điều kiện** | - Đã có tài khoản trong hệ thống<br>- Email đã được xác thực (emailVerified = true) |
| **Luồng chính** | 1. Người dùng truy cập `/login`<br>2. Nhập email/username và password<br>3. Hệ thống kiểm tra thông tin đăng nhập<br>4. Nếu đúng → Tạo session, chuyển đến trang chủ<br>5. Nếu sai → Tăng số lần đăng nhập sai, hiển thị thông báo lỗi<br>6. Nếu đăng nhập sai >= 5 lần → Khóa tài khoản 15 phút |
| **Luồng thay thế** | **Đăng nhập bằng Facebook:**<br>1. Click "Đăng nhập bằng Facebook"<br>2. Chuyển hướng đến Facebook OAuth2<br>3. Xác thực trên Facebook<br>4. Quay lại hệ thống với thông tin user<br>5. Tạo/cập nhật tài khoản, tạo session<br><br>**Đăng nhập bằng Google:**<br>Tương tự Facebook |
| **Hậu điều kiện** | - Session đăng nhập được tạo<br>- Người dùng được chuyển đến trang chủ/dashboard<br>- Số lần đăng nhập sai được reset về 0 (nếu đăng nhập thành công) |
| **Ngoại lệ** | - Email chưa được xác thực → Hiển thị thông báo "Vui lòng xác thực email"<br>- Tài khoản bị khóa → Hiển thị thời gian còn lại<br>- Thông tin đăng nhập sai → Hiển thị số lần còn lại |

**Bảng 1. Use Case Đăng nhập**

---

### **UC-02: ĐĂNG KÝ (REGISTER)**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-02 |
| **Tên Use Case** | Đăng ký |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Tạo tài khoản mới trong hệ thống với xác thực email |
| **Tiền điều kiện** | - Chưa có tài khoản<br>- Email chưa được sử dụng |
| **Luồng chính** | 1. Người dùng truy cập `/sign-up`<br>2. Điền form: Email, Username, Password, Confirm Password<br>3. Hệ thống validate:<br>&nbsp;&nbsp;&nbsp;- Kiểm tra email/username trùng<br>&nbsp;&nbsp;&nbsp;- Kiểm tra mật khẩu mạnh (≥8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt)<br>&nbsp;&nbsp;&nbsp;- Kiểm tra password = confirm password<br>4. Tạo tài khoản với `emailVerified = false`<br>5. Tạo verification token (thời hạn 24h)<br>6. Gửi email xác thực<br>7. Chuyển đến trang "Kiểm tra email của bạn" |
| **Hậu điều kiện** | - Tài khoản được tạo với trạng thái chưa xác thực<br>- Email verification được gửi<br>- Người dùng chưa thể đăng nhập cho đến khi xác thực email |
| **Ngoại lệ** | - Email đã tồn tại → "Email đã được sử dụng"<br>- Username đã tồn tại → "Tên người dùng đã được sử dụng"<br>- Mật khẩu yếu → "Mật khẩu không đủ mạnh"<br>- Password không khớp → "Mật khẩu xác nhận không khớp" |

**Bảng 2. Use Case Đăng ký**

---

### **UC-03: QUÊN/ĐẶT LẠI MẬT KHẨU**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-03 |
| **Tên Use Case** | Quên/Đặt lại mật khẩu |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Người dùng yêu cầu đặt lại mật khẩu qua email |
| **Tiền điều kiện** | - Đã có tài khoản trong hệ thống |
| **Luồng chính** | **Quên mật khẩu:**<br>1. Truy cập `/forgot-password`<br>2. Nhập email<br>3. Hệ thống tìm tài khoản theo email<br>4. Tạo password reset token (thời hạn 1h)<br>5. Gửi email chứa link reset<br>6. Hiển thị "Kiểm tra email của bạn"<br><br>**Đặt lại mật khẩu:**<br>1. Click link trong email → `/reset-password?token=xxx`<br>2. Hệ thống validate token (tồn tại, chưa hết hạn, chưa sử dụng)<br>3. Hiển thị form nhập mật khẩu mới<br>4. Nhập password mới + confirm password<br>5. Validate password strength<br>6. Hash password mới<br>7. Cập nhật password trong database<br>8. Đánh dấu token đã sử dụng<br>9. Chuyển đến trang login |
| **Hậu điều kiện** | - Mật khẩu được cập nhật thành công<br>- Token được đánh dấu đã sử dụng<br>- Người dùng có thể đăng nhập bằng mật khẩu mới |
| **Ngoại lệ** | - Email không tồn tại → Vẫn hiển thị "Kiểm tra email" (bảo mật)<br>- Token hết hạn → "Link đã hết hạn, vui lòng yêu cầu lại"<br>- Token đã sử dụng → "Link không hợp lệ"<br>- Mật khẩu mới yếu → "Mật khẩu không đủ mạnh" |

**Bảng 3. Use Case Quên/Đặt lại mật khẩu**

---

### **UC-04: XÁC THỰC EMAIL**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-04 |
| **Tên Use Case** | Xác thực Email |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Xác thực email sau khi đăng ký hoặc gửi lại email xác thực |
| **Tiền điều kiện** | - Đã đăng ký tài khoản<br>- Email chưa được xác thực |
| **Luồng chính** | **Xác thực Email:**<br>1. Click link trong email → `/verify-email?token=xxx`<br>2. Hệ thống validate token (tồn tại, chưa hết hạn 24h, chưa sử dụng)<br>3. Cập nhật `emailVerified = true`<br>4. Xóa token khỏi database<br>5. Hiển thị "Xác thực thành công"<br>6. Cho phép đăng nhập<br><br>**Gửi lại email xác thực:**<br>1. Truy cập `/resend-verification`<br>2. Nhập email<br>3. Kiểm tra email tồn tại và chưa verify<br>4. Xóa token cũ (nếu có)<br>5. Tạo token mới<br>6. Gửi email mới<br>7. Hiển thị "Email đã được gửi" |
| **Hậu điều kiện** | - Email được xác thực<br>- Người dùng có thể đăng nhập<br>- Token được xóa khỏi database |
| **Ngoại lệ** | - Token hết hạn → "Link xác thực đã hết hạn"<br>- Token không hợp lệ → "Link không hợp lệ"<br>- Email đã được xác thực → "Email đã được xác thực trước đó" |

**Bảng 4. Use Case Xác thực Email**

---

### **UC-05: TÀI KHOẢN CÁ NHÂN**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-05 |
| **Tên Use Case** | Tài khoản cá nhân |
| **Độ ưu tiên** | Trung bình |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Quản lý thông tin cá nhân, xem đơn hàng và đánh giá |
| **Tiền điều kiện** | - Đã đăng nhập |
| **Luồng chính** | 1. Truy cập `/account/profile`<br>2. **Xem thông tin:** Hiển thị Avatar, Fullname, Email, Phone, Address<br>3. **Cập nhật thông tin:**<br>&nbsp;&nbsp;&nbsp;- Click "Chỉnh sửa"<br>&nbsp;&nbsp;&nbsp;- Cập nhật: Fullname, Phone, Address, Ngày sinh<br>&nbsp;&nbsp;&nbsp;- Upload avatar (JPG/PNG, < 2MB)<br>&nbsp;&nbsp;&nbsp;- Submit form<br>&nbsp;&nbsp;&nbsp;- Validate và lưu vào database<br>4. **Xem đơn mua:** Truy cập `/account/orders`, hiển thị danh sách đơn hàng<br>5. **Xem đánh giá:** Truy cập `/account/reviews`, hiển thị sản phẩm đã đánh giá và chưa đánh giá |
| **Hậu điều kiện** | - Thông tin cá nhân được cập nhật<br>- Avatar được lưu trên server<br>- Hiển thị danh sách đơn hàng và đánh giá |
| **Ngoại lệ** | - File avatar quá lớn → "File không được vượt quá 2MB"<br>- Định dạng file không hợp lệ → "Chỉ chấp nhận JPG/PNG" |

**Bảng 5. Use Case Tài khoản cá nhân**

---

### **UC-06: ĐƠN HÀNG CỦA TÔI**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-06 |
| **Tên Use Case** | Đơn hàng của tôi |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Xem danh sách đơn hàng, chi tiết đơn hàng, theo dõi trạng thái và hủy đơn |
| **Tiền điều kiện** | - Đã đăng nhập |
| **Luồng chính** | 1. **Xem danh sách:** Truy cập `/account/orders`, hiển thị tất cả đơn hàng theo tabs:<br>&nbsp;&nbsp;&nbsp;- Tất cả<br>&nbsp;&nbsp;&nbsp;- Chờ xác nhận (PENDING)<br>&nbsp;&nbsp;&nbsp;- Đang giao (SHIPPING)<br>&nbsp;&nbsp;&nbsp;- Đã giao (DELIVERED)<br>&nbsp;&nbsp;&nbsp;- Đã hủy (CANCELLED)<br>2. **Xem chi tiết:** Click vào đơn hàng, hiển thị:<br>&nbsp;&nbsp;&nbsp;- Thông tin sản phẩm<br>&nbsp;&nbsp;&nbsp;- Địa chỉ giao hàng<br>&nbsp;&nbsp;&nbsp;- Phương thức thanh toán<br>&nbsp;&nbsp;&nbsp;- Timeline trạng thái<br>3. **Theo dõi trạng thái:** Hiển thị timeline: PENDING → CONFIRMED → SHIPPING → DELIVERED<br>4. **Hủy đơn hàng:** (Chỉ khi status = PENDING)<br>&nbsp;&nbsp;&nbsp;- Click "Hủy đơn hàng"<br>&nbsp;&nbsp;&nbsp;- Nhập lý do hủy<br>&nbsp;&nbsp;&nbsp;- Xác nhận<br>&nbsp;&nbsp;&nbsp;- Cập nhật status = CANCELLED<br>&nbsp;&nbsp;&nbsp;- Hoàn lại tồn kho |
| **Hậu điều kiện** | - Hiển thị danh sách và chi tiết đơn hàng<br>- Đơn hàng được hủy (nếu PENDING)<br>- Tồn kho được hoàn lại |
| **Ngoại lệ** | - Không có quyền xem đơn hàng → "Không tìm thấy đơn hàng"<br>- Không thể hủy đơn đã xác nhận → "Không thể hủy đơn hàng này" |

**Bảng 6. Use Case Đơn hàng của tôi**

---

### **UC-07: GIỎ HÀNG**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-07 |
| **Tên Use Case** | Giỏ hàng |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Quản lý giỏ hàng: thêm, xóa, cập nhật số lượng sản phẩm |
| **Tiền điều kiện** | - Đã đăng nhập |
| **Luồng chính** | 1. **Thêm vào giỏ:**<br>&nbsp;&nbsp;&nbsp;- Từ trang sản phẩm → Click "Thêm vào giỏ"<br>&nbsp;&nbsp;&nbsp;- Chọn số lượng<br>&nbsp;&nbsp;&nbsp;- Kiểm tra sản phẩm đã có trong giỏ → Cập nhật số lượng<br>&nbsp;&nbsp;&nbsp;- Lưu cart vào session<br>&nbsp;&nbsp;&nbsp;- Hiển thị "Đã thêm vào giỏ"<br>2. **Xem giỏ:** Truy cập `/cart`, hiển thị danh sách items với hình ảnh, tên, giá, số lượng, tổng tiền<br>3. **Cập nhật số lượng:**<br>&nbsp;&nbsp;&nbsp;- Thay đổi số lượng (input hoặc +/-)<br>&nbsp;&nbsp;&nbsp;- Kiểm tra số lượng > 0 và <= tồn kho<br>&nbsp;&nbsp;&nbsp;- Cập nhật cart, tính lại tổng tiền<br>4. **Xóa item:** Click "Xóa", xác nhận, xóa khỏi cart<br>5. **Thanh toán:** Click "Thanh toán" → Chuyển đến `/checkout` |
| **Hậu điều kiện** | - Giỏ hàng được cập nhật<br>- Tổng tiền được tính lại<br>- Session được lưu |
| **Ngoại lệ** | - Số lượng vượt quá tồn kho → "Số lượng vượt quá tồn kho"<br>- Sản phẩm hết hàng → "Sản phẩm tạm hết hàng" |

**Bảng 7. Use Case Giỏ hàng**

---

### **UC-08: SẢN PHẨM**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-08 |
| **Tên Use Case** | Sản phẩm |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Xem danh sách sản phẩm, tìm kiếm, lọc, sắp xếp và xem chi tiết |
| **Tiền điều kiện** | - Dữ liệu sản phẩm tồn tại |
| **Luồng chính** | 1. **Xem danh sách:** Truy cập `/products`, hiển thị sản phẩm dạng lưới (20 sản phẩm/trang)<br>2. **Tìm kiếm:** Nhập từ khóa, query `WHERE name LIKE '%keyword%' OR description LIKE '%keyword%'`<br>3. **Lọc theo danh mục:** Chọn danh mục, query `WHERE category_id = ?`<br>4. **Lọc theo giá:** Chọn khoảng giá, query `WHERE price BETWEEN min AND max`<br>5. **Sắp xếp:** Chọn tiêu chí (Giá thấp→cao, Giá cao→thấp, Tên A→Z, Mới nhất)<br>6. **Phân trang:** Click số trang để xem thêm<br>7. **Xem chi tiết:** Click sản phẩm → `/product/{id}`, hiển thị:<br>&nbsp;&nbsp;&nbsp;- Hình ảnh (nhiều ảnh)<br>&nbsp;&nbsp;&nbsp;- Tên, mô tả, giá<br>&nbsp;&nbsp;&nbsp;- Số lượng còn lại<br>&nbsp;&nbsp;&nbsp;- Đánh giá trung bình<br>&nbsp;&nbsp;&nbsp;- Danh sách đánh giá<br>&nbsp;&nbsp;&nbsp;- Sản phẩm liên quan |
| **Hậu điều kiện** | - Danh sách sản phẩm hiển thị theo tiêu chí<br>- Chi tiết sản phẩm được load đầy đủ |
| **Ngoại lệ** | - Không tìm thấy sản phẩm → "Không tìm thấy sản phẩm nào"<br>- ID không hợp lệ → "Sản phẩm không tồn tại" |

**Bảng 8. Use Case Sản phẩm**

---

## PHẦN 2: USE CASES CHO ADMIN

---

### **UC-09: QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-09 |
| **Tên Use Case** | Quản lý Tài khoản người dùng |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Admin |
| **Mô tả** | Quản lý tài khoản người dùng: xem, tìm kiếm, khóa/mở khóa, reset mật khẩu, xóa |
| **Tiền điều kiện** | - Đăng nhập với ROLE ADMIN |
| **Luồng chính** | 1. Truy cập `/admin/accounts`<br>2. **Xem danh sách:** Hiển thị tất cả tài khoản với Avatar, Username, Email, Role, Status, Ngày tạo<br>3. **Tìm kiếm:** Nhập keyword, query `WHERE username LIKE '%keyword%' OR email LIKE '%keyword%'`<br>4. **Xem chi tiết:** Click user, hiển thị thông tin chi tiết, lịch sử đơn hàng<br>5. **Khóa tài khoản:**<br>&nbsp;&nbsp;&nbsp;- Click "Khóa", xác nhận<br>&nbsp;&nbsp;&nbsp;- Cập nhật `isActive = false`<br>&nbsp;&nbsp;&nbsp;- Đăng xuất user<br>&nbsp;&nbsp;&nbsp;- Gửi email thông báo<br>6. **Mở khóa:** Cập nhật `isActive = true`, reset failed attempts<br>7. **Reset mật khẩu:**<br>&nbsp;&nbsp;&nbsp;- Tạo mật khẩu tạm thời<br>&nbsp;&nbsp;&nbsp;- Hash và cập nhật<br>&nbsp;&nbsp;&nbsp;- Gửi email mật khẩu tạm<br>8. **Xóa tài khoản:** Kiểm tra không có đơn hàng → Xóa |
| **Hậu điều kiện** | - Tài khoản được cập nhật/xóa<br>- Email thông báo được gửi<br>- Log hành động được ghi |
| **Ngoại lệ** | - Tài khoản có đơn hàng → "Không thể xóa tài khoản có đơn hàng" |

**Bảng 9. Use Case Quản lý Tài khoản người dùng**

---

### **UC-10: QUẢN LÝ ĐƠN HÀNG (ADMIN)**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-10 |
| **Tên Use Case** | Quản lý Đơn hàng (Admin) |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Admin |
| **Mô tả** | Quản lý đơn hàng: xem, lọc, cập nhật trạng thái, hủy đơn |
| **Tiền điều kiện** | - Đăng nhập với ROLE ADMIN |
| **Luồng chính** | 1. Truy cập `/admin/orders`<br>2. **Xem danh sách:** Hiển thị tất cả đơn hàng, sắp xếp theo ngày mới nhất<br>3. **Lọc theo trạng thái:** Chọn tab (PENDING/CONFIRMED/SHIPPING/DELIVERED/CANCELLED)<br>4. **Xem chi tiết:** Click đơn hàng, hiển thị:<br>&nbsp;&nbsp;&nbsp;- Thông tin khách hàng<br>&nbsp;&nbsp;&nbsp;- Địa chỉ giao hàng<br>&nbsp;&nbsp;&nbsp;- Danh sách sản phẩm<br>&nbsp;&nbsp;&nbsp;- Tổng tiền, phương thức thanh toán<br>&nbsp;&nbsp;&nbsp;- Timeline trạng thái<br>5. **Cập nhật trạng thái:**<br>&nbsp;&nbsp;&nbsp;- Chọn trạng thái mới (PENDING→CONFIRMED→SHIPPING→DELIVERED)<br>&nbsp;&nbsp;&nbsp;- Xác nhận<br>&nbsp;&nbsp;&nbsp;- Cập nhật database<br>&nbsp;&nbsp;&nbsp;- Gửi email thông báo khách hàng<br>6. **Hủy đơn:** Nhập lý do, cập nhật CANCELLED, hoàn tồn kho |
| **Hậu điều kiện** | - Trạng thái đơn được cập nhật<br>- Email thông báo được gửi<br>- Log được ghi |
| **Ngoại lệ** | - Không thể cập nhật trạng thái ngược → "Không thể cập nhật trạng thái" |

**Bảng 10. Use Case Quản lý Đơn hàng (Admin)**

---

### **UC-11: QUẢN LÝ SẢN PHẨM**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-11 |
| **Tên Use Case** | Quản lý Sản phẩm |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Admin |
| **Mô tả** | Quản lý sản phẩm: CRUD sản phẩm với upload hình ảnh |
| **Tiền điều kiện** | - Đăng nhập với ROLE ADMIN |
| **Luồng chính** | 1. Truy cập `/admin/products`<br>2. **Xem danh sách:** Hiển thị tất cả sản phẩm với Hình ảnh, Tên, Danh mục, Giá, Tồn kho, Trạng thái<br>3. **Tìm kiếm:** Nhập từ khóa, query theo tên/mô tả<br>4. **Tạo sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Click "Thêm sản phẩm"<br>&nbsp;&nbsp;&nbsp;- Điền form: Tên, Mô tả, Giá, Số lượng<br>&nbsp;&nbsp;&nbsp;- Chọn danh mục<br>&nbsp;&nbsp;&nbsp;- Upload nhiều hình ảnh (JPG/PNG, < 5MB/ảnh)<br>&nbsp;&nbsp;&nbsp;- Submit, validate, lưu database<br>5. **Cập nhật:** Load thông tin hiện tại, chỉnh sửa, upload/xóa ảnh, lưu<br>6. **Xóa:** Kiểm tra không có trong đơn hàng → Xóa sản phẩm và hình ảnh |
| **Hậu điều kiện** | - Sản phẩm được tạo/cập nhật/xóa<br>- Hình ảnh được lưu trên server |
| **Ngoại lệ** | - Sản phẩm có trong đơn hàng → "Không thể xóa"<br>- File ảnh quá lớn → "File không được vượt quá 5MB" |

**Bảng 11. Use Case Quản lý Sản phẩm**

---

### **UC-12: QUẢN LÝ DANH MỤC**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-12 |
| **Tên Use Case** | Quản lý Danh mục |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Admin |
| **Mô tả** | Quản lý danh mục sản phẩm: CRUD danh mục |
| **Tiền điều kiện** | - Đăng nhập với ROLE ADMIN |
| **Luồng chính** | 1. Truy cập `/admin/categories`<br>2. **Xem danh sách:** Hiển thị tất cả danh mục với Icon, Tên, Mô tả, Số sản phẩm, Ngày tạo<br>3. **Tạo danh mục:**<br>&nbsp;&nbsp;&nbsp;- Click "Thêm danh mục"<br>&nbsp;&nbsp;&nbsp;- Điền: Tên, Mô tả, Icon (optional)<br>&nbsp;&nbsp;&nbsp;- Validate tên không trống và chưa tồn tại<br>&nbsp;&nbsp;&nbsp;- Lưu database<br>4. **Cập nhật:** Load thông tin, chỉnh sửa, validate, lưu<br>5. **Xóa:** Kiểm tra không có sản phẩm → Xóa danh mục |
| **Hậu điều kiện** | - Danh mục được tạo/cập nhật/xóa thành công |
| **Ngoại lệ** | - Tên trùng → "Tên danh mục đã tồn tại"<br>- Có sản phẩm → "Không thể xóa danh mục có sản phẩm" |

**Bảng 12. Use Case Quản lý Danh mục**

---

### **UC-13: DASHBOARD ADMIN**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-13 |
| **Tên Use Case** | Dashboard Admin |
| **Độ ưu tiên** | Trung bình |
| **Tác nhân** | Admin |
| **Mô tả** | Xem tổng quan thống kê hệ thống |
| **Tiền điều kiện** | - Đăng nhập với ROLE ADMIN |
| **Luồng chính** | 1. Truy cập `/admin/dashboard`<br>2. **Xem tổng quan thống kê:**<br>&nbsp;&nbsp;&nbsp;- Tổng số sản phẩm (COUNT products)<br>&nbsp;&nbsp;&nbsp;- Tổng số đơn hàng (COUNT orders)<br>&nbsp;&nbsp;&nbsp;- Tổng số người dùng (COUNT accounts)<br>&nbsp;&nbsp;&nbsp;- Tổng doanh thu (SUM WHERE status = DELIVERED)<br>&nbsp;&nbsp;&nbsp;- Doanh thu tháng này<br>&nbsp;&nbsp;&nbsp;- Số đơn chờ xử lý (WHERE status = PENDING)<br>3. **Xem đơn hàng gần đây:** 10 đơn mới nhất<br>4. **Xem biểu đồ doanh thu:** Chọn khoảng thời gian (7 ngày/30 ngày/12 tháng), hiển thị line chart<br>5. **Xem sản phẩm bán chạy:** Top 10 sản phẩm |
| **Hậu điều kiện** | - Hiển thị đầy đủ thống kê và biểu đồ |
| **Ngoại lệ** | - Không có dữ liệu → Hiển thị 0 hoặc "Chưa có dữ liệu" |

**Bảng 13. Use Case Dashboard Admin**

---

### **UC-14: HỖ TRỢ KHÁCH HÀNG**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-14 |
| **Tên Use Case** | Hỗ trợ Khách hàng |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Nhân viên Bán hàng, Admin |
| **Mô tả** | Xem thông tin khách hàng, lịch sử đơn hàng để hỗ trợ giải đáp thắc mắc |
| **Tiền điều kiện** | - Đăng nhập với ROLE SALES_STAFF hoặc ADMIN<br>- Khách hàng đã tồn tại trong hệ thống |
| **Luồng chính** | 1. Truy cập `/sales/customers`<br>2. **Tìm kiếm khách hàng:**<br>&nbsp;&nbsp;&nbsp;- Nhập keyword (tên, email, SĐT)<br>&nbsp;&nbsp;&nbsp;- Query `WHERE fullName LIKE '%keyword%' OR email LIKE '%keyword%' OR phone LIKE '%keyword%'`<br>&nbsp;&nbsp;&nbsp;- Hiển thị danh sách khách hàng phù hợp<br>3. **Xem thông tin khách hàng (chỉ đọc):**<br>&nbsp;&nbsp;&nbsp;- Click vào khách hàng<br>&nbsp;&nbsp;&nbsp;- Hiển thị: Họ tên, Email, SĐT, Địa chỉ, Ngày đăng ký<br>&nbsp;&nbsp;&nbsp;- Hiển thị trạng thái tài khoản (Active/Locked)<br>&nbsp;&nbsp;&nbsp;- Hiển thị số lần đăng nhập thất bại (nếu có)<br>4. **Xem lịch sử đơn hàng:**<br>&nbsp;&nbsp;&nbsp;- Hiển thị danh sách tất cả đơn hàng của khách<br>&nbsp;&nbsp;&nbsp;- Sắp xếp theo ngày mới nhất<br>&nbsp;&nbsp;&nbsp;- Hiển thị: Mã đơn, Ngày đặt, Trạng thái, Tổng tiền<br>&nbsp;&nbsp;&nbsp;- Tổng số đơn hàng và tổng giá trị đã mua<br>5. **Xem chi tiết đơn hàng:**<br>&nbsp;&nbsp;&nbsp;- Click vào đơn hàng<br>&nbsp;&nbsp;&nbsp;- Hiển thị đầy đủ thông tin đơn hàng<br>&nbsp;&nbsp;&nbsp;- Hiển thị timeline trạng thái<br>6. **Tìm kiếm đơn hàng:**<br>&nbsp;&nbsp;&nbsp;- Tìm theo mã đơn hàng (Order ID)<br>&nbsp;&nbsp;&nbsp;- Tìm theo tên/SĐT người nhận |
| **Luồng thay thế** | **Luồng 2a: Không tìm thấy khách hàng**<br>1. Hệ thống không tìm thấy khách hàng phù hợp<br>2. Hiển thị thông báo "Không tìm thấy khách hàng"<br>3. Gợi ý kiểm tra lại từ khóa tìm kiếm<br>4. Quay lại bước 2<br><br>**Luồng 4a: Khách hàng chưa có đơn hàng**<br>1. Hiển thị thông báo "Khách hàng chưa có đơn hàng nào"<br>2. Gợi ý khách hàng mua sắm<br><br>**Luồng 5a: Xem chi tiết sản phẩm trong đơn**<br>1. Click vào sản phẩm trong đơn hàng<br>2. Hiển thị thông tin sản phẩm: Tên, Giá, Số lượng, Tồn kho hiện tại<br>3. Quay lại chi tiết đơn hàng |
| **Hậu điều kiện** | - Hiển thị thông tin khách hàng và lịch sử đơn hàng<br>- Nhân viên có đủ thông tin để hỗ trợ khách hàng<br>- Log hành động xem thông tin khách hàng (cho audit) |
| **Ngoại lệ** | - Không tìm thấy khách hàng → "Không tìm thấy khách hàng"<br>- Không có quyền xem → "Bạn không có quyền truy cập"<br>- Lỗi kết nối database → "Lỗi hệ thống, vui lòng thử lại"<br>- Session hết hạn → Chuyển về trang đăng nhập |
| **Ràng buộc** | - Nhân viên Bán hàng chỉ được XEM, không được SỬA thông tin khách hàng<br>- Không hiển thị mật khẩu khách hàng<br>- Thời gian response < 2 giây cho tìm kiếm<br>- Hiển thị tối đa 50 kết quả tìm kiếm/trang |
| **Yêu cầu phi chức năng** | - **Bảo mật:** Chỉ hiển thị thông tin cần thiết, không hiển thị dữ liệu nhạy cảm<br>- **Hiệu năng:** Tìm kiếm phải nhanh, sử dụng index trên email, phone<br>- **Usability:** Giao diện dễ sử dụng, tìm kiếm nhanh chóng<br>- **Audit:** Ghi log mỗi lần xem thông tin khách hàng |

**Bảng 14. Use Case Hỗ trợ Khách hàng**

### **Code liên quan:**
- `SalesController.java` - `/sales/customers`, `/sales/customer/{id}`
- `CustomerService.java` - `searchCustomers()`, `getCustomerDetails()`
- `OrderService.java` - `getOrdersByCustomerId()`
- **Security:** `@PreAuthorize("hasAnyRole('SALES_STAFF', 'ADMIN')")`

---

### **UC-15: QUẢN LÝ TỒN KHO**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-15 |
| **Tên Use Case** | Quản lý Tồn kho |
| **Độ ưu tiên** | Cao |
| **Tác nhân** | Nhân viên Kho, Admin |
| **Mô tả** | Quản lý số lượng tồn kho của sản phẩm và biến thể, theo dõi nhập/xuất kho |
| **Tiền điều kiện** | - Đăng nhập với ROLE WAREHOUSE_STAFF hoặc ADMIN<br>- Sản phẩm và biến thể đã tồn tại trong hệ thống |
| **Luồng chính** | 1. Truy cập `/warehouse/inventory`<br>2. **Xem danh sách tồn kho:**<br>&nbsp;&nbsp;&nbsp;- Hiển thị tất cả sản phẩm và biến thể<br>&nbsp;&nbsp;&nbsp;- Cột: SKU, Tên sản phẩm, Màu sắc, Kích thước, Số lượng tồn, Trạng thái<br>&nbsp;&nbsp;&nbsp;- Highlight sản phẩm sắp hết hàng (quantity < 10) màu vàng<br>&nbsp;&nbsp;&nbsp;- Highlight sản phẩm hết hàng (quantity = 0) màu đỏ<br>&nbsp;&nbsp;&nbsp;- Hiển thị tổng số SKU và tổng giá trị tồn kho<br>3. **Tìm kiếm sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Tìm theo SKU: Query `WHERE sku = ?` (exact match)<br>&nbsp;&nbsp;&nbsp;- Tìm theo tên: Query `WHERE product.name LIKE '%keyword%'`<br>&nbsp;&nbsp;&nbsp;- Tìm theo danh mục<br>4. **Xem chi tiết sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Click vào sản phẩm<br>&nbsp;&nbsp;&nbsp;- Hiển thị: Thông tin sản phẩm, Danh sách biến thể, Số lượng từng biến thể<br>&nbsp;&nbsp;&nbsp;- Hiển thị giá nhập, giá bán, lợi nhuận dự kiến<br>5. **Cập nhật số lượng tồn kho:**<br>&nbsp;&nbsp;&nbsp;- Click "Cập nhật tồn kho"<br>&nbsp;&nbsp;&nbsp;- Nhập số lượng mới (hoặc +/- số lượng)<br>&nbsp;&nbsp;&nbsp;- Chọn loại giao dịch: Nhập hàng/Xuất hàng/Kiểm kê/Hỏng hóc/Trả hàng<br>&nbsp;&nbsp;&nbsp;- Nhập lý do chi tiết<br>&nbsp;&nbsp;&nbsp;- Nhập ghi chú (optional)<br>&nbsp;&nbsp;&nbsp;- Xác nhận<br>&nbsp;&nbsp;&nbsp;- Validate: Số lượng >= 0<br>&nbsp;&nbsp;&nbsp;- Cập nhật `ProductVariants.quantity`<br>&nbsp;&nbsp;&nbsp;- Ghi log lịch sử nhập/xuất với timestamp, user, lý do<br>6. **Xem lịch sử nhập/xuất:**<br>&nbsp;&nbsp;&nbsp;- Hiển thị log: Ngày, Loại (Nhập/Xuất/Kiểm kê), Số lượng, Lý do, Người thực hiện<br>&nbsp;&nbsp;&nbsp;- Lọc theo khoảng thời gian<br>&nbsp;&nbsp;&nbsp;- Lọc theo loại giao dịch<br>&nbsp;&nbsp;&nbsp;- Export Excel báo cáo<br>7. **Xem đơn hàng cần chuẩn bị:**<br>&nbsp;&nbsp;&nbsp;- Hiển thị đơn hàng PENDING và CONFIRMED<br>&nbsp;&nbsp;&nbsp;- Hiển thị sản phẩm cần lấy từ kho với số lượng<br>&nbsp;&nbsp;&nbsp;- Sắp xếp theo độ ưu tiên (CONFIRMED trước) |
| **Luồng thay thế** | **Luồng 2a: Cảnh báo sắp hết hàng**<br>1. Hệ thống phát hiện sản phẩm có quantity < 10<br>2. Hiển thị badge cảnh báo màu vàng<br>3. Gửi thông báo cho Admin (nếu < 5)<br>4. Gợi ý nhập hàng bổ sung<br><br>**Luồng 5a: Số lượng không hợp lệ**<br>1. Nhân viên nhập số lượng âm hoặc > giới hạn<br>2. Hiển thị lỗi "Số lượng không hợp lệ"<br>3. Yêu cầu nhập lại<br>4. Quay lại bước 5<br><br>**Luồng 5b: Xuất kho vượt quá tồn kho**<br>1. Nhân viên xuất kho số lượng > tồn kho hiện tại<br>2. Hiển thị cảnh báo "Số lượng xuất vượt quá tồn kho"<br>3. Hiển thị số lượng tồn hiện tại<br>4. Yêu cầu nhập lại hoặc hủy<br><br>**Luồng 7a: Không đủ hàng cho đơn**<br>1. Phát hiện đơn hàng có sản phẩm hết hàng<br>2. Highlight đơn hàng màu đỏ<br>3. Gửi thông báo cho Sales Staff<br>4. Gợi ý hủy đơn hoặc chờ nhập hàng |
| **Hậu điều kiện** | - Số lượng tồn kho được cập nhật chính xác trong database<br>- Lịch sử nhập/xuất được ghi log đầy đủ<br>- Cảnh báo sản phẩm sắp hết hàng được hiển thị<br>- Thông báo được gửi cho Admin nếu cần |
| **Ngoại lệ** | - Số lượng âm → "Số lượng không hợp lệ"<br>- SKU không tồn tại → "Không tìm thấy sản phẩm"<br>- Không có quyền cập nhật → "Bạn không có quyền thực hiện thao tác này"<br>- Lỗi database → "Lỗi hệ thống, vui lòng thử lại"<br>- Concurrent update → "Dữ liệu đã thay đổi, vui lòng tải lại" |
| **Ràng buộc** | - Số lượng tồn kho phải >= 0<br>- Số lượng tối đa: 999,999 (giới hạn hệ thống)<br>- Lý do cập nhật là BẮT BUỘC<br>- Mỗi lần cập nhật phải ghi log<br>- Chỉ Admin mới được xóa log lịch sử<br>- Thời gian cập nhật < 1 giây |
| **Yêu cầu phi chức năng** | - **Tính toàn vẹn:** Đảm bảo số lượng tồn kho luôn chính xác, sử dụng transaction<br>- **Audit Trail:** Ghi log đầy đủ mọi thay đổi (ai, khi nào, làm gì)<br>- **Concurrency:** Xử lý đúng khi nhiều người cập nhật cùng lúc (optimistic locking)<br>- **Performance:** Index trên SKU, product_id để tìm kiếm nhanh<br>- **Alert:** Tự động cảnh báo khi sản phẩm sắp hết (< 10) hoặc hết hàng (= 0) |

**Bảng 15. Use Case Quản lý Tồn kho**

### **Code liên quan:**
- `WarehouseController.java` - `/warehouse/inventory`, `/warehouse/update`
- `InventoryService.java` - `updateStock()`, `getInventoryHistory()`
- `ProductVariantRepository.java` - `findBySku()`, `updateQuantity()`
- `InventoryLog` entity - Ghi log nhập/xuất
- **Security:** `@PreAuthorize("hasAnyRole('WAREHOUSE_STAFF', 'ADMIN')")`
- **Transaction:** `@Transactional` để đảm bảo tính toàn vẹn

---

### **UC-16: DUYỆT SẢN PHẨM (GUEST)**

| **Thuộc tính** | **Nội dung** |
|----------------|--------------|
| **Mã UC** | UC-16 |
| **Tên Use Case** | Duyệt Sản phẩm (Guest) |
| **Độ ưu tiên** | Trung bình |
| **Tác nhân** | Khách vãng lai |
| **Mô tả** | Xem danh sách và chi tiết sản phẩm mà không cần đăng nhập, khuyến khích đăng ký để mua hàng |
| **Tiền điều kiện** | - Không cần đăng nhập<br>- Dữ liệu sản phẩm tồn tại và có trạng thái active<br>- Website đang hoạt động |
| **Luồng chính** | 1. **Xem danh sách sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Truy cập `/` hoặc `/products`<br>&nbsp;&nbsp;&nbsp;- Hiển thị sản phẩm dạng lưới (20 sản phẩm/trang)<br>&nbsp;&nbsp;&nbsp;- Hiển thị: Hình ảnh, Tên, Giá, Giảm giá (nếu có), Đánh giá trung bình<br>&nbsp;&nbsp;&nbsp;- Hiển thị badge "Mới" cho sản phẩm < 7 ngày<br>&nbsp;&nbsp;&nbsp;- Hiển thị badge "Sale" cho sản phẩm có discount<br>2. **Tìm kiếm sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Nhập từ khóa vào search box<br>&nbsp;&nbsp;&nbsp;- Query `WHERE name LIKE '%keyword%' OR description LIKE '%keyword%'`<br>&nbsp;&nbsp;&nbsp;- Hiển thị kết quả phù hợp<br>&nbsp;&nbsp;&nbsp;- Highlight từ khóa tìm kiếm trong kết quả<br>3. **Lọc sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Lọc theo danh mục: Click vào danh mục<br>&nbsp;&nbsp;&nbsp;- Lọc theo giá: Chọn khoảng giá (< 100k, 100k-500k, > 500k)<br>&nbsp;&nbsp;&nbsp;- Lọc theo giới tính: Nam/Nữ/Unisex<br>&nbsp;&nbsp;&nbsp;- Lọc theo đánh giá: >= 4 sao, >= 3 sao<br>&nbsp;&nbsp;&nbsp;- Có thể kết hợp nhiều bộ lọc<br>4. **Sắp xếp sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Giá thấp → cao<br>&nbsp;&nbsp;&nbsp;- Giá cao → thấp<br>&nbsp;&nbsp;&nbsp;- Tên A → Z<br>&nbsp;&nbsp;&nbsp;- Mới nhất<br>&nbsp;&nbsp;&nbsp;- Bán chạy nhất<br>5. **Xem chi tiết sản phẩm:**<br>&nbsp;&nbsp;&nbsp;- Click vào sản phẩm → `/product/{id}`<br>&nbsp;&nbsp;&nbsp;- Hiển thị: Nhiều hình ảnh (gallery), Tên, Mô tả, Giá, Giảm giá<br>&nbsp;&nbsp;&nbsp;- Hiển thị thông tin: Chất liệu, Xuất xứ, Hướng dẫn bảo quản<br>&nbsp;&nbsp;&nbsp;- Hiển thị số lượng đã bán<br>&nbsp;&nbsp;&nbsp;- Hiển thị đánh giá trung bình và số lượng đánh giá<br>&nbsp;&nbsp;&nbsp;- Hiển thị danh sách đánh giá của khách hàng (5 đánh giá mới nhất)<br>&nbsp;&nbsp;&nbsp;- Hiển thị sản phẩm liên quan (cùng danh mục)<br>6. **Khuyến khích đăng ký:**<br>&nbsp;&nbsp;&nbsp;- Nút "Thêm vào giỏ hàng" bị disabled (màu xám)<br>&nbsp;&nbsp;&nbsp;- Hiển thị tooltip khi hover: "Vui lòng đăng nhập để mua hàng"<br>&nbsp;&nbsp;&nbsp;- Hiển thị banner sticky: "Đăng ký ngay để nhận ưu đãi 10%"<br>&nbsp;&nbsp;&nbsp;- Hiển thị popup sau 30 giây: "Đăng ký để mua hàng và nhận voucher"<br>7. **Chuyển đến đăng ký/đăng nhập:**<br>&nbsp;&nbsp;&nbsp;- Click "Đăng nhập" → `/login` (redirect về trang hiện tại sau khi đăng nhập)<br>&nbsp;&nbsp;&nbsp;- Click "Đăng ký" → `/sign-up`<br>&nbsp;&nbsp;&nbsp;- Click "Thêm vào giỏ" → Chuyển đến `/login` với thông báo |
| **Luồng thay thế** | **Luồng 1a: Không có sản phẩm**<br>1. Hệ thống không có sản phẩm nào<br>2. Hiển thị "Chưa có sản phẩm nào"<br>3. Gợi ý quay lại sau<br><br>**Luồng 2a: Không tìm thấy sản phẩm**<br>1. Không có sản phẩm phù hợp với từ khóa<br>2. Hiển thị "Không tìm thấy sản phẩm nào"<br>3. Gợi ý từ khóa tương tự hoặc sản phẩm phổ biến<br>4. Hiển thị top 10 sản phẩm bán chạy<br><br>**Luồng 5a: Sản phẩm hết hàng**<br>1. Sản phẩm có quantity = 0<br>2. Hiển thị badge "Hết hàng"<br>3. Nút "Thêm vào giỏ" bị disabled<br>4. Gợi ý đăng ký nhận thông báo khi có hàng<br><br>**Luồng 6a: Guest click "Thêm vào giỏ"**<br>1. Guest click nút "Thêm vào giỏ hàng"<br>2. Hiển thị modal "Vui lòng đăng nhập để mua hàng"<br>3. Cung cấp 2 nút: "Đăng nhập" và "Đăng ký"<br>4. Lưu product_id vào session để thêm vào giỏ sau khi đăng nhập<br><br>**Luồng 7a: Đăng ký thành công**<br>1. Guest hoàn tất đăng ký<br>2. Tự động đăng nhập<br>3. Chuyển về trang sản phẩm đang xem<br>4. Nút "Thêm vào giỏ" được kích hoạt |
| **Hậu điều kiện** | - Khách vãng lai xem được sản phẩm đầy đủ<br>- Khuyến khích đăng ký để mua hàng<br>- Không thể thêm vào giỏ hàng hoặc đặt hàng<br>- Tracking hành vi xem sản phẩm (analytics) |
| **Ngoại lệ** | - Không tìm thấy sản phẩm → "Không tìm thấy sản phẩm nào"<br>- ID không hợp lệ → "Sản phẩm không tồn tại"<br>- Sản phẩm đã bị xóa → "Sản phẩm không còn tồn tại"<br>- Lỗi load hình ảnh → Hiển thị placeholder image |
| **Ràng buộc** | - Guest KHÔNG được: Thêm vào giỏ, Đặt hàng, Viết đánh giá, Xem giỏ hàng<br>- Guest ĐƯỢC: Xem sản phẩm, Tìm kiếm, Lọc, Sắp xếp, Xem đánh giá<br>- Thời gian load trang < 3 giây<br>- Hiển thị tối đa 20 sản phẩm/trang<br>- Popup khuyến khích đăng ký chỉ hiển thị 1 lần/session |
| **Yêu cầu phi chức năng** | - **SEO:** Tối ưu cho công cụ tìm kiếm (meta tags, structured data)<br>- **Performance:** Cache danh sách sản phẩm, lazy load hình ảnh<br>- **Analytics:** Tracking hành vi Guest để phân tích conversion rate<br>- **Conversion:** Tối ưu UX để khuyến khích đăng ký (CTA rõ ràng, popup không quá phiền)<br>- **Mobile:** Responsive design, tối ưu cho mobile |

**Bảng 16. Use Case Duyệt Sản phẩm (Guest)**

### **Code liên quan:**
- `HomeController.java` - `/`, `/products`, `/product/{id}`
- `ProductService.java` - `getActiveProducts()`, `searchProducts()`
- **Security:** Không cần authentication, public access
- **Session:** Lưu product_id khi Guest click "Thêm vào giỏ" để xử lý sau khi đăng nhập
- **Analytics:** Google Analytics tracking cho Guest behavior

### **Conversion Strategy:**
1. **Passive:** Banner, tooltip khuyến khích đăng ký
2. **Active:** Popup sau 30 giây hoặc khi scroll 50% trang
3. **Trigger:** Khi click "Thêm vào giỏ" → Modal đăng nhập/đăng ký
4. **Incentive:** Voucher 10% cho đăng ký mới

---

## 📝 GHI CHÚ

- Tất cả **16 Use Cases** đã được đặc tả chi tiết dựa trên code thực tế và Use Case Diagrams đã vẽ
- Mỗi Use Case bao gồm: Mã UC, Tên, Độ ưu tiên, Tác nhân, Mô tả, Tiền điều kiện, Luồng chính, Hậu điều kiện, Ngoại lệ
- Hệ thống có **5 tác nhân:** Admin, Nhân viên Bán hàng, Nhân viên Kho, Khách hàng, Khách vãng lai
- **3 Use Cases mới:**
  - UC-14: Hỗ trợ Khách hàng (Nhân viên Bán hàng, Admin)
  - UC-15: Quản lý Tồn kho (Nhân viên Kho, Admin)
  - UC-16: Duyệt Sản phẩm (Khách vãng lai)
- Phù hợp cho báo cáo đồ án tốt nghiệp Giai đoạn 1

