# 📝 Tài Khoản Test - ShopOMG

## Tài Khoản Có Sẵn Trong Database

### 1. Tài Khoản Admin
- **Email:** `admin@gmail.com`
- **Password:** `123`
- **Role:** ADMIN
- **Trạng thái:** Active, Email Verified
- **Quyền:** Truy cập admin dashboard, quản lý toàn bộ hệ thống

### 2. Tài Khoản Khách Hàng
- **Email:** `khach@gmail.com`
- **Password:** `123`
- **Role:** USER
- **Trạng thái:** Active, Email Verified
- **Quyền:** Mua sắm, quản lý đơn hàng, profile

### 3. Tài Khoản Nguyễn Văn A
- **Username:** `$2a$10$Gc...` (encrypted)
- **Email:** `khach@gm...`
- **Password:** `0950900222`
- **Role:** USER

### 4. Tài Khoản Nguyễn Văn B
- **Username:** `$2a$10$Pxa...`
- **Email:** `khach@gm...`
- **Password:** `0930170295`
- **Role:** USER

### 5. Tài Khoản Nguyễn Minh
- **Username:** `hung123@g...`
- **Email:** `0730318907`
- **Password:** (encrypted)
- **Role:** USER

---

## Tài Khoản Để Test Tính Năng

### Test Email Verification
**Tạo tài khoản mới tại:** `/account/sign-up`
- Email: `test@gmail.com`
- Password: `123456`
- Sau khi đăng ký → Kiểm tra email → Click link verify

### Test Forgot Password
**Sử dụng tài khoản:**
- Email: `khach@gmail.com`
- Click "Quên mật khẩu?" → Nhập email → Check inbox

### Test Login Attempt Limiting
**Sử dụng tài khoản:**
- Email: `khach@gmail.com`
- Password sai: `wrongpassword`
- Thử 5 lần → Tài khoản bị khóa 15 phút

---

## Thông Tin Quan Trọng

### Mật Khẩu Mặc Định
Hầu hết tài khoản test đều dùng password: **`123`**

### Email Configuration
- SMTP: Gmail
- Email gửi: `240107.lam@gmail.com`
- App Password: `jxdz twph skah gkml`

### Database Connection
- Server: `localhost`
- Database: `ShopOMG`
- Username: `sa1`
- Password: `123`

---

## Ghi Chú

- Tất cả tài khoản hiện tại đã được set `email_verified = 1` (do migration)
- Tài khoản mới đăng ký sẽ có `email_verified = 0` và cần verify
- Admin account có thể truy cập `/admin/dashboard`
- User account chỉ có thể truy cập `/home` và các trang user
