# Phân Tích Nghiệp Vụ Còn Thiếu - ShopOMG

## Tổng Quan

Sau khi phân tích toàn bộ source code (Controller, Service, Repository, Entity, Template), dưới đây là danh sách các **tác vụ nghiệp vụ còn thiếu hoặc chưa hoàn thiện** trong hệ thống ShopOMG.

---

## 🔴 Mức Độ Cao (Thiếu Hoàn Toàn)

### 1. Hệ Thống Thanh Toán Online
- **Hiện tại**: Chỉ có thanh toán COD (tiền mặt khi nhận hàng)
- **Thiếu**: 
  - Tích hợp cổng thanh toán (VNPay, MoMo, ZaloPay)
  - Thanh toán ATM / thẻ quốc tế
  - Ví điện tử
  - QR Code thanh toán
  - Xử lý hoàn tiền (refund) khi hủy đơn đã thanh toán

### 2. Đánh Giá & Nhận Xét Sản Phẩm (Review)
- **Hiện tại**: Có template `account-reviews.html` nhưng **chưa có logic backend**
- **Thiếu**:
  - Entity `Review` (rating, comment, images, account, product)
  - `ReviewRepository`, `ReviewService`
  - Controller xử lý CRUD review
  - Chỉ cho phép đánh giá khi đã mua (đơn COMPLETED)
  - Hiển thị review trên trang chi tiết sản phẩm
  - Tính điểm trung bình (average rating)

### 3. Quản Lý Mã Giảm Giá / Voucher / Coupon
- **Hiện tại**: Có trường `discount` trên Product (giảm giá cố định từng sản phẩm)
- **Thiếu**:
  - Entity `Coupon` (code, type, value, minOrder, expiryDate, usageLimit)
  - Áp dụng mã giảm giá tại Checkout
  - Quản lý coupon (Admin)
  - Giới hạn sử dụng mỗi user
  - Coupon theo danh mục, sản phẩm cụ thể

### 4. Quản Lý Vận Chuyển
- **Hiện tại**: Chỉ có trường `shippingAddress` trong Order
- **Thiếu**:
  - Tính phí vận chuyển dựa trên địa chỉ/khoảng cách
  - Tích hợp đơn vị vận chuyển (GHN, GHTK, J&T)
  - Tracking number (mã vận đơn)
  - Theo dõi đơn hàng realtime
  - Ước tính thời gian giao hàng

### 5. Thông Báo (Notification System)
- **Hiện tại**: Chỉ có flash message trên trang
- **Thiếu**:
  - Entity `Notification` 
  - Thông báo realtime (WebSocket - đã có hạ tầng)
  - Thông báo khi đơn hàng thay đổi trạng thái
  - Thông báo khuyến mãi
  - Push notification (browser/email)
  - Đánh dấu đã đọc/chưa đọc
  - Icon bell với badge count trên header

---

## 🟡 Mức Độ Trung Bình (Có Nhưng Chưa Đủ)

### 6. Quản Lý Kho Hàng (Inventory)
- **Hiện tại**: Chỉ có `quantity` trên ProductVariant, giảm khi đặt hàng, hoàn khi hủy
- **Thiếu**:
  - Lịch sử nhập/xuất kho
  - Cảnh báo hết hàng (low stock alert)
  - Nhập hàng từ nhà cung cấp
  - Kiểm kê (inventory audit)
  - Báo cáo tồn kho

### 7. Hủy Đơn Hàng Phía User
- **Hiện tại**: Chỉ Admin mới hủy được đơn qua `AdminOrderController`
- **Thiếu**:
  - User tự hủy đơn khi trạng thái PENDING
  - Lý do hủy đơn
  - Chính sách hủy/hoàn trả

### 8. Tìm Kiếm Nâng Cao
- **Hiện tại**: `ProductService.searchProducts()` chỉ return `findAll()` (chưa implement)
- **Thiếu**:
  - Tìm kiếm theo nhiều tiêu chí kết hợp
  - Tìm kiếm gợi ý (autocomplete/suggestion)
  - Lịch sử tìm kiếm
  - Full-text search (Elasticsearch)

### 9. Wishlist / Danh Sách Yêu Thích
- **Hiện tại**: Không có
- **Thiếu**:
  - Entity `Wishlist` (accountId, productId)
  - Thêm/xóa sản phẩm yêu thích
  - Trang danh sách yêu thích
  - Button "Thêm vào yêu thích" trên product card

### 10. Báo Cáo & Thống Kê Nâng Cao
- **Hiện tại**: Dashboard cơ bản (tổng doanh thu, đơn hàng, export Excel)
- **Thiếu**:
  - Biểu đồ doanh thu theo thời gian (ngày/tuần/tháng/năm)
  - Báo cáo sản phẩm bán chạy/chậm
  - Báo cáo khách hàng (top buyers, customer retention)
  - Tỷ lệ chuyển đổi (conversion rate)
  - So sánh doanh thu giữa các kỳ
  - Export PDF

---

## 🟢 Mức Độ Nhẹ (Cải Thiện UX/Chất Lượng)

### 11. Phân Quyền Chi Tiết (RBAC)
- **Hiện tại**: Chỉ có 2 role: `ADMIN` và `USER`
- **Thiếu** (theo Use Case đã thiết kế):
  - Role `SALES_STAFF` (Nhân viên bán hàng)
  - Role `WAREHOUSE_STAFF` (Nhân viên kho)
  - Phân quyền chi tiết theo chức năng (permissions)

### 12. Quản Lý Hình Ảnh Sản Phẩm
- **Hiện tại**: Entity `ProductImage` tồn tại nhưng ít được sử dụng, chỉ có 1 ảnh chính (`product.image`)
- **Thiếu**:
  - Upload nhiều ảnh cho mỗi sản phẩm
  - Gallery ảnh trên trang chi tiết
  - Ảnh theo biến thể (color)
  - Resize/optimize ảnh tự động

### 13. Chat Hỗ Trợ Nâng Cao
- **Hiện tại**: Chat WebSocket lưu trong memory (`InMemoryChatService`)
- **Thiếu**:
  - Lưu trữ tin nhắn vào database (mất khi restart server)
  - Quản lý phiên chat (session)
  - Phân công nhân viên hỗ trợ
  - Lịch sử chat
  - Trạng thái online/offline
  - Đính kèm file/ảnh trong chat

### 14. SEO & Marketing
- **Thiếu**:
  - URL slug cho sản phẩm (VD: `/san-pham/ao-thun-nam` thay vì `/products/5`)
  - Meta tags cho SEO
  - Sitemap.xml
  - Social sharing (Open Graph tags)
  - Banner quảng cáo trang chủ (carousel)

### 15. Quản Lý Đơn Hàng Nâng Cao
- **Thiếu**:
  - In hóa đơn / phiếu giao hàng (PDF)
  - Ghi chú của khách hàng khi đặt hàng
  - Lý do hủy đơn
  - Lịch sử thay đổi trạng thái (audit trail cho order)
  - Đơn hoàn trả (Return/Refund)

### 16. Email Marketing & Tự Động Hóa
- **Hiện tại**: Email cho xác thực + reset password
- **Thiếu**:
  - Email xác nhận đơn hàng
  - Email thông báo trạng thái đơn hàng
  - Email nhắc giỏ hàng bị bỏ quên (abandoned cart)
  - Newsletter/email marketing

### 17. Bảo Mật Nâng Cao
- **Hiện tại**: BCrypt + OAuth2 + Login attempt lock
- **Thiếu**:
  - CSRF protection (đang tắt: `csrf.disable()`)
  - Rate limiting cho API
  - XSS protection cho input
  - Sanitize file upload
  - 2FA (Two-Factor Authentication)
  - Session management (invalidate all sessions)

### 18. Đa Ngôn Ngữ (i18n)
- **Hiện tại**: Hardcode tiếng Việt
- **Thiếu**:
  - Spring MessageSource
  - Chuyển đổi ngôn ngữ Việt/Anh

### 19. API cho Mobile App
- **Hiện tại**: Server-side rendering (Thymeleaf), chỉ có một vài AJAX endpoint
- **Thiếu**:
  - REST API đầy đủ (`/api/v1/...`)
  - JWT Authentication cho API
  - API Documentation (Swagger/OpenAPI)
  - CORS configuration cho mobile app

### 20. Performance & Caching
- **Thiếu**:
  - Redis cache cho sản phẩm hot
  - Query optimization (N+1 problem)
  - Lazy/eager loading strategy
  - Pagination chuẩn hóa
  - CDN cho static files

---

## Bảng Tổng Hợp Ưu Tiên

| # | Tác vụ | Mức Độ | Ưu Tiên | Ghi Chú |
|---|--------|--------|---------|---------|
| 1 | Thanh toán online | 🔴 | P0 | Cần thiết cho vận hành thực tế |
| 2 | Review sản phẩm | 🔴 | P0 | Template đã có, thiếu backend |
| 3 | Voucher/Coupon | 🔴 | P1 | Quan trọng cho marketing |
| 4 | Quản lý vận chuyển | 🔴 | P1 | Cần cho đơn hàng thực |
| 5 | Notification system | 🔴 | P1 | Đã có WebSocket infrastructure |
| 6 | Quản lý kho nâng cao | 🟡 | P1 | Nhập/xuất kho, cảnh báo |
| 7 | User hủy đơn | 🟡 | P1 | UX cơ bản cần có |
| 8 | Tìm kiếm nâng cao | 🟡 | P2 | Code placeholder đã có |
| 9 | Wishlist | 🟡 | P2 | Tính năng UX phổ biến |
| 10 | Thống kê nâng cao | 🟡 | P2 | Biểu đồ, so sánh |
| 11 | Phân quyền RBAC | 🟢 | P2 | Theo use case đã thiết kế |
| 12 | Multi-image product | 🟢 | P2 | Entity đã có sẵn |
| 13 | Chat nâng cao | 🟢 | P3 | Persist to DB |
| 14 | SEO & Marketing | 🟢 | P3 | URL slug, meta tags |
| 15 | Đơn hàng nâng cao | 🟢 | P2 | In hóa đơn, ghi chú |
| 16 | Email tự động hóa | 🟢 | P2 | Xác nhận đơn, nhắc nhở |
| 17 | Bảo mật nâng cao | 🟢 | P1 | CSRF đang tắt ⚠ |
| 18 | Đa ngôn ngữ | 🟢 | P3 | Nice-to-have |
| 19 | REST API cho mobile | 🟢 | P3 | Nếu cần mobile app |
| 20 | Caching & Performance | 🟢 | P3 | Optimization |

---

## Đề Xuất Roadmap

### Phase 1 - MVP Hoàn Chỉnh (2-3 tuần)
- ✅ Review sản phẩm (entity + backend + UI)
- ✅ User tự hủy đơn hàng
- ✅ Bật CSRF protection
- ✅ Email xác nhận đơn hàng

### Phase 2 - Nghiệp Vụ Nâng Cao (3-4 tuần)
- Tích hợp thanh toán VNPay
- Hệ thống Voucher/Coupon
- Notification system
- Quản lý kho nâng cao

### Phase 3 - UX & Marketing (2-3 tuần)
- Wishlist
- SEO improvements
- Thống kê biểu đồ
- Tìm kiếm nâng cao

### Phase 4 - Mở Rộng (4+ tuần)
- Tích hợp vận chuyển (GHN/GHTK)
- REST API + JWT
- Chat persist to DB
- Phân quyền RBAC chi tiết
