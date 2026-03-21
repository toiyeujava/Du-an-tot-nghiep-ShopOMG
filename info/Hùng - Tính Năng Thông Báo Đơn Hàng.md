# TÀI LIỆU HƯỚNG DẪN: TÍNH NĂNG THÔNG BÁO ĐƠN HÀNG (GIỐNG TIKTOK SHOP)

Chào bạn, đây là tài liệu được viết siêu chi tiết và dễ hiểu để giải thích cặn kẽ về cách hệ thống Thông Báo Đơn Hàng hoạt động. Giao diện và trải nghiệm được làm mô phỏng giống hệt TikTok Shop hay Shopee (có chuông báo đỏ, có popup danh sách, click vào mất chấm chưa đọc, và hiển thị cả ảnh sản phẩm).

Dưới đây là từng phần đã làm, được bóc tách rõ ràng nhé:

---

## PHẦN 1: DATABASE (CƠ SỞ DỮ LIỆU)

Để lưu trữ thông báo, chúng ta cần tạo một cái kho chứa (Bảng `Notifications`). Bảng này có nhiệm vụ lưu lại ai nhận thông báo, tiêu đề là gì, nội dung là gì, và quan trọng nhất là các **đường link ảnh sản phẩm**. 

**Tại sao lại lưu link ảnh (`ImageUrls`) thẳng vào đây?**
Bởi vì nếu mỗi lần bật cái chuông thông báo lên mà ứng dụng phải lặn lội đi tìm xem: "Đơn hàng này có mã bao nhiêu -> Đơn hàng này có bao nhiêu chi tiết -> Mỗi chi tiết là sản phẩm gì -> Sản phẩm đó ảnh ở đâu" thì sẽ cực kỳ chậm và lag (N+1 query). Do đó ta chơi chiêu: lúc tạo thông báo thì lôi hết ảnh ra gom thành 1 cục chữ (cách nhau bằng dấu phẩy) và ném hẳn vào cột `ImageUrls`. Lúc hiển thị chỉ việc cắt ra xài. Cực nhanh!

📌 **Đoạn code SQL để bạn tự tạo trong SQL Server:**
(Copy dán vào SSMS và ấn Execute)

```sql
-- 1. TẠO BẢNG NOTIFICATIONS CHỨA THÔNG BÁO
CREATE TABLE [dbo].[Notifications] (
    [Id] INT IDENTITY(1,1) PRIMARY KEY, -- Mã thông báo tự tăng
    [AccountId] INT NOT NULL, -- Mã ID của khách hàng nhận thông báo
    [OrderId] INT NULL, -- Mã ID của đơn hàng (nếu có)
    [Type] NVARCHAR(50) NOT NULL, -- Phân loại (Ví dụ: ORDER_PLACED, ORDER_SHIPPING)
    [Title] NVARCHAR(255) NOT NULL, -- Tiêu đề (VD: Xác nhận đơn hàng)
    [Content] NVARCHAR(MAX) NOT NULL, -- Nội dung chi tiết nhắn gửi
    [Link] NVARCHAR(255) NULL, -- Link để khi click vào sẽ bay tới trang nào
    [ImageUrls] NVARCHAR(MAX) NULL, -- CHỨA CÁC ĐƯỜNG LINK ẢNH DẠNG TEXT DÀI
    [IsRead] BIT NOT NULL DEFAULT 0, -- 0 là CHƯA ĐỌC, 1 là ĐÃ ĐỌC
    [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(), -- Giờ phút giây tạo ra
    
    -- Liên kết khóa ngoại (Foreign Key)
    CONSTRAINT [FK_Notifications_Accounts] FOREIGN KEY ([AccountId]) REFERENCES [dbo].[Accounts]([Id]),
    CONSTRAINT [FK_Notifications_Orders] FOREIGN KEY ([OrderId]) REFERENCES [dbo].[Orders]([Id])
);
GO

-- 2. TẠO 'ĐƯỜNG TẮT' (INDEX) ĐỂ TRUY VẤN CỰC NHANH
-- Đường tắt 1: Giúp lấy nhanh 10 thông báo mới nhất của 1 khách hàng
CREATE INDEX IX_Notifications_AccountId ON [dbo].[Notifications]([AccountId], [CreatedAt] DESC);
GO

-- Đường tắt 2: Giúp đếm cực nhanh số lượng "Thông báo CHƯA YÊU ĐƯỢC CHỨC" để nhảy số màu đỏ
CREATE INDEX IX_Notifications_AccountId_IsRead ON [dbo].[Notifications]([AccountId], [IsRead]);
GO
```

---

## PHẦN 2: TẦNG BACKEND (JAVA XỬ LÝ LOGIC NGẦM)

### 2.1 Class `Notification.java` (Entity)
Đây là "bản photocopy" của bảng SQL ở trên mang vào môi trường Java. Dùng để Spring Boot hiểu và đẩy dữ liệu lên xuống.
- Khai báo đúng từng trường (`@Column`) và ánh xạ `@ManyToOne` với bảng Accounts và Orders.

### 2.2 Đẻ Thông Báo Tự Động (`NotificationService.java`)
Ở đây mình thiết lập con robot (Service). Hai thời điểm nó sẽ tự động chạy:
1. **Khách chốt đơn thành công:** Chạy hàm `sendOrderPlacedNotification(...)`.
2. **Admin đổi trạng thái "Đang Giao":** Chạy hàm `sendOrderShippedNotification(...)`.

**Giải thích logic cắt ghép ảnh cực kỳ xịn mịn:**
Lúc bot tạo thông báo, nó sẽ mượn kho chứa `OrderDetailRepository` để chạy thằng thẳng vào DB tóm lấy toàn bộ cái list sản phẩm mà khách vừa mua trong đơn đó. Sau đó lầy các đường link ảnh nối lại với nhau bằng mã `,` ví dụ: `https://anh1.jpg,https://anh2.jpg`. Rồi nó dán cái chuỗi dài ngoằng này vào `notification.setImageUrls()`. Thế là xong, siêu an toàn mà không sợ lỗi LazyLoading (lỗi mà dữ liệu chưa kịp load lên bộ nhớ đã bị bắt đem đi xài).

### 2.3 Phục Vụ Giao Diện Gọi Báo "Đã Đọc" (`NotificationApiController.java`)
Giả sử có khách bấm vào chuông trên Web. Web nó cần báo vô Backend là: *"Ê mầy, khách nó bấm rồi, gỡ bỏ thông báo đi"*. 
Nó cung cấp 2 cái cửa (API):
- Đánh dấu 1 cái (`/api/notifications/{id}/read`)
- "Tao đọc hết rồi" (`/api/notifications/read-all`)

### 2.4 Mở Khóa Bảo Mật (`SecurityConfig.java`)
Spring Security bình thường như một ông bảo vệ khó tính, cứ hễ cục Frontend (chạy bằng Fetch/Ajax JS) gửi Data nặc danh vào thì ông ấy ném lỗi `403 Forbidden` do thiếu tem niêm phong (CSRF Token). Nên mình phải dặn ông bảo vệ: "*Bỏ chặn thư mục `/api/notifications/**` đi anh*".

---

## PHẦN 3: TẦNG GIAO DIỆN HIỂN THỊ (FRONTEND `header.html`)

Đây là cái mà khách hàng nhìn thấy, đòi hỏi phải mượt mã và đẹp nhé. Không được làm đơ lag giao diện hiện tại.

### Cục Cảnh Báo Màu Đỏ (Badge Đếm Số)
Lúc Web chạy lên, tự động kẹp biến đếm `@ModelAttribute("unreadNotificationCount")` vào trên góc Navbar. Nếu bằng 0 thì vứt class HTML đi không hiển thị. Nếu số > 0 thì in nguyên quả bóng đỏ có số nằm đè lên icon cái chuông góc phải.

### Bảng Menu Xổ Xuống Trực Quan Nhất (Dropdown)
Khi click vào chiếc chuông:
- Một cái khung dài `350px` hiện dầy đủ thông báo. Cuộn (Scroll) mượt mà thoải mái bằng chuột.
- **Nếu chưa đọc:** CSS đổ màu trắng sáng (`bg-white`) và châm mồi cho 1 cái "chấm tròn màu xanh lấp lánh" (`notif-dot`).
- **Nếu đã đọc rồi:** Chấm tròn biến mất, màu nền chìm vào vô hình (`bg-transparent`).

### Hiển Thị Kẻ Cắp - "Dàn Trận Đẹp Mắt 10 Sản Phẩm" (`flex-wrap`)
ĐÂY LÀ PHẦN XỊN NHẤT. Hồi nãy ở Backend mình giấu nguyên mảng ảnh vào 1 chuỗi dài ngăn bằng dấu phẩy đó. Giờ thì Thymeleaf có hàm `#strings.listSplit` chẻ nhỏ thằng đó ra lại thành 1 danh sách list link ảnh.
- Mình trải các ảnh đó vào thẻ `img` HTML, khoá kích thước hình vuông chuẩn `48x48px` và dùng `object-fit: cover` cắt cúp ảnh vô cùng sắc xảo chẳng thua gì Tiktok.
- Áp thẳng cái Flexbox `flex-wrap`: Giả sử khách mua 2 ảnh, nó đứng sát nhau. Khách tham mua đến tận 15 đôi giày, thì khi ảnh xếp đầy 1 hàng nó tự bo cua ngoan ngoãn xếp xuống hàng dưới trong cái khoảng khung `350px` đó. Không bao giờ tràn vỡ màn hình. Đỉnh chưa =))

### Trải Nghiệm 0 Giây Của Javascript
Khi mình click vào cái thông báo số lượng nhiều. Hàm Fetch gọi API ngầm rồi Spring Boot đá về code 'O-KÊ tao chỉnh rồi'. Javascipt nhẹ nhàng xóa "chấm xanh", giảm số đỏ của huy hiệu mà hệ thống Web vẫn mướt, không chớp màn nhấn F5 tí xíu nào. Cực kỳ mượt!

**=> Tổng kết lại: Một tính năng có vẻ đơn giản nhưng được chau truốt và quy hoạch logic cực kỳ sạch sẽ giúp Code dễ thở và Server không gánh tải tào lao.**
