# 📦 Hùng - Checkout & Payment Features

> **Ngày thực hiện:** 06/03/2026  
> **Người thực hiện:** Hùng  
> **Phạm vi:** Trang Checkout, QR Payment, Giỏ hàng

---

## 1. 🗑️ Xóa "Phí vận chuyển" khỏi trang Giỏ hàng (`cart.html`)

### Mô tả
Trước đó trang giỏ hàng hiển thị dòng **"Phí vận chuyển: Miễn phí"** khiến người dùng hiểu nhầm là ship luôn miễn phí. Đã xóa dòng này vì phí ship thực tế sẽ được tính chính xác ở trang Checkout.

### File thay đổi
- `src/main/resources/templates/user/cart.html` — xóa div hiển thị phí vận chuyển tĩnh.

---

## 2. 🚚 Phí vận chuyển động theo tỉnh thành (`checkout.html` + `CheckoutController.java`)

### Mô tả
Khi người dùng **chọn địa chỉ giao hàng**, hệ thống sẽ tự động tính phí ship dựa trên tỉnh thành so với trụ sở Shop tại **Đường Lê Văn Khương, Quận 12, TPHCM**.

### Bảng phí vận chuyển

| Vùng | Tỉnh/Thành phố | Phí | Badge màu |
|------|---------------|-----|-----------|
| Zone 1 – Nội thành | TPHCM (mọi biến thể: TP.HCM, TP. Hồ Chí Minh...) | **15,000đ** | 🟢 Xanh lá |
| Zone 2 – Lân cận | Bình Dương, Đồng Nai, Long An, Tây Ninh, BR-VT, Bình Phước | **25,000đ** | 🔵 Xanh dương |
| Zone 3 – Miền Nam | Cần Thơ, An Giang, Tiền Giang, Bến Tre, Vĩnh Long... | **35,000đ** | 🩵 Cyan |
| Zone 4 – Miền Trung | Đà Nẵng, Nghệ An, Quảng Nam, Tây Nguyên... | **45,000đ** | 🟡 Vàng |
| Zone 5 – Miền Bắc | Hà Nội, Hải Phòng và các tỉnh còn lại | **60,000đ** | 🔴 Đỏ |

### Cách hoạt động
1. User click chọn địa chỉ → JS gọi `selectAddress(id, name, phone, address, city)`
2. `updateShipping(city)` → `getZone(city)` dùng **substring matching** (không phải exact key) để detect TPHCM và các tỉnh
3. Phí ship được cập nhật **real-time** trên UI, lưu vào `<input hidden name="shippingFee">`
4. Khi submit form, `shippingFee` được gửi lên server
5. `CheckoutController.processCheckout()` đọc `shippingFee` → truyền vào redirect URL QR
6. `qrPaymentPage()` nhận `shippingFee` và cộng vào `order.finalAmount` để tạo QR đúng giá trị

### Fix bug TPHCM
Địa chỉ lưu trong DB thường là `"TP. Hồ Chí Minh"` (có dấu cách), không match key cũ `"TP.HCM"`. Đã sửa dùng `indexOf('hồ chí minh')` để detect mọi biến thể.

---

## 3. 🎉 Free Ship khi đơn ≥ 700,000đ (`checkout.html`)

### Mô tả
Khi tổng giá trị sản phẩm (**Tạm tính**) đạt **≥ 700,000đ**, phí vận chuyển tự động giảm về **0đ** và hiển thị badge thông báo.

### Cách hoạt động
- `FREE_SHIP_THRESHOLD = 700000`
- Kiểm tra 2 thời điểm:
  1. **Khi trang load** (`DOMContentLoaded`): đọc giá trị subtotal từ server → nếu ≥ 700k thì set ship = 0
  2. **Khi chọn địa chỉ** (`updateShipping()`): luôn kiểm tra subtotal trước khi áp zone fee
- Hiển thị: `shippingDisplay` = `"Miễn phí"`, badge = `"🎉 Free Ship! Đơn ≥ 700k"` màu xanh lá

---

## 4. 🎟️ Mã giảm giá OPENING – Giảm 20% đơn đầu tiên

### Mô tả
Mỗi tài khoản đã đăng nhập chỉ được dùng mã **`OPENING`** **một lần duy nhất** (cho đơn hàng đầu tiên), nhận ưu đãi **giảm 20%** trên tổng đơn (sản phẩm + phí ship).

### Frontend – Real-time, không cần F5
1. User nhập `OPENING` vào ô mã giảm giá → bấm **Áp dụng**
2. JS gọi AJAX: `GET /checkout/validate-coupon?code=OPENING`
3. Nếu hợp lệ → hiển thị thông báo xanh, tính giảm giá ngay:
   - `discount = 20% × (subtotal + shippingFee)`
   - Cập nhật ô **Giảm giá** và **Tổng cộng** ngay lập tức
4. Nếu không hợp lệ → hiển thị thông báo đỏ, không áp dụng
5. Khi user sửa mã → tự động reset giảm giá (`resetCoupon()`)

### Backend – Kiểm tra server-side
**Endpoint:** `GET /checkout/validate-coupon?code=`

**Logic kiểm tra:**
```
1. Account phải đang đăng nhập
2. Code phải là "OPENING" (case-insensitive)
3. Account chưa có bất kỳ đơn hàng nào trước đó
   → orderRepository.findByAccountId(accountId).size() == 0
```

**Response JSON:**
```json
// Hợp lệ:
{ "valid": true, "discountPercent": 20, "message": "Áp dụng thành công! Giảm 20% cho đơn hàng đầu tiên." }

// Không hợp lệ:
{ "valid": false, "message": "Mã OPENING chỉ áp dụng cho đơn hàng đầu tiên." }
```

**Thông báo lỗi:**
| Trường hợp | Thông báo |
|-----------|-----------|
| Mã sai | `"Mã giảm giá không hợp lệ."` |
| Đã từng đặt hàng | `"Mã OPENING chỉ áp dụng cho đơn hàng đầu tiên."` |
| Chưa đăng nhập | `"Bạn cần đăng nhập để dùng mã giảm giá."` |

### Công thức tính giá cuối (QR Amount)
```
QR Amount = Tạm tính (sản phẩm) + Phí ship - Giảm giá (20%)
```
**Ví dụ:**
- Sản phẩm: 530,000đ + Ship Miền Nam: 35,000đ = 565,000đ
- Giảm 20% × 565,000đ = 113,000đ
- **QR hiển thị: 452,000đ** ✅

---

## 5. 📱 Giao diện Phương thức thanh toán (`checkout.html`)

### Mô tả
Đồng nhất giao diện 2 ô thanh toán COD và QR để có cùng kích thước và mô tả rõ ràng.

| Phương thức | Tiêu đề | Mô tả nhỏ |
|------------|---------|-----------|
| COD | Thanh toán khi nhận hàng (COD) | _Thanh toán tiền mặt cho shipper khi nhận hàng_ |
| QR | Chuyển khoản qua mã QR | _Quét QR qua APP Ngân Hàng_ |

---

## 6. 🔗 Luồng dữ liệu tổng thể (End-to-End)

```
[Checkout Page]
  User chọn địa chỉ
    → JS: updateShipping(city) → fee (15k/25k/35k/45k/60k hoặc 0đ nếu ≥700k)
    → hidden: shippingFeeInput = fee

  User nhập OPENING + Áp dụng
    → AJAX: /checkout/validate-coupon → {valid: true, discountPercent: 20}
    → JS: discount = 20% × (sub + fee)
    → hidden: discountAmountInput = discount

  User bấm submit (QR mode)
    → POST /checkout/process?shippingFee=X&discountAmount=Y
    → Server tạo Order → redirect:/checkout/qr/{id}?shippingFee=X&discountAmount=Y

[QR Payment Page]
  amountLong = order.finalAmount + shippingFee - discountAmount
  VietQR URL = https://img.vietqr.io/image/MB-0961342609-compact.png?amount={amountLong}&addInfo=OMG-{orderId}
```

---

## 7. 📂 Danh sách file đã chỉnh sửa

| File | Loại | Nội dung thay đổi |
|------|------|------------------|
| `templates/user/cart.html` | HTML | Xóa dòng "Phí vận chuyển: Miễn phí" |
| `templates/user/checkout.html` | HTML + JS | Thêm phí ship động, free ship 700k, coupon OPENING real-time, UI payment method |
| `controller/user/CheckoutController.java` | Java | Thêm endpoint validate-coupon, đọc shippingFee + discountAmount trong processCheckout và qrPaymentPage |
