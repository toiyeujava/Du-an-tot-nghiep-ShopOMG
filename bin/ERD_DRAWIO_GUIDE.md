# HƯỚNG DẪN VẼ ERD ĐẦY ĐỦ - SHOPOMG (CHEN NOTATION)

## Tổng quan

Tài liệu này hướng dẫn vẽ ERD đầy đủ với **14 mối quan hệ** sử dụng ký hiệu Chen (hình thoi) trên Draw.io.

---

## PHẦN 1: DANH SÁCH 15 THỰC THỂ

### **1. ROLES** (Vai trò)
```
┌─────────────────┐
│     ROLES       │
├─────────────────┤
│ id (PK)         │
│ name (UK)       │
└─────────────────┘
```

### **2. ACCOUNTS** (Tài khoản)
```
┌──────────────────────────────┐
│         ACCOUNTS             │
├──────────────────────────────┤
│ id (PK)                      │
│ username (UK)                │
│ password                     │
│ full_name                    │
│ email (UK)                   │
│ phone                        │
│ avatar                       │
│ role_id (FK)                 │
│ is_active                    │
│ email_verified               │
│ failed_login_attempts        │
│ account_locked_until         │
│ last_login                   │
│ birth_date                   │
│ gender                       │
│ created_at                   │
│ updated_at                   │
└──────────────────────────────┘
```

### **3. EMAIL_VERIFICATION_TOKENS** (Token xác thực email)
```
┌─────────────────────────────────┐
│ EMAIL_VERIFICATION_TOKENS       │
├─────────────────────────────────┤
│ id (PK)                         │
│ account_id (FK)                 │
│ token (UK)                      │
│ expiry_date                     │
│ created_at                      │
└─────────────────────────────────┘
```

### **4. PASSWORD_RESET_TOKENS** (Token đặt lại mật khẩu)
```
┌─────────────────────────────┐
│  PASSWORD_RESET_TOKENS      │
├─────────────────────────────┤
│ id (PK)                     │
│ account_id (FK)             │
│ token (UK)                  │
│ expiry_date                 │
│ used                        │
│ created_at                  │
└─────────────────────────────┘
```

### **5. ADDRESSES** (Địa chỉ)
```
┌─────────────────────┐
│     ADDRESSES       │
├─────────────────────┤
│ id (PK)             │
│ account_id (FK)     │
│ recipient_name      │
│ phone               │
│ detail_address      │
│ city                │
│ district            │
│ is_default          │
└─────────────────────┘
```

### **6. CATEGORIES** (Danh mục)
```
┌─────────────────┐
│   CATEGORIES    │
├─────────────────┤
│ id (PK)         │
│ name            │
│ slug            │
│ image           │
│ is_active       │
└─────────────────┘
```

### **7. PRODUCTS** (Sản phẩm)
```
┌─────────────────────┐
│     PRODUCTS        │
├─────────────────────┤
│ id (PK)             │
│ name                │
│ slug                │
│ description         │
│ material            │
│ origin              │
│ category_id (FK)    │
│ image               │
│ gender              │
│ price               │
│ discount            │
│ view_count          │
│ is_active           │
│ created_at          │
└─────────────────────┘
```

### **8. PRODUCT_IMAGES** (Ảnh sản phẩm)
```
┌─────────────────────┐
│  PRODUCT_IMAGES     │
├─────────────────────┤
│ id (PK)             │
│ product_id (FK)     │
│ image_url           │
└─────────────────────┘
```

### **9. PRODUCT_VARIANTS** (Biến thể sản phẩm)
```
┌─────────────────────┐
│ PRODUCT_VARIANTS    │
├─────────────────────┤
│ id (PK)             │
│ product_id (FK)     │
│ color               │
│ size                │
│ quantity            │
│ sku (UK)            │
└─────────────────────┘
```

### **10. PRODUCT_REVIEWS** (Đánh giá sản phẩm)
```
┌─────────────────────┐
│  PRODUCT_REVIEWS    │
├─────────────────────┤
│ id (PK)             │
│ product_id (FK)     │
│ account_id (FK)     │
│ rating              │
│ comment             │
│ review_date         │
└─────────────────────┘
```

### **11. VOUCHERS** (Mã giảm giá)
```
┌─────────────────────────┐
│      VOUCHERS           │
├─────────────────────────┤
│ id (PK)                 │
│ code (UK)               │
│ discount_percent        │
│ discount_amount         │
│ min_order_amount        │
│ max_discount_amount     │
│ start_date              │
│ end_date                │
│ quantity                │
│ is_active               │
└─────────────────────────┘
```

### **12. CARTS** (Giỏ hàng)
```
┌─────────────────────────┐
│        CARTS            │
├─────────────────────────┤
│ id (PK)                 │
│ account_id (FK)         │
│ product_variant_id (FK) │
│ quantity                │
│ created_at              │
└─────────────────────────┘
```

### **13. ORDERS** (Đơn hàng)
```
┌─────────────────────┐
│      ORDERS         │
├─────────────────────┤
│ id (PK)             │
│ account_id (FK)     │
│ order_date          │
│ status              │
│ total_amount        │
│ shipping_fee        │
│ discount_amount     │
│ final_amount        │
│ payment_method      │
│ shipping_address    │
│ receiver_name       │
│ receiver_phone      │
│ note                │
└─────────────────────┘
```

### **14. ORDER_DETAILS** (Chi tiết đơn hàng)
```
┌─────────────────────────┐
│    ORDER_DETAILS        │
├─────────────────────────┤
│ id (PK)                 │
│ order_id (FK)           │
│ product_variant_id (FK) │
│ product_name            │
│ price                   │
│ quantity                │
│ total                   │
└─────────────────────────┘
```

---

## PHẦN 2: 14 MỐI QUAN HỆ CHI TIẾT

### **MỐI QUAN HỆ 1: ROLES - ACCOUNTS**

**Tên mối quan hệ:** `Phân quyền`

**Sơ đồ:**
```
[ROLES] ────1──── ◇ Phân quyền ◇ ────N──── [ACCOUNTS]
```

**Cách vẽ trên Draw.io:**
1. Vẽ hình chữ nhật `ROLES` (bên trái)
2. Vẽ hình chữ nhật `ACCOUNTS` (bên phải)
3. Vẽ hình thoi ở giữa, ghi "Phân quyền"
4. Nối `ROLES` → hình thoi (ghi "1" gần ROLES)
5. Nối hình thoi → `ACCOUNTS` (ghi "N" gần ACCOUNTS)

**Ý nghĩa:** Một vai trò được phân cho nhiều tài khoản

**Khóa ngoại:** `ACCOUNTS.role_id` → `ROLES.id`

---

### **MỐI QUAN HỆ 2: ACCOUNTS - EMAIL_VERIFICATION_TOKENS**

**Tên mối quan hệ:** `Xác thực`

**Sơ đồ:**
```
[ACCOUNTS] ────1──── ◇ Xác thực ◇ ────N──── [EMAIL_VERIFICATION_TOKENS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Xác thực" giữa ACCOUNTS và EMAIL_VERIFICATION_TOKENS
2. Nối ACCOUNTS (1) → Xác thực → EMAIL_VERIFICATION_TOKENS (N)

**Ý nghĩa:** Một tài khoản có nhiều token xác thực email

**Khóa ngoại:** `EMAIL_VERIFICATION_TOKENS.account_id` → `ACCOUNTS.id`

---

### **MỐI QUAN HỆ 3: ACCOUNTS - PASSWORD_RESET_TOKENS**

**Tên mối quan hệ:** `Yêu cầu đặt lại`

**Sơ đồ:**
```
[ACCOUNTS] ────1──── ◇ Yêu cầu đặt lại ◇ ────N──── [PASSWORD_RESET_TOKENS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Yêu cầu đặt lại" giữa ACCOUNTS và PASSWORD_RESET_TOKENS
2. Nối ACCOUNTS (1) → Yêu cầu đặt lại → PASSWORD_RESET_TOKENS (N)

**Ý nghĩa:** Một tài khoản có nhiều yêu cầu đặt lại mật khẩu

**Khóa ngoại:** `PASSWORD_RESET_TOKENS.account_id` → `ACCOUNTS.id`

---

### **MỐI QUAN HỆ 4: ACCOUNTS - ADDRESSES**

**Tên mối quan hệ:** `Sở hữu địa chỉ`

**Sơ đồ:**
```
[ACCOUNTS] ────1──── ◇ Sở hữu địa chỉ ◇ ────N──── [ADDRESSES]
```

**Cách vẽ:**
1. Vẽ hình thoi "Sở hữu địa chỉ" giữa ACCOUNTS và ADDRESSES
2. Nối ACCOUNTS (1) → Sở hữu địa chỉ → ADDRESSES (N)

**Ý nghĩa:** Một tài khoản có nhiều địa chỉ giao hàng

**Khóa ngoại:** `ADDRESSES.account_id` → `ACCOUNTS.id`

**Cascade:** ON DELETE CASCADE

---

### **MỐI QUAN HỆ 5: ACCOUNTS - ORDERS**

**Tên mối quan hệ:** `Đặt hàng`

**Sơ đồ:**
```
[ACCOUNTS] ────1──── ◇ Đặt hàng ◇ ────N──── [ORDERS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Đặt hàng" giữa ACCOUNTS và ORDERS
2. Nối ACCOUNTS (1) → Đặt hàng → ORDERS (N)

**Ý nghĩa:** Một tài khoản đặt nhiều đơn hàng

**Khóa ngoại:** `ORDERS.account_id` → `ACCOUNTS.id`

---

### **MỐI QUAN HỆ 6: ACCOUNTS - CARTS**

**Tên mối quan hệ:** `Sở hữu giỏ hàng`

**Sơ đồ:**
```
[ACCOUNTS] ────1──── ◇ Sở hữu giỏ hàng ◇ ────N──── [CARTS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Sở hữu giỏ hàng" giữa ACCOUNTS và CARTS
2. Nối ACCOUNTS (1) → Sở hữu giỏ hàng → CARTS (N)

**Ý nghĩa:** Một tài khoản có nhiều items trong giỏ hàng

**Khóa ngoại:** `CARTS.account_id` → `ACCOUNTS.id`

**Cascade:** ON DELETE CASCADE

---

### **MỐI QUAN HỆ 7: ACCOUNTS - PRODUCT_REVIEWS**

**Tên mối quan hệ:** `Viết đánh giá`

**Sơ đồ:**
```
[ACCOUNTS] ────1──── ◇ Viết đánh giá ◇ ────N──── [PRODUCT_REVIEWS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Viết đánh giá" giữa ACCOUNTS và PRODUCT_REVIEWS
2. Nối ACCOUNTS (1) → Viết đánh giá → PRODUCT_REVIEWS (N)

**Ý nghĩa:** Một tài khoản viết nhiều đánh giá sản phẩm

**Khóa ngoại:** `PRODUCT_REVIEWS.account_id` → `ACCOUNTS.id`

---

### **MỐI QUAN HỆ 8: CATEGORIES - PRODUCTS**

**Tên mối quan hệ:** `Phân loại`

**Sơ đồ:**
```
[CATEGORIES] ────1──── ◇ Phân loại ◇ ────N──── [PRODUCTS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Phân loại" giữa CATEGORIES và PRODUCTS
2. Nối CATEGORIES (1) → Phân loại → PRODUCTS (N)

**Ý nghĩa:** Một danh mục chứa nhiều sản phẩm

**Khóa ngoại:** `PRODUCTS.category_id` → `CATEGORIES.id`

---

### **MỐI QUAN HỆ 9: PRODUCTS - PRODUCT_IMAGES**

**Tên mối quan hệ:** `Minh họa`

**Sơ đồ:**
```
[PRODUCTS] ────1──── ◇ Minh họa ◇ ────N──── [PRODUCT_IMAGES]
```

**Cách vẽ:**
1. Vẽ hình thoi "Minh họa" giữa PRODUCTS và PRODUCT_IMAGES
2. Nối PRODUCTS (1) → Minh họa → PRODUCT_IMAGES (N)

**Ý nghĩa:** Một sản phẩm có nhiều hình ảnh minh họa

**Khóa ngoại:** `PRODUCT_IMAGES.product_id` → `PRODUCTS.id`

**Cascade:** ON DELETE CASCADE

---

### **MỐI QUAN HỆ 10: PRODUCTS - PRODUCT_VARIANTS**

**Tên mối quan hệ:** `Có biến thể`

**Sơ đồ:**
```
[PRODUCTS] ────1──── ◇ Có biến thể ◇ ────N──── [PRODUCT_VARIANTS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Có biến thể" giữa PRODUCTS và PRODUCT_VARIANTS
2. Nối PRODUCTS (1) → Có biến thể → PRODUCT_VARIANTS (N)

**Ý nghĩa:** Một sản phẩm có nhiều biến thể (màu sắc, kích thước)

**Khóa ngoại:** `PRODUCT_VARIANTS.product_id` → `PRODUCTS.id`

**Cascade:** ON DELETE CASCADE

---

### **MỐI QUAN HỆ 11: PRODUCTS - PRODUCT_REVIEWS**

**Tên mối quan hệ:** `Nhận đánh giá`

**Sơ đồ:**
```
[PRODUCTS] ────1──── ◇ Nhận đánh giá ◇ ────N──── [PRODUCT_REVIEWS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Nhận đánh giá" giữa PRODUCTS và PRODUCT_REVIEWS
2. Nối PRODUCTS (1) → Nhận đánh giá → PRODUCT_REVIEWS (N)

**Ý nghĩa:** Một sản phẩm nhận nhiều đánh giá từ khách hàng

**Khóa ngoại:** `PRODUCT_REVIEWS.product_id` → `PRODUCTS.id`

---

### **MỐI QUAN HỆ 12: ORDERS - ORDER_DETAILS**

**Tên mối quan hệ:** `Bao gồm`

**Sơ đồ:**
```
[ORDERS] ────1──── ◇ Bao gồm ◇ ────N──── [ORDER_DETAILS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Bao gồm" giữa ORDERS và ORDER_DETAILS
2. Nối ORDERS (1) → Bao gồm → ORDER_DETAILS (N)

**Ý nghĩa:** Một đơn hàng bao gồm nhiều chi tiết sản phẩm

**Khóa ngoại:** `ORDER_DETAILS.order_id` → `ORDERS.id`

**Cascade:** ON DELETE CASCADE

---

### **MỐI QUAN HỆ 13: PRODUCT_VARIANTS - ORDER_DETAILS**

**Tên mối quan hệ:** `Xuất hiện trong`

**Sơ đồ:**
```
[PRODUCT_VARIANTS] ────1──── ◇ Xuất hiện trong ◇ ────N──── [ORDER_DETAILS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Xuất hiện trong" giữa PRODUCT_VARIANTS và ORDER_DETAILS
2. Nối PRODUCT_VARIANTS (1) → Xuất hiện trong → ORDER_DETAILS (N)

**Ý nghĩa:** Một biến thể sản phẩm xuất hiện trong nhiều chi tiết đơn hàng

**Khóa ngoại:** `ORDER_DETAILS.product_variant_id` → `PRODUCT_VARIANTS.id`

---

### **MỐI QUAN HỆ 14: PRODUCT_VARIANTS - CARTS**

**Tên mối quan hệ:** `Được thêm vào`

**Sơ đồ:**
```
[PRODUCT_VARIANTS] ────1──── ◇ Được thêm vào ◇ ────N──── [CARTS]
```

**Cách vẽ:**
1. Vẽ hình thoi "Được thêm vào" giữa PRODUCT_VARIANTS và CARTS
2. Nối PRODUCT_VARIANTS (1) → Được thêm vào → CARTS (N)

**Ý nghĩa:** Một biến thể sản phẩm được thêm vào nhiều giỏ hàng

**Khóa ngoại:** `CARTS.product_variant_id` → `PRODUCT_VARIANTS.id`

**Cascade:** ON DELETE CASCADE

---

## PHẦN 3: GỢI Ý LAYOUT TỔNG THỂ

### **Layout theo nhóm chức năng:**

```
┌─────────────────────────────────────────────────────────────────┐
│                      NHÓM TÀI KHOẢN & BẢO MẬT                   │
└─────────────────────────────────────────────────────────────────┘

                         [ROLES]
                            |
                      (Phân quyền)
                            |
    [EMAIL_VERIFICATION] ←(Xác thực)← [ACCOUNTS] →(Đặt hàng)→ [ORDERS]
            TOKENS                         |                      |
                                           |                (Bao gồm)
                                           |                      |
    [PASSWORD_RESET] ←(Yêu cầu đặt lại)←  |              [ORDER_DETAILS]
         TOKENS                            |                      ↑
                                           |                      |
                                           |              (Xuất hiện trong)
                                           |                      |
                                    (Sở hữu địa chỉ)     [PRODUCT_VARIANTS]
                                           |                      |
                                      [ADDRESSES]          (Được thêm vào)
                                           |                      |
                                    (Sở hữu giỏ hàng)        [CARTS]
                                           |
                                        [CARTS]


┌─────────────────────────────────────────────────────────────────┐
│                      NHÓM SẢN PHẨM & DANH MỤC                   │
└─────────────────────────────────────────────────────────────────┘

                      [CATEGORIES]
                            |
                      (Phân loại)
                            |
                       [PRODUCTS]
                            |
            ┌───────────────┼───────────────┐
            |               |               |
        (Minh họa)    (Có biến thể)  (Nhận đánh giá)
            |               |               |
    [PRODUCT_IMAGES] [PRODUCT_VARIANTS] [PRODUCT_REVIEWS]
                                           ↑
                                           |
                                    (Viết đánh giá)
                                           |
                                      [ACCOUNTS]
```

---

## PHẦN 4: QUY TẮC VẼ TRÊN DRAW.IO

### **1. Màu sắc:**
- **Thực thể (Entity):** Màu xanh nhạt `#E3F2FD`
- **Hình thoi (Relationship):** Màu vàng nhạt `#FFF9C4`
- **Đường nối:** Màu đen
- **Cardinality (1, N):** Màu đỏ, font đậm

### **2. Ký hiệu:**
- **Primary Key (PK):** Gạch chân hoặc in đậm
- **Foreign Key (FK):** Đánh dấu (FK) màu xanh
- **Unique Key (UK):** Đánh dấu (UK) màu cam
- **Cascade Delete:** Đường nối đậm hơn + chú thích "CASCADE"

### **3. Kích thước:**
- **Thực thể:** 200px × auto
- **Hình thoi:** 120px × 80px
- **Font size:** 11pt cho thuộc tính, 13pt cho tên thực thể

### **4. Căn chỉnh:**
- Căn giữa text trong hình thoi
- Căn trái thuộc tính trong thực thể
- Khoảng cách giữa các thực thể: tối thiểu 150px

---

## PHẦN 5: CHECKLIST HOÀN THÀNH

### **Thực thể (15):**
- [ ] ROLES
- [ ] ACCOUNTS
- [ ] EMAIL_VERIFICATION_TOKENS
- [ ] PASSWORD_RESET_TOKENS
- [ ] ADDRESSES
- [ ] CATEGORIES
- [ ] PRODUCTS
- [ ] PRODUCT_IMAGES
- [ ] PRODUCT_VARIANTS
- [ ] PRODUCT_REVIEWS
- [ ] VOUCHERS
- [ ] CARTS
- [ ] ORDERS
- [ ] ORDER_DETAILS

### **Mối quan hệ (14):**
- [ ] 1. ROLES → ACCOUNTS (Phân quyền)
- [ ] 2. ACCOUNTS → EMAIL_VERIFICATION_TOKENS (Xác thực)
- [ ] 3. ACCOUNTS → PASSWORD_RESET_TOKENS (Yêu cầu đặt lại)
- [ ] 4. ACCOUNTS → ADDRESSES (Sở hữu địa chỉ)
- [ ] 5. ACCOUNTS → ORDERS (Đặt hàng)
- [ ] 6. ACCOUNTS → CARTS (Sở hữu giỏ hàng)
- [ ] 7. ACCOUNTS → PRODUCT_REVIEWS (Viết đánh giá)
- [ ] 8. CATEGORIES → PRODUCTS (Phân loại)
- [ ] 9. PRODUCTS → PRODUCT_IMAGES (Minh họa)
- [ ] 10. PRODUCTS → PRODUCT_VARIANTS (Có biến thể)
- [ ] 11. PRODUCTS → PRODUCT_REVIEWS (Nhận đánh giá)
- [ ] 12. ORDERS → ORDER_DETAILS (Bao gồm)
- [ ] 13. PRODUCT_VARIANTS → ORDER_DETAILS (Xuất hiện trong)
- [ ] 14. PRODUCT_VARIANTS → CARTS (Được thêm vào)

---

**Chúc bạn vẽ ERD thành công! 🎨**
