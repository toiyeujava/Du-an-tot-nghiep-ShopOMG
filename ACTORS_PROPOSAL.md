# ĐỀ XUẤT THÊM TÁC NHÂN CHO DỰ ÁN SHOPOMG

## Vấn đề hiện tại

Hiện tại hệ thống chỉ có **2 tác nhân:**
- **Admin** (Quản trị viên)
- **Khách hàng** (Customer/User)

→ **Quá ít** cho một hệ thống thương mại điện tử hoàn chỉnh

---

## ĐỀ XUẤT THÊM 3 TÁC NHÂN MỚI

### **Phương án 1: Thêm 3 tác nhân (Khuyến nghị)**

#### **1. Nhân viên Bán hàng (Sales Staff)**

**Vai trò:** Nhân viên hỗ trợ khách hàng và xử lý đơn hàng

**Chức năng:**
- ✅ Xem danh sách đơn hàng
- ✅ Xem chi tiết đơn hàng
- ✅ Cập nhật trạng thái đơn hàng (PENDING → CONFIRMED → SHIPPING)
- ✅ Hủy đơn hàng (với lý do)
- ✅ Gửi email thông báo cho khách hàng
- ✅ Xem thông tin khách hàng (chỉ đọc)
- ✅ Tìm kiếm đơn hàng theo mã, tên khách hàng, SĐT
- ❌ **KHÔNG** được: Xóa đơn hàng, quản lý sản phẩm, quản lý danh mục, xem báo cáo doanh thu

**Use Cases liên quan:**
- UC-10: Quản lý Đơn hàng (phiên bản giới hạn)
- UC-14 (mới): Hỗ trợ khách hàng

**Lý do cần có:**
- Phân tách quyền hạn rõ ràng
- Admin không cần xử lý tất cả đơn hàng
- Phù hợp với quy trình thực tế của shop

---

#### **2. Nhân viên Kho (Warehouse Staff)**

**Vai trò:** Quản lý tồn kho và cập nhật số lượng sản phẩm

**Chức năng:**
- ✅ Xem danh sách sản phẩm
- ✅ Xem số lượng tồn kho (ProductVariants)
- ✅ Cập nhật số lượng tồn kho
- ✅ Xem lịch sử nhập/xuất kho
- ✅ Tìm kiếm sản phẩm theo SKU, tên
- ❌ **KHÔNG** được: Thêm/xóa sản phẩm, thay đổi giá, quản lý danh mục

**Use Cases liên quan:**
- UC-15 (mới): Quản lý tồn kho
- UC-11: Quản lý Sản phẩm (chỉ xem và cập nhật số lượng)

**Lý do cần có:**
- Quản lý kho là chức năng quan trọng trong e-commerce
- Tách biệt quyền quản lý sản phẩm và quản lý kho
- Tránh nhầm lẫn khi cập nhật tồn kho

---

#### **3. Khách vãng lai (Guest)**

**Vai trò:** Người dùng chưa đăng ký/đăng nhập

**Chức năng:**
- ✅ Xem danh sách sản phẩm
- ✅ Xem chi tiết sản phẩm
- ✅ Tìm kiếm, lọc, sắp xếp sản phẩm
- ✅ Xem đánh giá sản phẩm
- ❌ **KHÔNG** được: Thêm vào giỏ hàng, đặt hàng, viết đánh giá

**Use Cases liên quan:**
- UC-08: Sản phẩm (chỉ xem)
- UC-16 (mới): Duyệt sản phẩm (Guest)

**Lý do cần có:**
- Phân biệt rõ khách vãng lai và khách hàng đã đăng ký
- Khuyến khích đăng ký để mua hàng
- Phù hợp với thực tế (nhiều người chỉ xem không mua)

---

### **Phương án 2: Thêm 2 tác nhân (Tối thiểu)**

Nếu muốn đơn giản hơn, chỉ thêm:
1. **Nhân viên Bán hàng** (Sales Staff)
2. **Khách vãng lai** (Guest)

→ Tổng cộng **4 tác nhân**

---

## SO SÁNH CÁC PHƯƠNG ÁN

| **Tiêu chí** | **Hiện tại (2 tác nhân)** | **Phương án 1 (5 tác nhân)** | **Phương án 2 (4 tác nhân)** |
|--------------|---------------------------|------------------------------|------------------------------|
| **Số lượng tác nhân** | 2 | 5 | 4 |
| **Phân quyền rõ ràng** | ❌ Chưa rõ | ✅ Rất rõ | ✅ Rõ |
| **Phù hợp thực tế** | ❌ Chưa | ✅ Rất phù hợp | ✅ Phù hợp |
| **Độ phức tạp** | Thấp | Vừa phải | Thấp |
| **Số Use Cases** | 13 | 16-17 | 14-15 |
| **Khuyến nghị** | ❌ | ✅ **Khuyến nghị** | ⚠️ Tối thiểu |

---

## DANH SÁCH TÁC NHÂN MỚI (5 TÁC NHÂN)

| **STT** | **Tên tác nhân** | **Vai trò** | **Quyền hạn** |
|---------|------------------|-------------|---------------|
| 1 | **Admin** | Quản trị viên | Toàn quyền hệ thống |
| 2 | **Nhân viên Bán hàng** | Sales Staff | Quản lý đơn hàng, hỗ trợ khách hàng |
| 3 | **Nhân viên Kho** | Warehouse Staff | Quản lý tồn kho |
| 4 | **Khách hàng** | Customer | Mua sắm, đánh giá, quản lý tài khoản |
| 5 | **Khách vãng lai** | Guest | Xem sản phẩm (chỉ đọc) |

---

## USE CASES MỚI CẦN THÊM

### **UC-14: Hỗ trợ Khách hàng**
- **Tác nhân:** Nhân viên Bán hàng
- **Mô tả:** Xem thông tin khách hàng, lịch sử đơn hàng, hỗ trợ giải đáp

### **UC-15: Quản lý Tồn kho**
- **Tác nhân:** Nhân viên Kho
- **Mô tả:** Cập nhật số lượng tồn kho, xem lịch sử nhập/xuất

### **UC-16: Duyệt Sản phẩm (Guest)**
- **Tác nhân:** Khách vãng lai
- **Mô tả:** Xem, tìm kiếm, lọc sản phẩm (chỉ đọc)

---

## MA TRẬN USE CASE - TÁC NHÂN MỚI

| **Use Case** | **Admin** | **Sales** | **Warehouse** | **Customer** | **Guest** |
|--------------|:---------:|:---------:|:-------------:|:------------:|:---------:|
| UC-01: Đăng nhập | ✅ | ✅ | ✅ | ✅ | - |
| UC-02: Đăng ký | - | - | - | ✅ | - |
| UC-03: Quên/Đặt lại mật khẩu | ✅ | ✅ | ✅ | ✅ | - |
| UC-04: Xác thực Email | - | - | - | ✅ | - |
| UC-05: Tài khoản cá nhân | ✅ | ✅ | ✅ | ✅ | - |
| UC-06: Đơn hàng của tôi | - | - | - | ✅ | - |
| UC-07: Giỏ hàng | - | - | - | ✅ | - |
| UC-08: Sản phẩm | ✅ | ✅ | ✅ | ✅ | ✅ |
| UC-09: Quản lý Tài khoản | ✅ | - | - | - | - |
| UC-10: Quản lý Đơn hàng | ✅ | ✅ (giới hạn) | - | - | - |
| UC-11: Quản lý Sản phẩm | ✅ | - | ✅ (chỉ số lượng) | - | - |
| UC-12: Quản lý Danh mục | ✅ | - | - | - | - |
| UC-13: Dashboard | ✅ | - | - | - | - |
| **UC-14: Hỗ trợ Khách hàng** | ✅ | ✅ | - | - | - |
| **UC-15: Quản lý Tồn kho** | ✅ | - | ✅ | - | - |
| **UC-16: Duyệt Sản phẩm** | - | - | - | - | ✅ |

---

## TRIỂN KHAI TRONG CODE

### **Bước 1: Cập nhật bảng Roles**

```sql
-- Thêm roles mới
INSERT INTO Roles (name) VALUES 
('ADMIN'),
('SALES_STAFF'),
('WAREHOUSE_STAFF'),
('USER'),
('GUEST');
```

### **Bước 2: Tạo Security Config**

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            // Public
            .requestMatchers("/", "/products/**", "/login", "/register").permitAll()
            
            // Customer
            .requestMatchers("/cart/**", "/checkout/**", "/account/**")
                .hasRole("USER")
            
            // Sales Staff
            .requestMatchers("/sales/orders/**")
                .hasAnyRole("SALES_STAFF", "ADMIN")
            
            // Warehouse Staff
            .requestMatchers("/warehouse/inventory/**")
                .hasAnyRole("WAREHOUSE_STAFF", "ADMIN")
            
            // Admin only
            .requestMatchers("/admin/**")
                .hasRole("ADMIN")
        );
        return http.build();
    }
}
```

### **Bước 3: Tạo Controllers mới**

```java
@Controller
@RequestMapping("/sales")
@PreAuthorize("hasAnyRole('SALES_STAFF', 'ADMIN')")
public class SalesController {
    // Xử lý đơn hàng cho nhân viên bán hàng
}

@Controller
@RequestMapping("/warehouse")
@PreAuthorize("hasAnyRole('WAREHOUSE_STAFF', 'ADMIN')")
public class WarehouseController {
    // Quản lý tồn kho
}
```

---

## KẾT LUẬN & KHUYẾN NGHỊ

### **Khuyến nghị: Chọn Phương án 1 (5 tác nhân)**

**Lý do:**
1. ✅ **Phân quyền rõ ràng:** Mỗi tác nhân có vai trò cụ thể
2. ✅ **Phù hợp thực tế:** Giống hệ thống thương mại điện tử thật
3. ✅ **Dễ mở rộng:** Có thể thêm tác nhân khác sau này
4. ✅ **Đáp ứng yêu cầu thầy:** Đủ tác nhân, không quá ít
5. ✅ **Không quá phức tạp:** Chỉ 5 tác nhân, dễ quản lý

**Tác nhân đề xuất:**
1. **Admin** - Quản trị viên (toàn quyền)
2. **Nhân viên Bán hàng** - Xử lý đơn hàng
3. **Nhân viên Kho** - Quản lý tồn kho
4. **Khách hàng** - Người mua hàng
5. **Khách vãng lai** - Người xem sản phẩm

**Use Cases tăng thêm:** 13 → 16 Use Cases

---

**Bạn có muốn tôi cập nhật lại file ACTORS_AND_USECASES.md với 5 tác nhân mới không?** 🎯
