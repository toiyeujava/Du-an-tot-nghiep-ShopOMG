# 📋 HƯỚNG DẪN CÀI ĐẶT VÀ SỬ DỤNG TÍNH NĂNG QUÊN MẬT KHẨU

## 🚀 Bước 1: Chạy Migration Script

Mở SQL Server Management Studio và chạy file migration:

```bash
# File: database_migration_forgot_password.sql
# Hoặc chạy bằng command:
sqlcmd -S localhost -d ShopOMG -U sa1 -P 123 -i database_migration_forgot_password.sql
```

## 📧 Bước 2: Cấu Hình Email

### Tùy chọn 1: Sử dụng Gmail

1. Tạo App Password:
   - Truy cập: https://myaccount.google.com/apppasswords
   - Tạo app password mới
   - Copy password

2. Cập nhật `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password-here
```

### Tùy chọn 2: Environment Variables (Khuyến nghị)

**Windows:**
```cmd
set EMAIL_USERNAME=your-email@gmail.com
set EMAIL_PASSWORD=your-app-password
```

**Linux/Mac:**
```bash
export EMAIL_USERNAME=your-email@gmail.com
export EMAIL_PASSWORD=your-app-password
```

## ▶️ Bước 3: Chạy Ứng Dụng

```bash
mvn spring-boot:run
```

## ✅ Bước 4: Test Tính Năng

1. Truy cập: http://localhost:8080/login
2. Click "Quên mật khẩu?"
3. Nhập email: `khach@gmail.com`
4. Kiểm tra inbox email
5. Click link trong email
6. Nhập mật khẩu mới
7. Đăng nhập với mật khẩu mới

---

## 🎯 Các Tính Năng Đã Triển Khai

✅ Form nhập email với validation  
✅ Gửi email reset password (HTML template đẹp)  
✅ Token bảo mật (UUID, 1 giờ, dùng 1 lần)  
✅ Form reset password với password strength indicator  
✅ Show/hide password toggle  
✅ Email enumeration protection  
✅ Transaction support  

---

## 📊 Endpoints Mới

- `GET /forgot-password` - Form nhập email
- `POST /forgot-password` - Gửi email
- `GET /reset-password?token=xxx` - Form reset
- `POST /reset-password` - Đổi mật khẩu

---

**Xem chi tiết:** [walkthrough.md](file:///C:/Users/24010/.gemini/antigravity/brain/ac386b10-b094-4dce-91cb-4a512e31138d/walkthrough.md)
