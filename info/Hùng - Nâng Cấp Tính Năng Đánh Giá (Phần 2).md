# Hùng - Nâng Cấp Tính Năng Đánh Giá (Phần 2)

> **Ngày thực hiện:** 02/03/2026  
> **Bổ sung sau:** `Hùng - Tính Năng Đánh Giá Sản Phẩm.md`  
> **Phạm vi:** Trạng thái nút đánh giá · UI/UX trang Reviews · Chỉnh sửa đánh giá

---

## Mục lục

1. [Logic nút hành động theo trạng thái đánh giá](#1-logic-nút-hành-động-theo-trạng-thái-đánh-giá)
2. [Nâng cấp UI/UX trang "Đánh giá của tôi"](#2-nâng-cấpuiux-trang-đánh-giá-của-tôi)
3. [Chức năng Chỉnh sửa đánh giá (Real-time Update)](#3-chức-năng-chỉnh-sửa-đánh-giá-real-time-update)
4. [Bug fix: Script bị loại khỏi layout](#4-bug-fix-script-bị-loại-khỏi-layout)
5. [Danh sách file thay đổi](#5-danh-sách-file-thay-đổi)

---

## 1. Logic nút hành động theo trạng thái đánh giá

**Files:** `AccountProfileController.java` · `account-orders.html`

### Mô tả

Trên trang `/account/orders`, các đơn hàng **HOÀN THÀNH** hiển thị nút đánh giá sản phẩm. Nút này có 2 trạng thái:

| Trạng thái | Nút | Màu | Hành động |
|---|---|---|---|
| **Chưa đánh giá** | ⭐ Viết đánh giá | Vàng (`btn-warning`) | Mở popup viết đánh giá |
| **Đã đánh giá** | ✓ Xem đánh giá | Xanh lá (`btn-outline-success`) | Link đến `/account/reviews` |

Ngoài ra, **Tab "Cần đánh giá"** chỉ hiển thị đơn hàng có ít nhất một sản phẩm **chưa được review** — sau khi gửi đánh giá, đơn tự động biến mất khỏi tab này.

### Luồng dữ liệu (Backend → Frontend)

```
Controller orders()
    │
    ├── Query all orders of user
    │
    ├── Query all reviews of user  → collect productIds vào Set<Integer>
    │
    └── Pass vào model:
            "orders"             → danh sách đơn hàng
            "reviewedProductIds" → Set<Integer> (productId đã review)
```

### Backend: `AccountProfileController.java`

```java
// Tạo Set chứa productId mà user đã review
Set<Integer> reviewedProductIds = productReviewRepository
    .findByAccountIdOrderByReviewDateDesc(acc.getId())
    .stream()
    .map(r -> r.getProduct().getId())
    .collect(Collectors.toSet());

model.addAttribute("orders", orders);
model.addAttribute("reviewedProductIds", reviewedProductIds);
```

### Template: `account-orders.html`

**Thêm `data-has-unreviewed` vào mỗi order-card:**
```html
<div th:each="order : ${orders}" class="order-card"
     th:attr="data-status=${order.status},
              data-order-id=${order.id},
              data-has-unreviewed=${order.status == 'COMPLETED' and
                  !reviewedProductIds.contains(order.orderDetails[0].productVariant.product.id)}">
```

**Nút đánh giá có điều kiện:**
```html
<span th:if="${order.status == 'COMPLETED'}">
    <!-- Chưa đánh giá: Viết đánh giá -->
    <button th:if="${!reviewedProductIds.contains(order.orderDetails[0].productVariant.product.id)}"
            class="btn btn-warning btn-sm review-btn" th:attr="data-order-id=${order.id}">
        <i class="fas fa-star me-1"></i>Viết đánh giá
    </button>
    <!-- Đã đánh giá: Xem đánh giá -->
    <a th:if="${reviewedProductIds.contains(order.orderDetails[0].productVariant.product.id)}"
       href="/account/reviews" class="btn btn-outline-success btn-sm">
        <i class="fas fa-check-circle me-1"></i>Xem đánh giá
    </a>
</span>
```

**Filter JavaScript tab "Cần đánh giá":**
```javascript
} else if (status === 'NEED_REVIEW') {
    // Chỉ hiện đơn COMPLETED có sản phẩm chưa review
    card.style.display =
        card.getAttribute('data-has-unreviewed') === 'true' ? 'block' : 'none';
}
```

---

## 2. Nâng cấp UI/UX trang "Đánh giá của tôi"

**File:** `account-reviews.html`

### Thiết kế Card Layout

Mỗi đánh giá được trình bày dưới dạng **Card** với 2 vùng chính:

```
┌──────────────────────────────────────────────┐
│  [Ảnh SP]  Tên sản phẩm (link → trang SP)    │
│            🕐 14:30 · 02/03/2026      [★4/5] │
│                                      [  Tốt] │
├──────────────────────────────────────────────┤
│  ★★★★☆                          [✏ Sửa]    │
│  "Nội dung nhận xét của người dùng..."       │
└──────────────────────────────────────────────┘
```

### Star Badge màu theo điểm

| Điểm | Class CSS | Màu nền | Màu chữ |
|---|---|---|---|
| 5 sao | `star-5` | `#e8f5e9` (xanh lá nhạt) | `#2e7d32` |
| 4 sao | `star-4` | `#f1f8e9` | `#558b2f` |
| 3 sao | `star-3` | `#fff3e0` (cam nhạt) | `#e65100` |
| 1-2 sao | `star-low` | `#ffebee` (đỏ nhạt) | `#c62828` |

### Empty State

Khi chưa có đánh giá nào, trang hiển thị:
- Vòng tròn vàng chứa icon ⭐
- Text "Chưa có đánh giá nào"
- Border dashed toàn trang (2px dashed #e9ecef)
- Nút "Xem đơn hàng" link về `/account/orders`

### Data Attributes trên card (dùng cho chức năng Edit)

```html
<div class="review-card"
     th:id="'review-card-' + ${review.id}"
     th:attr="data-review-id=${review.id},
              data-product-id=${review.product.id},
              data-product-name=${review.product.name},
              data-product-image=${review.product.image},
              data-rating=${review.rating},
              data-comment=${review.comment != null ? review.comment : ''}">
```

---

## 3. Chức năng Chỉnh sửa đánh giá (Real-time Update)

**File:** `account-reviews.html` (toàn bộ JS + modal HTML)

### Luồng tổng thể

```
Click ✏ Sửa
    │
    ▼
Đọc data-* từ card → pre-fill modal
    │
    ▼
Modal mở (tiêu đề: "Chỉnh sửa đánh giá")
    │   ┌─────────────────────────────────┐
    │   │ [Ảnh SP]  Tên sản phẩm          │
    │   │ ★★★★☆  (đã chọn từ trước)       │
    │   │ [Textarea chứa comment cũ]      │
    │   │ [  Cập nhật đánh giá  ]         │
    │   └─────────────────────────────────┘
    │
    ▼
Click "Cập nhật đánh giá"
    │
    ├── Chưa chọn sao → cảnh báo (giống luồng tạo mới)
    │
    └── Đã chọn sao → POST /account/reviews/submit (upsert)
                            │
                   ┌────────┴────────┐
               Thành công         Lỗi
                   │               │
           Real-time update    Toast đỏ
           card (không reload)
                   │
            Toast xanh lá
            "✓ Cập nhật
             thành công"
```

### Pre-fill dữ liệu vào modal

```javascript
// Lấy data từ card element
const savedRating  = parseInt(card.getAttribute('data-rating')) || 0;
const savedComment = card.getAttribute('data-comment') || '';

// Tô vàng đúng số sao
currentStarValue = savedRating;
document.querySelectorAll('.star-icon').forEach(s => {
    s.style.color = parseInt(s.getAttribute('data-star')) <= savedRating
        ? '#f5a623' : '#ccc';
});

// Đổ comment vào textarea
textarea.value = savedComment;
document.getElementById('reviewCharCount').textContent = savedComment.length + '/300';
```

### Real-time Card Update (không reload trang)

Sau khi server trả về `{ success: true }`:

```javascript
// 1. Cập nhật data-* để lần sửa tiếp theo đọc đúng  
card.setAttribute('data-rating', newRating);
card.setAttribute('data-comment', newComment);

// 2. Cập nhật star badge (class + số)
badge.className = 'star-badge ' + BADGE_CLASSES[newRating];
badge.querySelector('.badge-rating-num').textContent = newRating;

// 3. Cập nhật label (Tốt/Xuất sắc/...)
labelEl.textContent = STAR_LABELS[newRating - 1];
labelEl.style.color = STAR_COLORS[newRating];

// 4. Cập nhật hàng sao đầy đủ
starsDisplay.innerHTML = [1,2,3,4,5].map(i =>
    `<i class="${i <= newRating ? 'fas' : 'far'} fa-star"
        style="${i <= newRating ? 'color:#ffc107;' : 'color:#dee2e6;'}..."></i>`
).join('');

// 5. Cập nhật text comment
commentEl.textContent = newComment || 'Không có nhận xét';
```

### Toast Notification

```javascript
function showToast(msg, type) {
    const toast = document.getElementById('reviewToast');
    toast.textContent = msg;
    toast.style.display = 'flex';
    toast.style.background = type === 'success' ? '#1b5e20' : '#b71c1c';
    toast.style.color = '#fff';
    // Tự ẩn sau 3 giây
    toast._timer = setTimeout(() => toast.style.display = 'none', 3000);
}
// Gọi: showToast('✓ Cập nhật đánh giá thành công!', 'success');
```

Toast cố định góc **phải phía dưới** màn hình (`position:fixed; bottom:24px; right:24px`).

### Back Button — Phát hiện thay đổi

```javascript
document.getElementById('reviewBackBtn').addEventListener('click', function () {
    const hasChanges =
        currentStarValue !== parseInt(currentCardEl?.getAttribute('data-rating') || '0')
        || document.getElementById('reviewText').value.trim()
           !== (currentCardEl?.getAttribute('data-comment') || '');

    if (!hasChanges) {
        // Không thay đổi → đóng ngay, không hỏi
        bootstrap.Modal.getInstance(document.getElementById('reviewModal')).hide();
        return;
    }
    // Có thay đổi → hiện popup xác nhận
    abandonModal = new bootstrap.Modal(document.getElementById('confirmAbandonModal'));
    abandonModal.show();
});
```

---

## 4. Bug fix: Script bị loại khỏi layout

**File:** `account-reviews.html`

### Nguyên nhân

Layout dùng `th:replace="~{fragments/layout :: view(~{::title}, ~{::article})}"` — chỉ trích xuất **đúng thẻ `<article>`** và nội dung bên trong.

```html
<!-- SAI: </article> đóng sớm, style/script bị bỏ -->
    </article>         ← dòng 224 (quá sớm)

<style>...</style>     ← BỊ BỎ HOÀN TOÀN
<script>...</script>   ← BỊ BỎ HOÀN TOÀN

<!-- ĐÚNG: di chuyển </article> xuống sau script -->
<style>...</style>
<script>...</script>
</article>             ← đóng đúng chỗ
```

### Hậu quả nếu không sửa
- Không có CSS → card không đẹp
- Không có JS → click nút Sửa **không có phản ứng** gì cả (modal không mở)

### Fix
Di chuyển `</article>` xuống **sau** `</script>` để cả `<style>` lẫn `<script>` nằm trong phần nội dung được layout inject vào trang.

> **Lưu ý cho các trang khác:** Các trang Thymeleaf dùng `~{::article}` (hoặc `~{::section}`, `~{::main}`) **phải** đặt toàn bộ `<style>` và `<script>` bên trong thẻ wrapper tương ứng, không được để ngoài.

---

## 5. Danh sách file thay đổi

| File | Thay đổi |
|---|---|
| `controller/user/AccountProfileController.java` | `orders()` thêm query `reviewedProductIds` → pass vào model |
| `templates/user/account-orders.html` | Thêm `data-has-unreviewed` trên order-card; logic nút Viết/Xem đánh giá; cập nhật JS filter tab NEED_REVIEW |
| `templates/user/account-reviews.html` | Thiết kế lại toàn bộ với card layout; thêm nút Sửa; thêm modal Edit + JS pre-fill + real-time update + toast; fix bug `</article>` sớm |
