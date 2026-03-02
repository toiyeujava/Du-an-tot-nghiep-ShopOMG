# BÁO CÁO KIỂM THỬ CHỨC NĂNG GIỎ HÀNG - DỰ ÁN SHOPOMG

**Ngày báo cáo:** 02/03/2026
**Người thực hiện:** Đức
**Vị trí:** Sinh viên thực hiện đồ án tốt nghiệp - FPT University

---

## 1. Mục tiêu kiểm thử
Xác nhận tính ổn định và chính xác của các tiến trình xử lý trong giỏ hàng, bao gồm các hoạt động CRUD, xử lý AJAX không tải lại trang và kiểm tra ràng buộc số lượng tồn kho.

## 2. Các tệp tin liên quan
- **Backend:** `CartController.java`, `CartService.java`
- **Frontend:** `user/cart.html` (Thymeleaf, JavaScript)

## 3. Nội dung kiểm thử chi tiết

### A. Chức năng Thêm vào giỏ hàng
- **Thêm sản phẩm (Form Post):** Kiểm tra khả năng nhận diện `productId`, `color`, `size` để tìm đúng `variantId`.
- **Thêm sản phẩm (AJAX - `/cart/add-ajax`):** Xác nhận dữ liệu trả về dạng JSON để cập nhật số lượng trên icon giỏ hàng mà không cần reload trang.
- **Xử lý đăng nhập:** Nếu người dùng chưa đăng nhập, hệ thống lưu lại `pendingCartAction` vào session và chuyển hướng đến trang `/login`.

### B. Chức năng Quản lý giỏ hàng (`/cart`)
- **Hiển thị danh sách:** Truy xuất chính xác tên sản phẩm, hình ảnh, màu sắc, kích thước và đơn giá (sau khi trừ discount).
- **Cập nhật số lượng (AJAX):**
    - Tăng/giảm số lượng trực tiếp trên giao diện.
    - Tự động tính toán lại `newItemTotal` (tổng tiền món hàng) và tổng thanh toán toàn bộ giỏ hàng qua JavaScript.
- **Kiểm tra tồn kho:**
    - Hệ thống chặn việc tăng số lượng vượt quá `productVariant.quantity`.
    - Hiển thị thông báo lỗi `stock-warning` và Modal thông báo nếu kho không đủ.

### C. Chức năng Xóa và Mua ngay
- **Xóa sản phẩm:** Kiểm tra chức năng xóa từng mục (`/remove/{id}`) và xóa toàn bộ giỏ hàng (`/clear`).
- **Xác nhận xóa:** Modal yêu cầu xác nhận trước khi xóa tất cả để đảm bảo an toàn dữ liệu.
- **Mua ngay (`/buy-now`):** Sử dụng AJAX để tạo giỏ hàng nhanh và trả về `cartId` để điều hướng ngay sang trang thanh toán.

## 4. Kết quả kiểm thử thực tế
| STT | Kịch bản kiểm thử | Kết quả mong đợi | Trạng thái |
|:---:|:---|:---|:---:|
| 1 | Thêm sản phẩm hợp lệ | Hiển thị thông báo thành công (Toast) | Thành công |
| 2 | Thay đổi số lượng | Tổng tiền đơn hàng tự động cập nhật | Thành công |
| 3 | Số lượng vượt quá tồn kho | Hiển thị thông báo "Đã đạt số lượng tối đa trong kho" | Thành công |
| 4 | Xóa sản phẩm khỏi giỏ | Mục sản phẩm biến mất, cập nhật lại tổng tiền | Thành công |
| 5 | Truy cập giỏ hàng khi chưa login | Chuyển hướng về trang đăng nhập | Thành công |

## 5. Nhận xét và Đánh giá
- **Ưu điểm:** Giao diện phản hồi nhanh nhờ tích hợp AJAX cho các thao tác cập nhật và mua ngay. Việc sử dụng `RedirectAttributes` giúp hiển thị thông báo (Success/Error) trực quan.
- **Hạn chế:** Cần tối ưu hóa thêm tốc độ truy vấn ở các phương thức trợ giúp `getCurrentAccountId()` nếu lượng người dùng tăng cao.