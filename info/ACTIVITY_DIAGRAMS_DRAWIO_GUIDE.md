# Hướng Dẫn Vẽ Activity Diagrams Trên Draw.io - ShopOMG

## Mục Lục

1. [Hướng Dẫn Chung](#hướng-dẫn-chung)
2. [Luồng Xác Thực](#luồng-xác-thực)
3. [Luồng Xác Thực Email](#luồng-xác-thực-email)
4. [Luồng Đặt Lại Mật Khẩu](#luồng-đặt-lại-mật-khẩu)
5. [Luồng Quản Lý Tài Khoản](#luồng-quản-lý-tài-khoản)
6. [Luồng Quản Lý Admin](#luồng-quản-lý-admin)
7. [Luồng Mua Sắm](#luồng-mua-sắm)

---

## Hướng Dẫn Chung

### Chuẩn Bị

1. Truy cập https://www.draw.io/ hoặc https://app.diagrams.net/
2. Chọn "Create New Diagram"
3. Chọn "Blank Diagram" và đặt tên file

### Các Hình Dạng Cần Dùng

**Trong Draw.io, sử dụng các hình từ thư viện "General" và "Flowchart":**

- **Hình Oval (Rounded Rectangle)**: Điểm bắt đầu/kết thúc
  - Màu: Xanh lá nhạt (#E8F5E9) cho Start, Đỏ nhạt (#FFEBEE) cho End
  - Border: 2px
  
- **Hình Chữ Nhật (Rectangle)**: Các bước xử lý
  - Màu: Xanh dương nhạt (#E3F2FD)
  - Border: 2px
  
- **Hình Thoi (Diamond)**: Điểm quyết định (Decision)
  - Màu: Vàng nhạt (#FFF9C4)
  - Border: 2px
  
- **Mũi Tên (Arrow)**: Kết nối các bước
  - Độ dày: 2px
  - Màu: Đen (#000000)
  - Label: Điều kiện (Yes/No, có/không)

### Font Chữ Khuyến Nghị

- Font: Arial hoặc Roboto
- Size: 11pt cho text trong hình, 9pt cho label trên mũi tên
- Căn giữa (Center align)

---

## LUỒNG 1: XÁC THỰC

### 1.1. Đăng Nhập Với Giới Hạn Số Lần Thử

#### Các Nodes (Hình)

**Start Node:**
- Hình: Oval
- Text: "Người dùng truy cập /login"
- Màu: #E8F5E9

**Decision Nodes (Hình Thoi):**
1. "Có tham số email?"
2. "Tài khoản bị khóa?"
3. "Input hợp lệ?"
4. "Tài khoản bị khóa?"
5. "Email đã xác thực?"
6. "Thông tin đăng nhập đúng?"
7. "Số lần thử >= 5?"
8. "Vai trò người dùng?"

**Process Nodes (Hình Chữ Nhật):**
1. "Hiển thị số lần thử còn lại"
2. "Hiển thị số phút còn lại bị khóa"
3. "Hiển thị form đăng nhập"
4. "Người dùng nhập thông tin"
5. "Hiển thị lỗi validation"
6. "Tính toán thời gian khóa còn lại"
7. "Hiển thị lỗi tài khoản bị khóa"
8. "Hiển thị lỗi cần xác thực email"
9. "Ghi nhận đăng nhập thất bại"
10. "Tăng failed_login_attempts"
11. "Khóa tài khoản: account_locked_until = now + 15 phút"
12. "Hiển thị lỗi với số lần thử còn lại"
13. "Hiển thị thông báo tài khoản bị khóa"
14. "Ghi nhận đăng nhập thành công"
15. "Reset failed_login_attempts = 0"
16. "Xóa account_locked_until"
17. "Cập nhật last_login"
18. "Tạo phiên đăng nhập"

**End Nodes (Oval):**
1. "Quay lại trang login"
2. "Chuyển đến trang xác thực"
3. "Chuyển đến /admin/dashboard" (nếu ADMIN)
4. "Chuyển đến /home" (nếu USER)

#### Kết Nối (Arrows)

**Luồng chính:**
1. Start → "Có tham số email?"
2. "Có tham số email?" → (Yes) → "Hiển thị số lần thử còn lại"
3. "Có tham số email?" → (No) → "Hiển thị form đăng nhập"
4. "Hiển thị số lần thử còn lại" → "Tài khoản bị khóa?"
5. "Tài khoản bị khóa?" → (Yes) → "Hiển thị số phút còn lại bị khóa"
6. "Tài khoản bị khóa?" → (No) → "Hiển thị form đăng nhập"
7. "Hiển thị số phút còn lại bị khóa" → "Hiển thị form đăng nhập"

**Luồng submit:**
8. "Hiển thị form đăng nhập" → "Người dùng nhập thông tin"
9. "Người dùng nhập thông tin" → "Input hợp lệ?"
10. "Input hợp lệ?" → (No) → "Hiển thị lỗi validation"
11. "Hiển thị lỗi validation" → "Hiển thị form đăng nhập"

**Luồng kiểm tra khóa:**
12. "Input hợp lệ?" → (Yes) → "Tài khoản bị khóa?"
13. "Tài khoản bị khóa?" → (Yes) → "Tính toán thời gian khóa còn lại"
14. "Tính toán thời gian khóa còn lại" → "Hiển thị lỗi tài khoản bị khóa"
15. "Hiển thị lỗi tài khoản bị khóa" → End: "Quay lại trang login"

**Luồng kiểm tra email:**
16. "Tài khoản bị khóa?" → (No) → "Email đã xác thực?"
17. "Email đã xác thực?" → (No) → "Hiển thị lỗi cần xác thực email"
18. "Hiển thị lỗi cần xác thực email" → End: "Chuyển đến trang xác thực"

**Luồng xác thực:**
19. "Email đã xác thực?" → (Yes) → "Thông tin đăng nhập đúng?"
20. "Thông tin đăng nhập đúng?" → (No) → "Ghi nhận đăng nhập thất bại"
21. "Ghi nhận đăng nhập thất bại" → "Tăng failed_login_attempts"
22. "Tăng failed_login_attempts" → "Số lần thử >= 5?"
23. "Số lần thử >= 5?" → (Yes) → "Khóa tài khoản: account_locked_until = now + 15 phút"
24. "Số lần thử >= 5?" → (No) → "Hiển thị lỗi với số lần thử còn lại"
25. "Khóa tài khoản: account_locked_until = now + 15 phút" → "Hiển thị thông báo tài khoản bị khóa"
26. "Hiển thị thông báo tài khoản bị khóa" → End: "Quay lại trang login"
27. "Hiển thị lỗi với số lần thử còn lại" → End: "Quay lại trang login"

**Luồng thành công:**
28. "Thông tin đăng nhập đúng?" → (Yes) → "Ghi nhận đăng nhập thành công"
29. "Ghi nhận đăng nhập thành công" → "Reset failed_login_attempts = 0"
30. "Reset failed_login_attempts = 0" → "Xóa account_locked_until"
31. "Xóa account_locked_until" → "Cập nhật last_login"
32. "Cập nhật last_login" → "Tạo phiên đăng nhập"
33. "Tạo phiên đăng nhập" → "Vai trò người dùng?"
34. "Vai trò người dùng?" → (ADMIN) → End: "Chuyển đến /admin/dashboard"
35. "Vai trò người dùng?" → (USER) → End: "Chuyển đến /home"

#### Ghi Chú Vẽ

- Sắp xếp theo chiều dọc từ trên xuống
- Các decision nodes nên căn giữa
- Các luồng phụ (error) nên ở bên phải
- Luồng chính ở giữa

---

### 1.2. Đăng Ký Với Xác Thực Email

#### Các Nodes

**Start:**
- "Người dùng truy cập /account/sign-up"

**Process Nodes:**
1. "Hiển thị form đăng ký"
2. "Người dùng điền form: username, fullName, email, password, phone"
3. "Người dùng submit đăng ký"
4. "Hiển thị lỗi validation"
5. "Hiển thị lỗi mật khẩu không khớp"
6. "Hiển thị lỗi độ mạnh mật khẩu"
7. "Hiển thị lỗi email đã tồn tại"
8. "Hiển thị lỗi username đã tồn tại"
9. "Tạo tài khoản với emailVerified = false"
10. "Hash mật khẩu bằng BCrypt"
11. "Đặt role = USER, isActive = true"
12. "Lưu tài khoản vào database"
13. "Tạo UUID verification token"
14. "Đặt token expiry = now + 24 giờ"
15. "Lưu token vào email_verification_tokens"
16. "Xây dựng email xác thực với link token"
17. "Gửi email xác thực"

**Decision Nodes:**
1. "Validation passed?"
2. "password == confirmPassword?"
3. "Mật khẩu đủ mạnh?"
4. "Email đã tồn tại?"
5. "Username đã tồn tại?"

**End:**
- "Chuyển đến /verify-email-sent"

#### Kết Nối

1. Start → "Hiển thị form đăng ký"
2. "Hiển thị form đăng ký" → "Người dùng điền form"
3. "Người dùng điền form" → "Người dùng submit đăng ký"
4. "Người dùng submit đăng ký" → "Validation passed?"
5. "Validation passed?" → (No) → "Hiển thị lỗi validation" → "Hiển thị form đăng ký"
6. "Validation passed?" → (Yes) → "password == confirmPassword?"
7. "password == confirmPassword?" → (No) → "Hiển thị lỗi mật khẩu không khớp" → "Hiển thị form đăng ký"
8. "password == confirmPassword?" → (Yes) → "Mật khẩu đủ mạnh?"
9. "Mật khẩu đủ mạnh?" → (No) → "Hiển thị lỗi độ mạnh mật khẩu" → "Hiển thị form đăng ký"
10. "Mật khẩu đủ mạnh?" → (Yes) → "Email đã tồn tại?"
11. "Email đã tồn tại?" → (Yes) → "Hiển thị lỗi email đã tồn tại" → "Hiển thị form đăng ký"
12. "Email đã tồn tại?" → (No) → "Username đã tồn tại?"
13. "Username đã tồn tại?" → (Yes) → "Hiển thị lỗi username đã tồn tại" → "Hiển thị form đăng ký"
14. "Username đã tồn tại?" → (No) → "Tạo tài khoản với emailVerified = false"
15. "Tạo tài khoản với emailVerified = false" → "Hash mật khẩu bằng BCrypt"
16. "Hash mật khẩu bằng BCrypt" → "Đặt role = USER, isActive = true"
17. "Đặt role = USER, isActive = true" → "Lưu tài khoản vào database"
18. "Lưu tài khoản vào database" → "Tạo UUID verification token"
19. "Tạo UUID verification token" → "Đặt token expiry = now + 24 giờ"
20. "Đặt token expiry = now + 24 giờ" → "Lưu token vào email_verification_tokens"
21. "Lưu token vào email_verification_tokens" → "Xây dựng email xác thực với link token"
22. "Xây dựng email xác thực với link token" → "Gửi email xác thực"
23. "Gửi email xác thực" → End

---

### 1.3. Đăng Nhập OAuth2 (Facebook/Google)

#### Các Nodes

**Start:**
- "Người dùng click nút đăng nhập mạng xã hội"

**Process Nodes:**
1. "Chuyển hướng đến OAuth provider"
2. "Người dùng xác thực với provider"
3. "Nhận OAuth2 callback"
4. "Trích xuất thông tin: email, name, ID"
5. "Sử dụng provider ID làm identifier"
6. "Sử dụng email làm identifier"
7. "Tìm tài khoản theo email"
8. "Tìm tài khoản theo username = provider ID"
9. "Cập nhật last_login"
10. "Tạo phiên đăng nhập"
11. "Tạo tài khoản mới"
12. "Đặt username = provider ID, email = email (nếu có), fullName = name, emailVerified = true"
13. "Đặt role = USER, isActive = true"
14. "Lưu tài khoản mới vào database"
15. "Tạo phiên đăng nhập mới"

**Decision Nodes:**
1. "Xác thực thành công?"
2. "Email có sẵn?"
3. "Tìm thấy tài khoản?"

**End Nodes:**
1. "Hiển thị lỗi xác thực"
2. "Chuyển đến /home" (người dùng cũ)
3. "Chuyển đến /home" (người dùng mới)

#### Kết Nối

1. Start → "Chuyển hướng đến OAuth provider"
2. "Chuyển hướng đến OAuth provider" → "Người dùng xác thực với provider"
3. "Người dùng xác thực với provider" → "Xác thực thành công?"
4. "Xác thực thành công?" → (No) → End: "Hiển thị lỗi xác thực"
5. "Xác thực thành công?" → (Yes) → "Nhận OAuth2 callback"
6. "Nhận OAuth2 callback" → "Trích xuất thông tin: email, name, ID"
7. "Trích xuất thông tin: email, name, ID" → "Email có sẵn?"
8. "Email có sẵn?" → (No) → "Sử dụng provider ID làm identifier"
9. "Email có sẵn?" → (Yes) → "Sử dụng email làm identifier"
10. "Sử dụng email làm identifier" → "Tìm tài khoản theo email"
11. "Sử dụng provider ID làm identifier" → "Tìm tài khoản theo username = provider ID"
12. "Tìm tài khoản theo email" → "Tìm thấy tài khoản?"
13. "Tìm tài khoản theo username = provider ID" → "Tìm thấy tài khoản?"
14. "Tìm thấy tài khoản?" → (Yes) → "Cập nhật last_login"
15. "Cập nhật last_login" → "Tạo phiên đăng nhập"
16. "Tạo phiên đăng nhập" → End: "Chuyển đến /home"
17. "Tìm thấy tài khoản?" → (No) → "Tạo tài khoản mới"
18. "Tạo tài khoản mới" → "Đặt username = provider ID, email = email (nếu có), fullName = name, emailVerified = true"
19. "Đặt username = provider ID..." → "Đặt role = USER, isActive = true"
20. "Đặt role = USER, isActive = true" → "Lưu tài khoản mới vào database"
21. "Lưu tài khoản mới vào database" → "Tạo phiên đăng nhập mới"
22. "Tạo phiên đăng nhập mới" → End: "Chuyển đến /home"

---

## LUỒNG 2: XÁC THỰC EMAIL

### 2.1. Xác Thực Email Từ Link

#### Các Nodes

**Start:**
- "Người dùng click link trong email"

**Process Nodes:**
1. "Parse URL: /verify-email?token=xxx"
2. "Trích xuất token string"
3. "Query bảng email_verification_tokens"
4. "Hiển thị trang lỗi link không hợp lệ"
5. "Hiển thị lỗi token hết hạn/không hợp lệ"
6. "Xóa token đã hết hạn"
7. "Lấy tài khoản liên kết"
8. "Đặt emailVerified = true"
9. "Lưu tài khoản vào database"
10. "Xóa token đã sử dụng"
11. "Hiển thị trang xác thực thành công"
12. "Cung cấp link đến /login"

**Decision Nodes:**
1. "Có tham số token?"
2. "Token tồn tại trong DB?"
3. "Token expiry > now?"

**End Nodes:**
1. "Kết thúc" (sau lỗi)
2. "Người dùng có thể đăng nhập"

#### Kết Nối

1. Start → "Parse URL: /verify-email?token=xxx"
2. "Parse URL..." → "Có tham số token?"
3. "Có tham số token?" → (No) → "Hiển thị trang lỗi link không hợp lệ" → End: "Kết thúc"
4. "Có tham số token?" → (Yes) → "Trích xuất token string"
5. "Trích xuất token string" → "Query bảng email_verification_tokens"
6. "Query bảng email_verification_tokens" → "Token tồn tại trong DB?"
7. "Token tồn tại trong DB?" → (No) → "Hiển thị lỗi token hết hạn/không hợp lệ" → End: "Kết thúc"
8. "Token tồn tại trong DB?" → (Yes) → "Token expiry > now?"
9. "Token expiry > now?" → (No) → "Xóa token đã hết hạn" → "Hiển thị lỗi token hết hạn/không hợp lệ"
10. "Token expiry > now?" → (Yes) → "Lấy tài khoản liên kết"
11. "Lấy tài khoản liên kết" → "Đặt emailVerified = true"
12. "Đặt emailVerified = true" → "Lưu tài khoản vào database"
13. "Lưu tài khoản vào database" → "Xóa token đã sử dụng"
14. "Xóa token đã sử dụng" → "Hiển thị trang xác thực thành công"
15. "Hiển thị trang xác thực thành công" → "Cung cấp link đến /login"
16. "Cung cấp link đến /login" → End: "Người dùng có thể đăng nhập"

---

### 2.2. Gửi Lại Email Xác Thực

#### Các Nodes

**Start:**
- "Người dùng truy cập /resend-verification"

**Process Nodes:**
1. "Hiển thị form nhập email"
2. "Người dùng nhập địa chỉ email"
3. "Người dùng submit form"
4. "Hiển thị lỗi format"
5. "Tìm tài khoản theo email"
6. "Hiển thị thông báo chung thành công"
7. "Xóa các token xác thực cũ"
8. "Tạo UUID token mới"
9. "Đặt expiry = now + 24 giờ"
10. "Lưu token mới vào database"
11. "Xây dựng email xác thực"
12. "Gửi email với token mới"
13. "Hiển thị thông báo gửi lại thành công"

**Decision Nodes:**
1. "Email format hợp lệ?"
2. "Tài khoản tồn tại?"
3. "Đã xác thực rồi?"

**End Nodes:**
1. "Chuyển đến /verify-email-sent"

#### Kết Nối

1. Start → "Hiển thị form nhập email"
2. "Hiển thị form nhập email" → "Người dùng nhập địa chỉ email"
3. "Người dùng nhập địa chỉ email" → "Người dùng submit form"
4. "Người dùng submit form" → "Email format hợp lệ?"
5. "Email format hợp lệ?" → (No) → "Hiển thị lỗi format" → "Hiển thị form nhập email"
6. "Email format hợp lệ?" → (Yes) → "Tìm tài khoản theo email"
7. "Tìm tài khoản theo email" → "Tài khoản tồn tại?"
8. "Tài khoản tồn tại?" → (No) → "Hiển thị thông báo chung thành công" → End: "Chuyển đến /verify-email-sent"
9. "Tài khoản tồn tại?" → (Yes) → "Đã xác thực rồi?"
10. "Đã xác thực rồi?" → (Yes) → "Hiển thị thông báo chung thành công"
11. "Đã xác thực rồi?" → (No) → "Xóa các token xác thực cũ"
12. "Xóa các token xác thực cũ" → "Tạo UUID token mới"
13. "Tạo UUID token mới" → "Đặt expiry = now + 24 giờ"
14. "Đặt expiry = now + 24 giờ" → "Lưu token mới vào database"
15. "Lưu token mới vào database" → "Xây dựng email xác thực"
16. "Xây dựng email xác thực" → "Gửi email với token mới"
17. "Gửi email với token mới" → "Hiển thị thông báo gửi lại thành công"
18. "Hiển thị thông báo gửi lại thành công" → End: "Chuyển đến /verify-email-sent"

---

## LUỒNG 3: ĐẶT LẠI MẬT KHẨU

### 3.1. Yêu Cầu Quên Mật Khẩu

#### Các Nodes

**Start:**
- "Người dùng truy cập /forgot-password"

**Process Nodes:**
1. "Hiển thị form nhập email"
2. "Người dùng nhập email"
3. "Người dùng submit form"
4. "Hiển thị lỗi validation"
5. "Tìm tài khoản theo email"
6. "Hiển thị thông báo chung"
7. "Xóa các token reset password cũ"
8. "Tạo UUID reset token"
9. "Đặt expiry = now + 1 giờ"
10. "Đặt used = false"
11. "Lưu token vào password_reset_tokens"
12. "Xây dựng email reset password"
13. "Gửi email với link reset"
14. "Hiển thị thông báo thành công"

**Decision Nodes:**
1. "Email format hợp lệ?"
2. "Tài khoản tồn tại?"
3. "Tài khoản đang active?"

**End Nodes:**
1. "Kết thúc - bảo mật: không tiết lộ email"
2. "Người dùng kiểm tra email"

#### Kết Nối

1. Start → "Hiển thị form nhập email"
2. "Hiển thị form nhập email" → "Người dùng nhập email"
3. "Người dùng nhập email" → "Người dùng submit form"
4. "Người dùng submit form" → "Email format hợp lệ?"
5. "Email format hợp lệ?" → (No) → "Hiển thị lỗi validation" → "Hiển thị form nhập email"
6. "Email format hợp lệ?" → (Yes) → "Tìm tài khoản theo email"
7. "Tìm tài khoản theo email" → "Tài khoản tồn tại?"
8. "Tài khoản tồn tại?" → (No) → "Hiển thị thông báo chung" → End: "Kết thúc - bảo mật: không tiết lộ email"
9. "Tài khoản tồn tại?" → (Yes) → "Tài khoản đang active?"
10. "Tài khoản đang active?" → (No) → "Hiển thị thông báo chung"
11. "Tài khoản đang active?" → (Yes) → "Xóa các token reset password cũ"
12. "Xóa các token reset password cũ" → "Tạo UUID reset token"
13. "Tạo UUID reset token" → "Đặt expiry = now + 1 giờ"
14. "Đặt expiry = now + 1 giờ" → "Đặt used = false"
15. "Đặt used = false" → "Lưu token vào password_reset_tokens"
16. "Lưu token vào password_reset_tokens" → "Xây dựng email reset password"
17. "Xây dựng email reset password" → "Gửi email với link reset"
18. "Gửi email với link reset" → "Hiển thị thông báo thành công"
19. "Hiển thị thông báo thành công" → End: "Người dùng kiểm tra email"

---

### 3.2. Đặt Lại Mật Khẩu Với Token

#### Các Nodes

**Start:**
- "Người dùng click link: /reset-password?token=xxx"

**Process Nodes:**
1. "Hiển thị lỗi link không hợp lệ"
2. "Query bảng password_reset_tokens"
3. "Hiển thị lỗi hết hạn/không hợp lệ"
4. "Hiển thị lỗi đã sử dụng"
5. "Hiển thị form reset password"
6. "Người dùng nhập mật khẩu mới"
7. "Người dùng submit form"
8. "Hiển thị lỗi validation"
9. "Hiển thị yêu cầu độ mạnh"
10. "Hiển thị lỗi không khớp"
11. "Validate token lần nữa"
12. "Hash mật khẩu mới với BCrypt"
13. "Cập nhật password của tài khoản"
14. "Lưu tài khoản"
15. "Đánh dấu token.used = true"
16. "Lưu token"
17. "Hiển thị thông báo thành công"

**Decision Nodes:**
1. "Có tham số token?"
2. "Token tồn tại?"
3. "expiry_date > now?"
4. "Token used == false?"
5. "Validation passed?"
6. "Độ mạnh mật khẩu OK?"
7. "newPassword == confirmPassword?"
8. "Vẫn còn hợp lệ?"

**End Nodes:**
1. "Chuyển đến /login"
2. "Chuyển đến /forgot-password"

#### Kết Nối

1. Start → "Có tham số token?"
2. "Có tham số token?" → (No) → "Hiển thị lỗi link không hợp lệ" → End: "Chuyển đến /login"
3. "Có tham số token?" → (Yes) → "Query bảng password_reset_tokens"
4. "Query bảng password_reset_tokens" → "Token tồn tại?"
5. "Token tồn tại?" → (No) → "Hiển thị lỗi hết hạn/không hợp lệ" → End: "Chuyển đến /forgot-password"
6. "Token tồn tại?" → (Yes) → "expiry_date > now?"
7. "expiry_date > now?" → (No) → "Hiển thị lỗi hết hạn/không hợp lệ"
8. "expiry_date > now?" → (Yes) → "Token used == false?"
9. "Token used == false?" → (No) → "Hiển thị lỗi đã sử dụng" → End: "Chuyển đến /forgot-password"
10. "Token used == false?" → (Yes) → "Hiển thị form reset password"
11. "Hiển thị form reset password" → "Người dùng nhập mật khẩu mới"
12. "Người dùng nhập mật khẩu mới" → "Người dùng submit form"
13. "Người dùng submit form" → "Validation passed?"
14. "Validation passed?" → (No) → "Hiển thị lỗi validation" → "Hiển thị form reset password"
15. "Validation passed?" → (Yes) → "Độ mạnh mật khẩu OK?"
16. "Độ mạnh mật khẩu OK?" → (No) → "Hiển thị yêu cầu độ mạnh" → "Hiển thị form reset password"
17. "Độ mạnh mật khẩu OK?" → (Yes) → "newPassword == confirmPassword?"
18. "newPassword == confirmPassword?" → (No) → "Hiển thị lỗi không khớp" → "Hiển thị form reset password"
19. "newPassword == confirmPassword?" → (Yes) → "Validate token lần nữa"
20. "Validate token lần nữa" → "Vẫn còn hợp lệ?"
21. "Vẫn còn hợp lệ?" → (No) → "Hiển thị lỗi hết hạn/không hợp lệ"
22. "Vẫn còn hợp lệ?" → (Yes) → "Hash mật khẩu mới với BCrypt"
23. "Hash mật khẩu mới với BCrypt" → "Cập nhật password của tài khoản"
24. "Cập nhật password của tài khoản" → "Lưu tài khoản"
25. "Lưu tài khoản" → "Đánh dấu token.used = true"
26. "Đánh dấu token.used = true" → "Lưu token"
27. "Lưu token" → "Hiển thị thông báo thành công"
28. "Hiển thị thông báo thành công" → End: "Chuyển đến /login"

---

## LUỒNG 4: QUẢN LÝ TÀI KHOẢN

### 4.1. Xem Thông Tin Cá Nhân

#### Các Nodes

**Start:**
- "Người dùng truy cập /account/profile"

**Process Nodes:**
1. "Lấy authenticated principal"
2. "Trích xuất email từ OAuth2 token"
3. "Trích xuất username từ principal"
4. "Tìm tài khoản theo email"
5. "Tìm tài khoản theo username"
6. "Load thông tin tài khoản"
7. "Tạo ProfileForm DTO"
8. "Điền dữ liệu: username, fullName, phone, email, avatarUrl, birthDate, gender"
9. "Thêm profileForm vào model"
10. "Đặt activePage = profile"

**Decision Nodes:**
1. "OAuth2 hay Form auth?"
2. "Tìm thấy tài khoản?"

**End Nodes:**
1. "Chuyển đến /login?error=true"
2. "Render user/account-profile.html"

#### Kết Nối

1. Start → "Lấy authenticated principal"
2. "Lấy authenticated principal" → "OAuth2 hay Form auth?"
3. "OAuth2 hay Form auth?" → (OAuth2) → "Trích xuất email từ OAuth2 token"
4. "OAuth2 hay Form auth?" → (Form) → "Trích xuất username từ principal"
5. "Trích xuất email từ OAuth2 token" → "Tìm tài khoản theo email"
6. "Trích xuất username từ principal" → "Tìm tài khoản theo username"
7. "Tìm tài khoản theo email" → "Tìm thấy tài khoản?"
8. "Tìm tài khoản theo username" → "Tìm thấy tài khoản?"
9. "Tìm thấy tài khoản?" → (No) → End: "Chuyển đến /login?error=true"
10. "Tìm thấy tài khoản?" → (Yes) → "Load thông tin tài khoản"
11. "Load thông tin tài khoản" → "Tạo ProfileForm DTO"
12. "Tạo ProfileForm DTO" → "Điền dữ liệu: username, fullName, phone, email, avatarUrl, birthDate, gender"
13. "Điền dữ liệu..." → "Thêm profileForm vào model"
14. "Thêm profileForm vào model" → "Đặt activePage = profile"
15. "Đặt activePage = profile" → End: "Render user/account-profile.html"

---

### 4.2. Cập Nhật Thông Tin Cá Nhân Với Avatar

#### Các Nodes

**Start:**
- "Người dùng submit form cập nhật profile"

**Process Nodes:**
1. "Nhận dữ liệu ProfileForm + avatarFile"
2. "Hiển thị lỗi validation trên form"
3. "Lấy authenticated principal"
4. "Tìm tài khoản bằng helper method"
5. "Cập nhật: fullName, phone, birthDate, gender"
6. "Lưu tài khoản vào database"
7. "Lưu file vào thư mục uploads"
8. "Lấy đường dẫn file tương đối"
9. "Cập nhật account.avatar = filePath"
10. "Thêm flash message thành công"

**Decision Nodes:**
1. "Input validation passed?"
2. "Tìm thấy tài khoản?"
3. "File avatar được upload?"
4. "File không rỗng?"

**End Nodes:**
1. "Quay lại trang profile"
2. "Chuyển đến /login?error=true"
3. "Chuyển đến /account/profile"

#### Kết Nối

1. Start → "Nhận dữ liệu ProfileForm + avatarFile"
2. "Nhận dữ liệu ProfileForm + avatarFile" → "Input validation passed?"
3. "Input validation passed?" → (No) → "Hiển thị lỗi validation trên form" → End: "Quay lại trang profile"
4. "Input validation passed?" → (Yes) → "Lấy authenticated principal"
5. "Lấy authenticated principal" → "Tìm tài khoản bằng helper method"
6. "Tìm tài khoản bằng helper method" → "Tìm thấy tài khoản?"
7. "Tìm thấy tài khoản?" → (No) → End: "Chuyển đến /login?error=true"
8. "Tìm thấy tài khoản?" → (Yes) → "Cập nhật: fullName, phone, birthDate, gender"
9. "Cập nhật: fullName, phone, birthDate, gender" → "File avatar được upload?"
10. "File avatar được upload?" → (No) → "Lưu tài khoản vào database"
11. "File avatar được upload?" → (Yes) → "File không rỗng?"
12. "File không rỗng?" → (No) → "Lưu tài khoản vào database"
13. "File không rỗng?" → (Yes) → "Lưu file vào thư mục uploads"
14. "Lưu file vào thư mục uploads" → "Lấy đường dẫn file tương đối"
15. "Lấy đường dẫn file tương đối" → "Cập nhật account.avatar = filePath"
16. "Cập nhật account.avatar = filePath" → "Lưu tài khoản vào database"
17. "Lưu tài khoản vào database" → "Thêm flash message thành công"
18. "Thêm flash message thành công" → End: "Chuyển đến /account/profile"

---

## LUỒNG 5: QUẢN LÝ ADMIN

### 5.1. Truy Cập Admin Dashboard

#### Các Nodes

**Start:**
- "Admin truy cập /admin/dashboard"

**Process Nodes:**
1. "Load dữ liệu thống kê dashboard"
2. "Đếm tổng số sản phẩm"
3. "Đếm tổng số đơn hàng"
4. "Đếm tổng số người dùng"
5. "Tính tổng doanh thu"
6. "Lấy đơn hàng gần đây"
7. "Thêm tất cả dữ liệu vào model"
8. "Đặt pageTitle = Tổng quan - Admin"

**Decision Nodes:**
1. "Người dùng đã xác thực?"
2. "Role == ADMIN?"

**End Nodes:**
1. "Chuyển đến /login"
2. "Hiển thị 403 Access Denied"
3. "Render admin/dashboard.html"

#### Kết Nối

1. Start → "Người dùng đã xác thực?"
2. "Người dùng đã xác thực?" → (No) → End: "Chuyển đến /login"
3. "Người dùng đã xác thực?" → (Yes) → "Role == ADMIN?"
4. "Role == ADMIN?" → (No) → End: "Hiển thị 403 Access Denied"
5. "Role == ADMIN?" → (Yes) → "Load dữ liệu thống kê dashboard"
6. "Load dữ liệu thống kê dashboard" → "Đếm tổng số sản phẩm"
7. "Đếm tổng số sản phẩm" → "Đếm tổng số đơn hàng"
8. "Đếm tổng số đơn hàng" → "Đếm tổng số người dùng"
9. "Đếm tổng số người dùng" → "Tính tổng doanh thu"
10. "Tính tổng doanh thu" → "Lấy đơn hàng gần đây"
11. "Lấy đơn hàng gần đây" → "Thêm tất cả dữ liệu vào model"
12. "Thêm tất cả dữ liệu vào model" → "Đặt pageTitle = Tổng quan - Admin"
13. "Đặt pageTitle = Tổng quan - Admin" → End: "Render admin/dashboard.html"

---

### 5.2. Quản Lý Sản Phẩm

#### Các Nodes

**Start:**
- "Admin truy cập /admin/products"

**Process Nodes:**
1. "Load tất cả sản phẩm từ database"
2. "Thêm danh sách sản phẩm vào model"
3. "Chuyển đến /admin/products/create"
4. "Hiển thị form tạo sản phẩm"
5. "Admin điền thông tin sản phẩm"
6. "Admin submit form"
7. "Hiển thị lỗi validation"
8. "Lưu sản phẩm mới vào database"
9. "Load sản phẩm theo ID"
10. "Hiển thị form sửa với dữ liệu"
11. "Admin cập nhật các trường"
12. "Admin submit cập nhật"
13. "Hiển thị lỗi"
14. "Cập nhật sản phẩm trong database"
15. "Xóa sản phẩm khỏi database"

**Decision Nodes:**
1. "Đã xác thực & ADMIN?"
2. "Hành động của Admin?"
3. "Validation passed?" (tạo)
4. "Validation passed?" (sửa)
5. "Xác nhận xóa?"

**End Nodes:**
1. "Access Denied"
2. "Render admin/products.html"
3. "Chuyển đến /admin/products" (sau tạo/sửa/xóa)
4. "Hủy"

#### Kết Nối

1. Start → "Đã xác thực & ADMIN?"
2. "Đã xác thực & ADMIN?" → (No) → End: "Access Denied"
3. "Đã xác thực & ADMIN?" → (Yes) → "Load tất cả sản phẩm từ database"
4. "Load tất cả sản phẩm từ database" → "Thêm danh sách sản phẩm vào model"
5. "Thêm danh sách sản phẩm vào model" → End: "Render admin/products.html"
6. End: "Render admin/products.html" → "Hành động của Admin?"

**Luồng Tạo Mới:**
7. "Hành động của Admin?" → (Tạo mới) → "Chuyển đến /admin/products/create"
8. "Chuyển đến /admin/products/create" → "Hiển thị form tạo sản phẩm"
9. "Hiển thị form tạo sản phẩm" → "Admin điền thông tin sản phẩm"
10. "Admin điền thông tin sản phẩm" → "Admin submit form"
11. "Admin submit form" → "Validation passed?"
12. "Validation passed?" → (No) → "Hiển thị lỗi validation" → "Hiển thị form tạo sản phẩm"
13. "Validation passed?" → (Yes) → "Lưu sản phẩm mới vào database"
14. "Lưu sản phẩm mới vào database" → End: "Chuyển đến /admin/products"

**Luồng Sửa:**
15. "Hành động của Admin?" → (Sửa) → "Load sản phẩm theo ID"
16. "Load sản phẩm theo ID" → "Hiển thị form sửa với dữ liệu"
17. "Hiển thị form sửa với dữ liệu" → "Admin cập nhật các trường"
18. "Admin cập nhật các trường" → "Admin submit cập nhật"
19. "Admin submit cập nhật" → "Validation passed?"
20. "Validation passed?" → (No) → "Hiển thị lỗi" → "Hiển thị form sửa với dữ liệu"
21. "Validation passed?" → (Yes) → "Cập nhật sản phẩm trong database"
22. "Cập nhật sản phẩm trong database" → End: "Chuyển đến /admin/products"

**Luồng Xóa:**
23. "Hành động của Admin?" → (Xóa) → "Xác nhận xóa?"
24. "Xác nhận xóa?" → (No) → End: "Hủy"
25. "Xác nhận xóa?" → (Yes) → "Xóa sản phẩm khỏi database"
26. "Xóa sản phẩm khỏi database" → End: "Chuyển đến /admin/products"

---

### 5.3. Quản Lý Đơn Hàng, Danh Mục & Tài Khoản

> **Lưu ý**: Biểu đồ này kết hợp 3 module quản lý. Bạn có thể vẽ riêng hoặc gộp chung.

#### Các Nodes

**Start:**
- "Admin điều hướng đến phần quản lý"

**Decision Node Chính:**
- "Phần nào?"

**Luồng Đơn Hàng - Process Nodes:**
1. "Truy cập /admin/orders"
2. "Load tất cả đơn hàng từ database"
3. "Sắp xếp theo ngày tạo giảm dần"
4. "Hiển thị danh sách đơn hàng"
5. "Hiển thị chi tiết đơn hàng"
6. "Cập nhật trạng thái đơn hàng"
7. "Lưu thay đổi đơn hàng"

**Luồng Danh Mục - Process Nodes:**
1. "Truy cập /admin/categories"
2. "Load tất cả danh mục"
3. "Hiển thị danh sách danh mục"
4. "Tạo danh mục mới"
5. "Sửa danh mục"
6. "Xóa danh mục"
7. "Lưu danh mục"

**Luồng Tài Khoản - Process Nodes:**
1. "Truy cập /admin/accounts"
2. "Load tất cả tài khoản"
3. "Hiển thị danh sách tài khoản"
4. "Hiển thị profile người dùng"
5. "Bật/tắt cờ isActive"
6. "Reset failed attempts & unlock"
7. "Lưu thay đổi tài khoản"

**End Nodes:**
1. "Làm mới danh sách đơn hàng"
2. "Làm mới danh sách danh mục"
3. "Làm mới danh sách tài khoản"

#### Kết Nối - Đơn Hàng

1. Start → "Phần nào?"
2. "Phần nào?" → (Đơn hàng) → "Truy cập /admin/orders"
3. "Truy cập /admin/orders" → "Load tất cả đơn hàng từ database"
4. "Load tất cả đơn hàng từ database" → "Sắp xếp theo ngày tạo giảm dần"
5. "Sắp xếp theo ngày tạo giảm dần" → "Hiển thị danh sách đơn hàng"
6. "Hiển thị danh sách đơn hàng" → "Hành động của Admin?"
7. "Hành động của Admin?" → (Xem chi tiết) → "Hiển thị chi tiết đơn hàng"
8. "Hành động của Admin?" → (Cập nhật trạng thái) → "Cập nhật trạng thái đơn hàng"
9. "Cập nhật trạng thái đơn hàng" → "Lưu thay đổi đơn hàng"
10. "Lưu thay đổi đơn hàng" → End: "Làm mới danh sách đơn hàng"

#### Kết Nối - Danh Mục

1. "Phần nào?" → (Danh mục) → "Truy cập /admin/categories"
2. "Truy cập /admin/categories" → "Load tất cả danh mục"
3. "Load tất cả danh mục" → "Hiển thị danh sách danh mục"
4. "Hiển thị danh sách danh mục" → "Hành động của Admin?"
5. "Hành động của Admin?" → (Tạo) → "Tạo danh mục mới"
6. "Hành động của Admin?" → (Sửa) → "Sửa danh mục"
7. "Hành động của Admin?" → (Xóa) → "Xóa danh mục"
8. "Tạo danh mục mới" → "Lưu danh mục"
9. "Sửa danh mục" → "Lưu danh mục"
10. "Lưu danh mục" → End: "Làm mới danh sách danh mục"

#### Kết Nối - Tài Khoản

1. "Phần nào?" → (Tài khoản) → "Truy cập /admin/accounts"
2. "Truy cập /admin/accounts" → "Load tất cả tài khoản"
3. "Load tất cả tài khoản" → "Hiển thị danh sách tài khoản"
4. "Hiển thị danh sách tài khoản" → "Hành động của Admin?"
5. "Hành động của Admin?" → (Xem profile) → "Hiển thị profile người dùng"
6. "Hành động của Admin?" → (Bật/tắt Active) → "Bật/tắt cờ isActive"
7. "Hành động của Admin?" → (Mở khóa) → "Reset failed attempts & unlock"
8. "Bật/tắt cờ isActive" → "Lưu thay đổi tài khoản"
9. "Reset failed attempts & unlock" → "Lưu thay đổi tài khoản"
10. "Lưu thay đổi tài khoản" → End: "Làm mới danh sách tài khoản"

---

## LUỒNG 6: MUA SẮM

### 6.1. Duyệt Sản Phẩm & Xem Chi Tiết

#### Các Nodes

**Start:**
- "Người dùng truy cập /products"

**Process Nodes:**
1. "Load sản phẩm từ database"
2. "Lọc theo danh mục"
3. "Lọc theo khoảng giá"
4. "Lọc theo từ khóa tìm kiếm"
5. "Sắp xếp kết quả"
6. "Phân trang kết quả"
7. "Tiếp tục duyệt"
8. "Điều hướng đến /product/{id}"
9. "Load sản phẩm theo ID"
10. "Load hình ảnh sản phẩm"
11. "Load đánh giá sản phẩm"
12. "Load sản phẩm liên quan"
13. "Thêm vào giỏ hàng"

**Decision Nodes:**
1. "Có áp dụng bộ lọc?"
2. "Người dùng click sản phẩm?"
3. "Sản phẩm tồn tại?"
4. "Hành động người dùng?"

**End Nodes:**
1. "Hiển thị lưới sản phẩm"
2. "Hiển thị 404 error"
3. "Hiển thị trang chi tiết sản phẩm"

#### Kết Nối

1. Start → "Load sản phẩm từ database"
2. "Load sản phẩm từ database" → "Có áp dụng bộ lọc?"
3. "Có áp dụng bộ lọc?" → (Yes) → "Lọc theo danh mục"
4. "Lọc theo danh mục" → "Lọc theo khoảng giá"
5. "Lọc theo khoảng giá" → "Lọc theo từ khóa tìm kiếm"
6. "Lọc theo từ khóa tìm kiếm" → "Sắp xếp kết quả"
7. "Có áp dụng bộ lọc?" → (No) → "Sắp xếp kết quả"
8. "Sắp xếp kết quả" → "Phân trang kết quả"
9. "Phân trang kết quả" → End: "Hiển thị lưới sản phẩm"
10. End: "Hiển thị lưới sản phẩm" → "Người dùng click sản phẩm?"
11. "Người dùng click sản phẩm?" → (Yes) → "Điều hướng đến /product/{id}"
12. "Người dùng click sản phẩm?" → (No) → "Tiếp tục duyệt"
13. "Điều hướng đến /product/{id}" → "Load sản phẩm theo ID"
14. "Load sản phẩm theo ID" → "Sản phẩm tồn tại?"
15. "Sản phẩm tồn tại?" → (No) → End: "Hiển thị 404 error"
16. "Sản phẩm tồn tại?" → (Yes) → "Load hình ảnh sản phẩm"
17. "Load hình ảnh sản phẩm" → "Load đánh giá sản phẩm"
18. "Load đánh giá sản phẩm" → "Load sản phẩm liên quan"
19. "Load sản phẩm liên quan" → End: "Hiển thị trang chi tiết sản phẩm"
20. End: "Hiển thị trang chi tiết sản phẩm" → "Hành động người dùng?"
21. "Hành động người dùng?" → (Thêm vào giỏ) → "Thêm vào giỏ hàng"
22. "Hành động người dùng?" → (Xem sản phẩm khác) → "Điều hướng đến /product/{id}"
23. "Hành động người dùng?" → (Quay lại) → End: "Hiển thị lưới sản phẩm"

---

### 6.2. Giỏ Hàng & Thanh Toán

#### Các Nodes

**Start:**
- "Người dùng thêm sản phẩm vào giỏ"

**Process Nodes:**
1. "Tạo session mới"
2. "Lấy giỏ hàng từ session"
3. "Khởi tạo giỏ hàng rỗng"
4. "Cập nhật số lượng"
5. "Thêm item mới vào giỏ"
6. "Tính lại tổng giỏ hàng"
7. "Lưu giỏ hàng vào session"
8. "Cập nhật badge số lượng giỏ hàng"
9. "Điều hướng đến /cart"
10. "Hiển thị tất cả item trong giỏ"
11. "Cập nhật số lượng item"
12. "Xóa item khỏi giỏ"
13. "Điều hướng đến /checkout"
14. "Load địa chỉ người dùng"
15. "Hiển thị form checkout"
16. "Người dùng submit đơn hàng"
17. "Hiển thị lỗi validation"
18. "Tạo đơn hàng trong database"
19. "Xóa giỏ hàng khỏi session"
20. "Gửi email xác nhận đơn hàng"

**Decision Nodes:**
1. "Session người dùng tồn tại?"
2. "Sản phẩm đã có trong giỏ?"
3. "Người dùng điều hướng đến?"
4. "Hành động trong giỏ?"
5. "Người dùng đã xác thực?"
6. "Validation passed?"

**End Nodes:**
1. "Quay lại sản phẩm"
2. "Chuyển đến /login"
3. "Hiển thị trang thành công đơn hàng"

#### Kết Nối

1. Start → "Session người dùng tồn tại?"
2. "Session người dùng tồn tại?" → (No) → "Tạo session mới"
3. "Session người dùng tồn tại?" → (Yes) → "Lấy giỏ hàng từ session"
4. "Tạo session mới" → "Khởi tạo giỏ hàng rỗng"
5. "Lấy giỏ hàng từ session" → "Sản phẩm đã có trong giỏ?"
6. "Khởi tạo giỏ hàng rỗng" → "Sản phẩm đã có trong giỏ?"
7. "Sản phẩm đã có trong giỏ?" → (Yes) → "Cập nhật số lượng"
8. "Sản phẩm đã có trong giỏ?" → (No) → "Thêm item mới vào giỏ"
9. "Cập nhật số lượng" → "Tính lại tổng giỏ hàng"
10. "Thêm item mới vào giỏ" → "Tính lại tổng giỏ hàng"
11. "Tính lại tổng giỏ hàng" → "Lưu giỏ hàng vào session"
12. "Lưu giỏ hàng vào session" → "Cập nhật badge số lượng giỏ hàng"
13. "Cập nhật badge số lượng giỏ hàng" → "Người dùng điều hướng đến?"
14. "Người dùng điều hướng đến?" → (Xem giỏ) → "Điều hướng đến /cart"
15. "Người dùng điều hướng đến?" → (Tiếp tục mua) → End: "Quay lại sản phẩm"
16. "Điều hướng đến /cart" → "Hiển thị tất cả item trong giỏ"
17. "Hiển thị tất cả item trong giỏ" → "Hành động trong giỏ?"
18. "Hành động trong giỏ?" → (Cập nhật SL) → "Cập nhật số lượng item"
19. "Cập nhật số lượng item" → "Tính lại tổng giỏ hàng"
20. "Hành động trong giỏ?" → (Xóa item) → "Xóa item khỏi giỏ"
21. "Xóa item khỏi giỏ" → "Tính lại tổng giỏ hàng"
22. "Hành động trong giỏ?" → (Thanh toán) → "Người dùng đã xác thực?"
23. "Người dùng đã xác thực?" → (No) → End: "Chuyển đến /login"
24. "Người dùng đã xác thực?" → (Yes) → "Điều hướng đến /checkout"
25. "Điều hướng đến /checkout" → "Load địa chỉ người dùng"
26. "Load địa chỉ người dùng" → "Hiển thị form checkout"
27. "Hiển thị form checkout" → "Người dùng submit đơn hàng"
28. "Người dùng submit đơn hàng" → "Validation passed?"
29. "Validation passed?" → (No) → "Hiển thị lỗi validation"
30. "Hiển thị lỗi validation" → "Hiển thị form checkout"
31. "Validation passed?" → (Yes) → "Tạo đơn hàng trong database"
32. "Tạo đơn hàng trong database" → "Xóa giỏ hàng khỏi session"
33. "Xóa giỏ hàng khỏi session" → "Gửi email xác nhận đơn hàng"
34. "Gửi email xác nhận đơn hàng" → End: "Hiển thị trang thành công đơn hàng"

---

## PHỤ LỤC: MẸO VẼ TRÊN DRAW.IO

### 1. Sắp Xếp Layout

- **Luồng chính**: Vẽ theo chiều dọc từ trên xuống
- **Luồng lỗi**: Đặt ở bên phải hoặc bên trái
- **Decision nodes**: Căn giữa với các nhánh rẽ sang 2 bên
- **Khoảng cách**: Giữ khoảng cách đều giữa các nodes (50-80px)

### 2. Sử Dụng Màu Sắc

- **Start nodes**: Xanh lá nhạt (#E8F5E9)
- **End nodes thành công**: Xanh lá đậm hơn (#C8E6C9)
- **End nodes lỗi**: Đỏ nhạt (#FFEBEE)
- **Process nodes**: Xanh dương nhạt (#E3F2FD)
- **Decision nodes**: Vàng nhạt (#FFF9C4)
- **Nodes quan trọng**: Có thể tô màu cam nhạt (#FFE0B2)

### 3. Tính Năng Hữu Ích

- **Auto-layout**: Arrange → Layout → Vertical Flow
- **Align**: Arrange → Align → Center/Top/Bottom
- **Distribute**: Arrange → Distribute → Vertically
- **Copy style**: Ctrl+Shift+C (copy), Ctrl+Shift+V (paste)
- **Connector**: Sử dụng "Waypoint" connector cho mũi tên đẹp hơn

### 4. Export

- File → Export as → PNG (cho hình ảnh)
- File → Export as → PDF (cho tài liệu)
- File → Save as → .drawio (lưu để chỉnh sửa sau)

---

## KẾT LUẬN

Bạn đã có đầy đủ hướng dẫn để vẽ **13 Activity Diagrams** cho dự án ShopOMG trên Draw.io:

✅ **3 Diagrams Xác Thực**: Login, Registration, OAuth2  
✅ **2 Diagrams Email Verification**: Verify, Resend  
✅ **2 Diagrams Password Reset**: Forgot Password, Reset Password  
✅ **2 Diagrams Account Management**: View Profile, Update Profile  
✅ **3 Diagrams Admin Management**: Dashboard, Products, Orders/Categories/Accounts  
✅ **2 Diagrams Shopping**: Browse Products, Cart & Checkout  

**Thời gian ước tính**: 4-6 giờ để vẽ hoàn chỉnh tất cả các diagrams.

**Lưu ý**: Bạn có thể vẽ từng diagram riêng lẻ hoặc gộp tất cả vào một file Draw.io với nhiều pages (tabs).

Chúc bạn vẽ thành công! 🎨
