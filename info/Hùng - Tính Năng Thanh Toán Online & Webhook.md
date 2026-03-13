# Tính Năng Thanh Toán Online (VietQR) & Xử Lý Webhook (SePay)

Tài liệu này giải thích chi tiết về luồng hoạt động của tính năng thanh toán tự động trong hệ thống, bao gồm giao diện quét mã QR, cơ chế nhận Webhook từ SePay và cập nhật giao diện theo thời gian thực (Real-time) bằng SSE.

## 1. Giao diện Thanh toán QR (`qr-payment.html`)
- **Hiển thị thông tin**: Hệ thống hiển thị rõ Mã đơn hàng, Số tiền cần thanh toán, Nội dung chuyển khoản (bắt buộc theo cú pháp `OMG{orderId}`), và thông tin Ngân hàng nhận (MB Bank).
- **Mã QR tự động**: Mã QR được render dựa trên chuẩn của **VietQR**. Khi khách hàng dùng ví điện tử hoặc app ngân hàng để quét, hệ thống tự động điền sẵn số tiền và nội dung chuyển khoản, giúp tránh sai sót nhập liệu.
- **Thời gian chờ (Countdown)**: Giao diện thiết lập đồng hồ đếm ngược 10 phút. Nếu hết giờ, màn hình hiển thị thông báo "Hết hạn" nhưng khách vẫn có thể liên hệ Support nếu họ đã lỡ chuyển khoản trễ.
- **Real-time với SSE (Server-Sent Events)**: 
  - Khi mở trang, trình duyệt lập tức kết nối tới endpoint SSE (`/api/payment-events/{orderId}`).
  - Trình duyệt sẽ luôn ở trạng thái "lắng nghe". Ngay khi khách hàng chuyển khoản thành công và server xử lý xong Webhook, server sẽ đẩy tín hiệu `payment_success` xuống.
  - Màn hình lập tức hiển thị Modal "Thành công" và tuỳ chọn xem chi tiết giao dịch **má không cần tải lại trang**.

## 2. Webhook xử lý thanh toán (`SePayWebhookController.java`)
- **Endpoint nhận tín hiệu**: `POST /api/webhook/sepay`
- **Cơ chế xác thực (Authentication)**: Sử dụng Bearer Token so sánh với Key bí mật cấu hình trong `application.properties` (hoặc biến môi trường). Nếu Token không khớp, lập tức từ chối nhằm ngăn chặn request giả mạo.
- **Luồng xử lý (Workflow)**:
  1. Chỉ xử lý các giao dịch chiều vào (`transferType = "in"`), bỏ qua rút tiền hoặc báo lỗi.
  2. Dùng biểu thức chính quy (Regex) để trích xuất Order ID từ nội dung chuyển khoản theo pattern `OMG\d+`.
  3. Đối chiếu trong Database. Nếu thấy đơn hàng, tiếp tục lấy `transferAmount` (số tiền thực nhận) so sánh chặt chẽ với `finalAmount` (Tổng tiền cuối cùng của đơn hàng).
  4. **Thành công**: Cập nhật trạng thái `paymentStatus = "PAID"`, `status = "PENDING"`, lưu lịch sử thanh toán. Sau đó gọi `sseEmitterRegistry.notify(...)` để báo cho Frontend đang chờ.
  5. **Thất bại** (Sai số tiền, không tìm thấy đơn): Log lại hệ thống để xử lý ngoại lệ và push event `payment_error` (nếu khách chưa đóng màn hình) – Dữ liệu gốc của đơn hàng không bị thay đổi.
  6. **Phản hồi**: Luôn trả về HTTP 200 kèm JSON response để SePay hiểu là đã xử lý xong và không request lại (tránh webhook retry storm).
- **Security Bypass**: Endpoint được loại trừ khỏi tính năng CSRF protection của Spring Security để các hệ thống thứ ba (như SePay) có thể POST Data trực tiếp mà không cần Cookie/Token nội bộ của người dùng.

## 3. Server-Sent Events (`SseEmitterRegistry.java` & `SseController.java`)
- Là hệ thống Pub/Sub cục bộ quản lý các connection SSE của client được chia theo từng `orderId`. 
- Giải pháp này rẻ, gọn và hiệu quả hơn WebSocket đối với tác vụ chỉ cần Server thông báo một chiều xuống Client.
- Trình duyệt đóng vai trò Listener, còn Server đóng vai trò Emitter. Sự kiện được phát đi một cách bất đồng bộ và xuyên suốt ngay khi Webhook hoàn tất vòng đời.

## 4. Chi tiết giao dịch (`TransactionDetailController.java` & `transaction-detail.html`)
- Màn hình này cho phép khách hàng xem lại biên lai (Bill) sau khi đã thanh toán thành công.
- **Bảo mật hiển thị**: Hệ thống cố tình che giấu đi các thông tin cấu hình nhạy cảm (như API Key, toàn bộ log Webhook nội bộ) và chỉ hiển thị các trường an toàn như: *Mã đơn hàng, Mã tham chiếu ngân hàng, Số tiền, Trạng thái, và Thời gian thanh toán.*
- Nếu người truy cập chưa đăng nhập hoặc cố nhập orderId sai, hệ thống tự động redirect về trang chủ hoặc danh sách Đơn hàng của tôi.

---
**Tổng kết:** 
Kiến trúc này đảm bảo nguyên tắc: **Khách hàng không phải bấm nút "Xác nhận đã chuyển khoản"** hay F5 liên tục. Mọi thứ được đồng bộ và phản hồi dưới 2 giây ngay khi tiền về tài khoản ngân hàng, tạo trải nghiệm tiệm cận ví điện tử.
