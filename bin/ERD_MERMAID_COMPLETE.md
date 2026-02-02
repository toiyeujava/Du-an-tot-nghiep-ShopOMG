# ERD MERMAID - SHOPOMG (ĐẦY ĐỦ 14 MỐI QUAN HỆ)

## Code Mermaid ERD

```mermaid
erDiagram
    %% ===== ĐỊNH NGHĨA CÁC THỰC THỂ =====
    
    ROLES {
        int id PK
        varchar name UK "ADMIN, USER"
    }
    
    ACCOUNTS {
        int id PK
        varchar username UK
        varchar password
        nvarchar full_name
        varchar email UK
        varchar phone
        nvarchar avatar
        int role_id FK
        date birth_date
        nvarchar gender
        bit is_active
        bit email_verified
        int failed_login_attempts
        datetime account_locked_until
        datetime last_login
        datetime created_at
        datetime updated_at
    }
    
    EMAIL_VERIFICATION_TOKENS {
        int id PK
        int account_id FK
        varchar token UK
        datetime expiry_date
        datetime created_at
    }
    
    PASSWORD_RESET_TOKENS {
        int id PK
        int account_id FK
        varchar token UK
        datetime expiry_date
        bit used
        datetime created_at
    }
    
    ADDRESSES {
        int id PK
        int account_id FK
        nvarchar recipient_name
        varchar phone
        nvarchar detail_address
        nvarchar city
        nvarchar district
        bit is_default
    }
    
    CATEGORIES {
        int id PK
        nvarchar name
        varchar slug UK
        nvarchar image
        bit is_active
    }
    
    PRODUCTS {
        int id PK
        nvarchar name
        varchar slug
        nvarchar description
        nvarchar material
        nvarchar origin
        int category_id FK
        nvarchar image
        nvarchar gender
        decimal price
        int discount
        int view_count
        bit is_active
        datetime created_at
    }
    
    PRODUCT_IMAGES {
        int id PK
        int product_id FK
        nvarchar image_url
    }
    
    PRODUCT_VARIANTS {
        int id PK
        int product_id FK
        nvarchar color
        nvarchar size
        int quantity
        varchar sku UK
    }
    
    PRODUCT_REVIEWS {
        int id PK
        int product_id FK
        int account_id FK
        int rating
        nvarchar comment
        datetime review_date
    }
    
    VOUCHERS {
        int id PK
        varchar code UK
        int discount_percent
        decimal discount_amount
        decimal min_order_amount
        decimal max_discount_amount
        datetime start_date
        datetime end_date
        int quantity
        bit is_active
    }
    
    CARTS {
        int id PK
        int account_id FK
        int product_variant_id FK
        int quantity
        datetime created_at
    }
    
    ORDERS {
        int id PK
        int account_id FK
        datetime order_date
        nvarchar status
        decimal total_amount
        decimal shipping_fee
        decimal discount_amount
        decimal final_amount
        nvarchar payment_method
        nvarchar shipping_address
        nvarchar receiver_name
        varchar receiver_phone
        nvarchar note
    }
    
    ORDER_DETAILS {
        int id PK
        int order_id FK
        int product_variant_id FK
        nvarchar product_name
        decimal price
        int quantity
        decimal total
    }
    
    %% ===== 14 MỐI QUAN HỆ (TIẾNG VIỆT) =====
    
    %% 1. Phân quyền
    ROLES ||--o{ ACCOUNTS : "Phan_Quyen"
    
    %% 2. Xác thực
    ACCOUNTS ||--o{ EMAIL_VERIFICATION_TOKENS : "Xac_Thuc"
    
    %% 3. Yêu cầu đặt lại
    ACCOUNTS ||--o{ PASSWORD_RESET_TOKENS : "Yeu_Cau_Dat_Lai"
    
    %% 4. Sở hữu địa chỉ
    ACCOUNTS ||--o{ ADDRESSES : "So_Huu_Dia_Chi"
    
    %% 5. Đặt hàng
    ACCOUNTS ||--o{ ORDERS : "Dat_Hang"
    
    %% 6. Sở hữu giỏ hàng
    ACCOUNTS ||--o{ CARTS : "So_Huu_Gio_Hang"
    
    %% 7. Viết đánh giá
    ACCOUNTS ||--o{ PRODUCT_REVIEWS : "Viet_Danh_Gia"
    
    %% 8. Phân loại
    CATEGORIES ||--o{ PRODUCTS : "Phan_Loai"
    
    %% 9. Minh họa
    PRODUCTS ||--o{ PRODUCT_IMAGES : "Minh_Hoa"
    
    %% 10. Có biến thể
    PRODUCTS ||--o{ PRODUCT_VARIANTS : "Co_Bien_The"
    
    %% 11. Nhận đánh giá
    PRODUCTS ||--o{ PRODUCT_REVIEWS : "Nhan_Danh_Gia"
    
    %% 12. Bao gồm
    ORDERS ||--o{ ORDER_DETAILS : "Bao_Gom"
    
    %% 13. Xuất hiện trong
    PRODUCT_VARIANTS ||--o{ ORDER_DETAILS : "Xuat_Hien_Trong"
    
    %% 14. Được thêm vào
    PRODUCT_VARIANTS ||--o{ CARTS : "Duoc_Them_Vao"
```

---

## Giải thích ký hiệu Mermaid

### **Cardinality (Bản số):**
- `||--o{` : Quan hệ 1-to-Many (1:N)
  - `||` : Exactly one (1)
  - `o{` : Zero or more (N)

### **Tên mối quan hệ:**
- Sử dụng dấu `_` thay cho khoảng trắng
- Ví dụ: "Phân quyền" → `Phan_Quyen`

---

## Danh sách 14 mối quan hệ

| **STT** | **Thực thể 1** | **Mối quan hệ** | **Thực thể 2** | **Tên Mermaid** |
|---------|----------------|-----------------|----------------|-----------------|
| 1 | ROLES | Phân quyền | ACCOUNTS | `Phan_Quyen` |
| 2 | ACCOUNTS | Xác thực | EMAIL_VERIFICATION_TOKENS | `Xac_Thuc` |
| 3 | ACCOUNTS | Yêu cầu đặt lại | PASSWORD_RESET_TOKENS | `Yeu_Cau_Dat_Lai` |
| 4 | ACCOUNTS | Sở hữu địa chỉ | ADDRESSES | `So_Huu_Dia_Chi` |
| 5 | ACCOUNTS | Đặt hàng | ORDERS | `Dat_Hang` |
| 6 | ACCOUNTS | Sở hữu giỏ hàng | CARTS | `So_Huu_Gio_Hang` |
| 7 | ACCOUNTS | Viết đánh giá | PRODUCT_REVIEWS | `Viet_Danh_Gia` |
| 8 | CATEGORIES | Phân loại | PRODUCTS | `Phan_Loai` |
| 9 | PRODUCTS | Minh họa | PRODUCT_IMAGES | `Minh_Hoa` |
| 10 | PRODUCTS | Có biến thể | PRODUCT_VARIANTS | `Co_Bien_The` |
| 11 | PRODUCTS | Nhận đánh giá | PRODUCT_REVIEWS | `Nhan_Danh_Gia` |
| 12 | ORDERS | Bao gồm | ORDER_DETAILS | `Bao_Gom` |
| 13 | PRODUCT_VARIANTS | Xuất hiện trong | ORDER_DETAILS | `Xuat_Hien_Trong` |
| 14 | PRODUCT_VARIANTS | Được thêm vào | CARTS | `Duoc_Them_Vao` |

---

## Hướng dẫn sử dụng với Draw.io

### **Cách 1: Import trực tiếp (Nếu Draw.io hỗ trợ)**

1. Copy toàn bộ code Mermaid từ phần "Code Mermaid ERD" ở trên
2. Mở Draw.io → File → Import → From Text → Mermaid
3. Paste code → Import

### **Cách 2: Qua Mermaid Live (Khuyến nghị)**

1. Truy cập https://mermaid.live/
2. Paste code Mermaid vào editor
3. Xem preview ERD
4. Click "Actions" → "Download SVG" hoặc "Download PNG"
5. Mở Draw.io → File → Import → Chọn file SVG/PNG vừa tải
6. Chỉnh sửa và lưu

### **Cách 3: Vẽ thủ công theo hướng dẫn**

Sử dụng file `ERD_DRAWIO_GUIDE.md` để vẽ từng thực thể và mối quan hệ với hình thoi

---

## Lưu ý

- **Mermaid không hỗ trợ hình thoi (diamond)** cho mối quan hệ như ký hiệu Chen
- Nếu muốn có hình thoi, bạn cần vẽ thủ công trên Draw.io theo hướng dẫn trong file `ERD_DRAWIO_GUIDE.md`
- Code Mermaid này dùng để **xem nhanh cấu trúc** và **export ảnh** để tham khảo

---

**Chúc bạn thành công! 🎨**
