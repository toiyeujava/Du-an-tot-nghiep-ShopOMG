# Hùng - Tính Năng Đánh Giá Sản Phẩm (Review Feature)

> **Ngày thực hiện:** 02/03/2026  
> **Thành viên:** Nguyễn Minh Hùng  
> **Phạm vi:** Trang Đơn hàng (`/account/orders`) & Trang Đánh giá (`/account/reviews`)

---

## Mục lục

1. [Tổng quan tính năng](#1-tổng-quan-tính-năng)
2. [Tab "Cần đánh giá"](#2-tab-cần-đánh-giá)
3. [Popup Viết đánh giá](#3-popup-viết-đánh-giá)
4. [Luồng Hủy / Thoát dở dang](#4-luồng-hủy--thoát-dở-dang)
5. [Luồng Gửi thành công](#5-luồng-gửi-thành-công)
6. [Trang Đánh giá của tôi](#6-trang-đánh-giá-của-tôi)
7. [Backend - API & Database](#7-backend---api--database)
8. [Danh sách file đã thay đổi](#8-danh-sách-file-đã-thay-đổi)

---

## 1. Tổng quan tính năng

```
[Đơn hàng HOÀN THÀNH]
        │
        ▼
  Tab "Cần đánh giá"  ──►  Nút ⭐ "Đánh giá"
                                    │
                                    ▼
                          Popup "Viết đánh giá"
                          ┌─────────────────────┐
                          │ ← Viết đánh giá      │
                          │ [Ảnh SP] Tên / Biến thể │
                          │ ★ ★ ★ ★ ★            │
                          │ [Textarea 300 ký tự] │
                          │ [Upload ảnh/video]   │
                          │ [   Gửi   ]          │
                          └─────────────────────┘
                                    │
                    ┌───────────────┴────────────────┐
              Chưa chọn sao                    Đã chọn sao
                    │                               │
          Hiện cảnh báo                   POST /account/reviews/submit
          "Đây là mục bắt buộc"                     │
                                          ▼
                                  Lưu vào DB (ProductReviews)
                                          │
                                          ▼
                              Redirect → /account/reviews
                              (Hiển thị đánh giá vừa gửi)
```

---

## 2. Tab "Cần đánh giá"

**File:** `account-orders.html`

### Mô tả
Thêm một tab mới trong phần filter đơn hàng có tên **"Cần đánh giá"**. Tab này chỉ hiển thị các đơn hàng có trạng thái `COMPLETED` (Hoàn thành).

### Cách hoạt động
- Sử dụng `data-status="NEED_REVIEW"` để phân biệt tab này với các tab trạng thái khác.
- JavaScript lọc và ẩn/hiện các `.order-card` dựa trên trạng thái `COMPLETED`.
- Trên mỗi đơn hàng HOÀN THÀNH, nút **"⭐ Đánh giá"** được hiển thị ở footer.

### HTML liên quan
```html
<!-- Tab Cần đánh giá -->
<li class="nav-item">
    <a class="nav-link" data-status="NEED_REVIEW">Cần đánh giá</a>
</li>

<!-- Nút đánh giá trên đơn COMPLETED -->
<button class="btn btn-warning btn-sm review-btn">
    <i class="fas fa-star me-1"></i> Đánh giá
</button>
```

---

## 3. Popup Viết đánh giá

**File:** `account-orders.html` (cả HTML + CSS + JS)

### Các thành phần trong Popup

| Thành phần | Mô tả |
|---|---|
| **Header** | Tiêu đề "Viết đánh giá" căn giữa, nút `←` (Back) bên trái |
| **Thông tin sản phẩm** | Tự động load từ `data-*` của order-detail-item (ảnh, tên, màu, size, số lượng) |
| **Đánh giá sao** | 5 sao tương tác — hover sáng vàng, click để chọn |
| **Label sao** | Tự thay đổi: *Rất tệ / Tệ / Ổn / Tốt / Xuất sắc* |
| **Textarea** | Placeholder "Chia sẻ ý nghĩ của bạn", tối đa 300 ký tự, đếm ký tự realtime |
| **Upload ảnh/video** | Click vùng dashed border → chọn file → preview thumbnail; nút `×` xóa từng file; ô `+` thêm tiếp |
| **Nút Gửi** | Màu đỏ `#ff3366`, gửi review lên server |

### Data attributes trên order-detail-item
```html
<div class="order-detail-item"
     th:attr="data-product-id=${detail.productVariant.product.id},
              data-product-name=${detail.productName},
              data-product-image=${detail.productVariant.product.image},
              data-product-color=${detail.productVariant.color},
              data-product-size=${detail.productVariant.size},
              data-product-qty=${detail.quantity}">
```

### Validation
- Nếu bấm **Gửi** mà **chưa chọn sao** → hiện popup tối nhỏ *"Để gửi, hãy thêm đánh giá"* (tự ẩn sau 2 giây) + chữ đỏ *"Đây là mục bắt buộc"* bên dưới dòng sao.

---

## 4. Luồng Hủy / Thoát dở dang

**File:** `account-orders.html` (JS + modal `#confirmAbandonModal`)

### Mô tả
Khi người dùng đang điền review mà click nút `←` (Back):

```
Click ← 
    │
    ├── Chưa có nội dung gì  →  Đóng popup ngay (không hỏi)
    │
    └── Đã nhập sao / text / upload ảnh
            │
            ▼
    Hiện Popup xác nhận:
    ┌────────────────────────────────┐
    │  📋✓                          │
    │  Tiếp tục đánh giá để         │
    │  giúp những người khác        │
    │                               │
    │  Nếu bạn hủy bỏ, đánh giá    │
    │  của bạn sẽ không lưu lại     │
    │                               │
    │  [  Tiếp tục viết  ] ← đỏ    │
    │  [     Hủy bỏ      ] ← xám   │
    └────────────────────────────────┘
```

### Hành vi các nút
- **"Tiếp tục viết"** → chỉ đóng popup xác nhận, giữ nguyên popup review
- **"Hủy bỏ"** → đóng cả hai popup, mọi nội dung đã nhập bị xóa

---

## 5. Luồng Gửi thành công

**File:** `account-orders.html` (JS) + `AccountProfileController.java` (Backend)

### Cách hoạt động

1. Người dùng chọn ít nhất 1 sao → click **"Gửi"**
2. Nút bị disable, text đổi thành *"Đang gửi..."*
3. JavaScript gọi `fetch()` POST đến `/account/reviews/submit`:

```javascript
fetch('/account/reviews/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        productId: currentProductId,  // ID sản phẩm đầu tiên trong đơn
        rating: currentStarValue,     // 1-5
        comment: '...'               // Nội dung text (có thể rỗng)
    })
})
```

4. Server trả `{ success: true }` → đóng popup → redirect `/account/reviews`
5. Nếu lỗi → enable lại nút + hiện `alert()` thông báo lỗi

---

## 6. Trang Đánh giá của tôi

**File:** `account-reviews.html`

### Mô tả
Trang `/account/reviews` chỉ hiển thị **đánh giá đã gửi thành công**. Không còn hiển thị sản phẩm chờ đánh giá (những sản phẩm đó ở tab "Cần đánh giá" trên trang Đơn hàng).

### Giao diện mỗi đánh giá

```
[Ảnh SP]  Tên sản phẩm (link → trang SP)
          ★★★★☆  Tốt
          "Sản phẩm rất tốt, chất vải đẹp..."
          🕐 14:30 02/03/2026
```

### Trạng thái rỗng
Khi chưa có đánh giá nào → hiện icon sao + text *"Bạn chưa có đánh giá nào"* + nút **"Xem đơn hàng"**.

### Thymeleaf template (đoạn chính)
```html
<div th:each="review : ${myReviews}" ...>
    <img th:src="@{${review.product.image}}">
    <div>
        <a th:text="${review.product.name}"></a>
        <!-- Stars: i <= review.rating → filled, else outline -->
        <th:block th:each="i : ${#numbers.sequence(1, 5)}">
            <i th:class="${i <= review.rating} ? 
                'fas fa-star text-warning' : 'far fa-star text-muted'"></i>
        </th:block>
        <p th:text="${review.comment}"></p>
        <small th:text="${#temporals.format(review.reviewDate, 'HH:mm dd/MM/yyyy')}"></small>
    </div>
</div>
```

---

## 7. Backend - API & Database

### 7.1 Database Table

Bảng `ProductReviews` (đã có trong SQL script):

```sql
CREATE TABLE ProductReviews (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    product_id  INT NOT NULL,   -- FK → Products
    account_id  INT NOT NULL,   -- FK → Accounts
    rating      INT CHECK (rating BETWEEN 1 AND 5),
    comment     NVARCHAR(1000),
    review_date DATETIME DEFAULT GETDATE()
);
```

### 7.2 Entity: `ProductReview.java`

**Package:** `poly.edu.entity`

```java
@Entity
@Table(name = "ProductReviews")
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private Integer rating;      // 1-5
    private String comment;      // Tối đa 1000 ký tự
    private LocalDateTime reviewDate; // Auto set @PrePersist
}
```

### 7.3 Repository: `ProductReviewRepository.java`

**Package:** `poly.edu.repository`

| Method | Mô tả |
|---|---|
| `findByAccountIdOrderByReviewDateDesc(Integer accountId)` | Lấy tất cả đánh giá của user, mới nhất trước |
| `findByProductIdOrderByReviewDateDesc(Integer productId)` | Lấy đánh giá của một sản phẩm |
| `existsByProductIdAndAccountId(...)` | Kiểm tra user đã review sản phẩm chưa |
| `findByProductIdAndAccountId(...)` | Lấy đánh giá cụ thể để update (upsert) |
| `getAverageRating(Integer productId)` | Tính điểm trung bình (dùng cho trang sản phẩm) |
| `countByProductId(Integer productId)` | Đếm số lượt review |

### 7.4 API Endpoint: `POST /account/reviews/submit`

**Controller:** `AccountProfileController.java`

**Request body (JSON):**
```json
{
    "productId": 70,
    "rating": 4,
    "comment": "Sản phẩm rất tốt!"
}
```

**Response thành công:**
```json
{
    "success": true,
    "message": "Đánh giá đã được gửi thành công!"
}
```

**Response lỗi:**
```json
{
    "error": "Số sao không hợp lệ"
}
```

**Logic xử lý:**
- Xác thực đăng nhập (401 nếu chưa login)
- Validate rating 1-5
- **Upsert:** Nếu user đã review sản phẩm này rồi → cập nhật, chưa có → tạo mới
- Lưu vào DB và trả `200 OK`

### 7.5 API Endpoint: `GET /account/reviews`

**Controller:** `AccountProfileController.java`

- Lấy user từ `Principal`
- Query `findByAccountIdOrderByReviewDateDesc(accountId)`
- Truyền vào model attribute `myReviews`
- Render template `user/account-reviews`

---

## 8. Danh sách file đã thay đổi

### File mới tạo

| File | Loại | Mô tả |
|---|---|---|
| `entity/ProductReview.java` | Java Entity | Map bảng ProductReviews |
| `repository/ProductReviewRepository.java` | JPA Repository | Các query cho review |

### File cập nhật

| File | Loại | Thay đổi chính |
|---|---|---|
| `controller/user/AccountProfileController.java` | Java Controller | Thêm inject repo, cập nhật `reviews()`, thêm `submitReview()` |
| `repository/OrderRepository.java` | JPA Repository | Thêm `findByAccountIdAndStatusOrderByOrderDateDesc()` |
| `templates/user/account-orders.html` | Thymeleaf HTML | Thêm tab, nút đánh giá, modal review, modal xác nhận, JS đầy đủ |
| `templates/user/account-reviews.html` | Thymeleaf HTML | Hiển thị review thật, empty state |

---

> **Ghi chú:** Tính năng hiển thị đánh giá trên trang sản phẩm (product detail page) và tính năng ẩn nút "Cần đánh giá" sau khi đã review chưa được triển khai trong phiên này — có thể bổ sung sau.
