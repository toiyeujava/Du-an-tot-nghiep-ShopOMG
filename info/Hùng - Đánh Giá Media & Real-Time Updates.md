# Hùng - Đánh Giá Media & Real-Time Updates

> **Ngày thực hiện:** 12/03/2026  
> **Phạm vi:** Trang Đánh giá sản phẩm, Trang Đơn mua, Backend API  

---

## 1. Upload Ảnh/Video vào Đánh giá Sản phẩm

### Mô tả
Khách hàng có thể đính kèm **ảnh (JPG/PNG/GIF)** hoặc **video (MP4)** khi viết hoặc chỉnh sửa đánh giá sản phẩm. Ảnh/video sẽ được lưu trên server và hiển thị công khai bên dưới bình luận.

### Các file đã thay đổi

#### Database
```sql
ALTER TABLE ProductReviews ADD media_url NVARCHAR(2000) NULL;
```
- Thêm cột `media_url` vào bảng `ProductReviews` để lưu đường dẫn file.

#### Backend

| File | Thay đổi |
|---|---|
| `ProductReview.java` | Thêm field `@Column(name = "media_url") private String mediaUrl;` |
| `AccountProfileController.java` | Thêm `@RequestParam MultipartFile media` vào `submitReview`, lưu file qua `FileService`, trả về `mediaUrl` trong JSON response |
| `ProductReviewController.java` | Thêm `mediaUrl` vào Map trả về khi GET `/api/reviews?productId=` |
| `ProductReviewRepository.java` | Thêm `findFirstByProductIdAndAccountId()` để tránh lỗi `NonUniqueResultException` khi cùng 1 sản phẩm được đánh giá nhiều lần |
| `WebMvcConfig.java` | Cấu hình serve file tĩnh từ thư mục `uploads/` bằng `addResourceHandlers` |

#### Frontend

| File | Thay đổi |
|---|---|
| `account-orders.html` | Đổi `fetch` review sang `FormData` thay vì JSON để gửi file; thêm UI preview ảnh/video |
| `account-reviews.html` | Thêm ô upload ảnh/video vào modal Chỉnh sửa đánh giá; hiển thị ảnh bên dưới comment trong thẻ đánh giá; cập nhật ảnh realtime không cần F5 |
| `product-detail.html` | Đổi `fetch` sang `FormData`; hiển thị ảnh/video đánh giá bên dưới mỗi bình luận |

### Luồng hoạt động
```
User chọn ảnh/video
  → Preview hiện ngay trong modal
  → Submit bằng FormData (multipart/form-data)
  → Backend nhận MultipartFile, gọi FileService.save()
  → File được lưu vào /uploads/reviews/
  → Đường dẫn /uploads/reviews/xxx.jpg được lưu vào cột media_url
  → Response trả về { success: true, review: { id, mediaUrl } }
  → Frontend cập nhật thẻ đánh giá ngay (không cần F5)
```

### Giới hạn file
- Định dạng: JPG, PNG, GIF, MP4
- Kích thước tối đa: **10MB**

---

## 2. Real-Time Cập nhật Trạng thái Đơn hàng (Polling)

### Mô tả
Trang **Đơn mua** tự động kiểm tra trạng thái các đơn hàng mỗi **10 giây** mà không cần F5. Khi Admin duyệt đơn, trạng thái badge thay đổi ngay trên trang của khách.

### Các file đã thay đổi

| File | Thay đổi |
|---|---|
| `AccountProfileController.java` | Thêm `GET /account/orders/statuses` trả về `[{orderId, status}]` dạng JSON |
| `account-orders.html` | Thêm `setInterval(pollOrderStatuses, 10000)` — gọi API mỗi 10s, so sánh trạng thái cũ/mới và cập nhật badge |

### API Polling

**Endpoint:** `GET /account/orders/statuses`  
**Auth:** Yêu cầu đăng nhập  
**Response:**
```json
[
  { "orderId": 1, "status": "PENDING" },
  { "orderId": 2, "status": "COMPLETED" }
]
```

### Luồng hoạt động
```
Mỗi 10 giây:
  → JS gọi GET /account/orders/statuses
  → So sánh status mới với data-status hiện tại của từng thẻ đơn hàng
  → Nếu thay đổi: cập nhật badge màu + hiệu ứng glow xanh
  → Nếu status = COMPLETED: tự động chèn nút "⭐ Viết đánh giá"
  → Nếu status = CANCELLED: ẩn nút Hủy đơn
```

### Mapping trạng thái → Badge

| Status | Badge |
|---|---|
| `PENDING` | 🟡 CHỜ XÁC NHẬN |
| `CONFIRMED` | 🔵 ĐÃ XÁC NHẬN |
| `SHIPPING` | 🟢 ĐANG GIAO |
| `COMPLETED` | ✅ HOÀN THÀNH |
| `CANCELLED` | 🔴 ĐÃ HỦY |

---

## 3. Nút "Viết đánh giá" Xuất hiện Tự động

### Mô tả
Khi đơn hàng chuyển sang trạng thái **COMPLETED** (do Admin duyệt), nút **"⭐ Viết đánh giá"** sẽ xuất hiện ngay trong footer của thẻ đơn hàng mà **không cần tải lại trang**.

### Cơ chế
- Được xử lý bởi hàm `updateOrderCard()` trong polling JS
- Khi `newStatus === 'COMPLETED'`, JS inject thêm nút review vào DOM
- Nút này liên kết với `data-order-id` để mở đúng modal đánh giá cho đơn đó

---

## 4. Xác nhận Thanh toán Online (SePay Webhook)

### Mô tả
Hệ thống tích hợp **SePay** để tự động xác nhận thanh toán chuyển khoản ngân hàng. Khi khách chuyển khoản thành công, SePay gửi webhook về server → đơn hàng tự động chuyển sang trạng thái **Đã thanh toán** → popup thành công hiển thị ngay trên trang.

### Các file liên quan

| File | Vai trò |
|---|---|
| `SePayWebhookController.java` | Nhận POST từ SePay, xác thực API key, parse dữ liệu thanh toán |
| `PaymentService.java` | Xử lý logic: tìm đơn hàng theo mã `transferContent`, cập nhật trạng thái thanh toán |
| `OrderController.java` / `PaymentController.java` | Expose endpoint polling `/api/payment/status/{orderId}` cho frontend |
| `checkout.html` | Poll status sau khi hiển thị QR, show popup khi thanh toán xong |
| `SecurityConfig.java` | Cho phép SePay gọi webhook không cần CSRF (permitAll cho `/webhook/sepay`) |

### Luồng hoạt động
```
Khách hàng quét mã QR VietQR
  → Chuyển khoản với nội dung = Mã đơn hàng (ví dụ: OMG12345)
  → Ngân hàng xác nhận giao dịch
  → SePay nhận giao dịch, gửi POST đến /webhook/sepay với thông tin:
      { transferContent, amount, bankAccount }
  → SePayWebhookController xác thực API key
  → Tìm đơn hàng có code trùng với transferContent
  → Cập nhật order.paymentStatus = PAID
  → Frontend đang poll /api/payment/status/{orderId} mỗi 3s
  → Nhận được status PAID → hiển thị popup "Thanh toán thành công!"
```

### Luồng QR Page (Checkout)
```
Trang QR hiển thị:
  ✅ Mã QR VietQR (tự sinh từ thông tin tài khoản + số tiền + mã đơn)
  ⚠️ Cảnh báo đỏ: "Giữ nguyên nội dung chuyển khoản"
  ⏳ Đồng hồ đếm ngược 10 phút
  → Nếu hết giờ: hiển thị thông báo hướng dẫn liên hệ shop qua chatbox
  → Nếu thanh toán thành công: popup xanh "Đơn hàng đã được xác nhận!"
```

### Cấu hình cần thiết (application.properties)
```properties
sepay.api-key=YOUR_SEPAY_API_KEY_HERE
```

> ⚠️ **Lưu ý bảo mật:** Không commit API key thật vào Git. Sử dụng biến môi trường hoặc file `.env` không được track.

---

## Tóm tắt các chức năng Hùng phụ trách

| STT | Chức năng | Trạng thái |
|---|---|---|
| 1 | Upload ảnh/video vào đánh giá | ✅ Hoàn thành |
| 2 | Hiển thị ảnh/video dưới bình luận (product-detail) | ✅ Hoàn thành |
| 3 | Chỉnh sửa đánh giá kèm ảnh (account-reviews) | ✅ Hoàn thành |
| 4 | Cập nhật ảnh realtime sau khi chỉnh sửa (không F5) | ✅ Hoàn thành |
| 5 | Real-time polling trạng thái đơn hàng (10s/lần) | ✅ Hoàn thành |
| 6 | Nút "Viết đánh giá" tự xuất hiện khi đơn hoàn thành | ✅ Hoàn thành |
| 7 | Xác nhận thanh toán online qua SePay webhook | ✅ Hoàn thành |
| 8 | QR countdown 10 phút + cảnh báo nội dung CK | ✅ Hoàn thành |
