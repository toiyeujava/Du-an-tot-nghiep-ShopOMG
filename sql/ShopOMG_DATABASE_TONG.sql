/*
===========================================================================
   PROJECT: SHOP OMG - GRADUATION VERSION (REAL IMAGES EDITION)
   AUTHOR: Gemini & User
   DATE: 30/12/2025
   DESCRIPTION: 
   - Script tạo Database hoàn chỉnh.
   - Dữ liệu: Sản phẩm thật, Ảnh thật (Link sống), Giá thật.
===========================================================================
*/

USE ShopOMG;
GO

-- 1. TẠO DATABASE
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'ShopOMG')
BEGIN
    ALTER DATABASE ShopOMG SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ShopOMG;
END
GO

CREATE DATABASE ShopOMG;
GO
USE master;
GO
-- Chuyển database về chế độ đa người dùng
ALTER DATABASE ShopOMG SET MULTI_USER;
GO
USE ShopOMG;
GO

-- 2. TẠO CÁC BẢNG (TABLES)

-- Bảng Vai trò
CREATE TABLE Roles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Bảng Tài khoản
CREATE TABLE Accounts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Demo: 123456
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    avatar NVARCHAR(500) DEFAULT N'https://ui-avatars.com/api/?background=random&color=fff&name=User',
    role_id INT NOT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Accounts_Roles FOREIGN KEY (role_id) REFERENCES Roles(id)
);

-- Bảng Sổ địa chỉ
CREATE TABLE Addresses (
    id INT IDENTITY(1,1) PRIMARY KEY,
    account_id INT NOT NULL,
    recipient_name NVARCHAR(100),
    phone VARCHAR(15),
    detail_address NVARCHAR(255),
    city NVARCHAR(100),
    district NVARCHAR(100),
    is_default BIT DEFAULT 0,
    CONSTRAINT FK_Addresses_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE
);

-- Bảng Danh mục
CREATE TABLE Categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    slug VARCHAR(150) UNIQUE,
    image NVARCHAR(500),
    is_active BIT DEFAULT 1
);

-- Bảng Sản phẩm
CREATE TABLE Products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    slug VARCHAR(250),
    description NVARCHAR(MAX),
    material NVARCHAR(100),
    origin NVARCHAR(100),
    category_id INT NOT NULL,
    image NVARCHAR(500), -- Ảnh đại diện chính
    gender NVARCHAR(20), 
    price DECIMAL(18,2) NOT NULL,
    discount INT DEFAULT 0,
    view_count INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Products_Categories FOREIGN KEY (category_id) REFERENCES Categories(id)
);

-- Bảng Ảnh phụ sản phẩm
CREATE TABLE ProductImages (
    id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    CONSTRAINT FK_ProductImages_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE
);

-- Bảng Biến thể sản phẩm
CREATE TABLE ProductVariants (
    id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    color NVARCHAR(50) NOT NULL,
    size NVARCHAR(50) NOT NULL,
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    sku VARCHAR(50) UNIQUE,
    CONSTRAINT FK_ProductVariants_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE
);

-- Bảng Mã giảm giá
CREATE TABLE Vouchers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    discount_percent INT CHECK (discount_percent BETWEEN 0 AND 100),
    discount_amount DECIMAL(18,2),
    min_order_amount DECIMAL(18,2) DEFAULT 0,
    max_discount_amount DECIMAL(18,2),
    start_date DATETIME,
    end_date DATETIME,
    quantity INT DEFAULT 100,
    is_active BIT DEFAULT 1
);

-- Bảng Đơn hàng
CREATE TABLE Orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    account_id INT,
    order_date DATETIME DEFAULT GETDATE(),
    status NVARCHAR(50) DEFAULT 'PENDING',
    total_amount DECIMAL(18,2),
    shipping_fee DECIMAL(18,2) DEFAULT 0,
    discount_amount DECIMAL(18,2) DEFAULT 0,
    final_amount DECIMAL(18,2),
    payment_method NVARCHAR(50) DEFAULT 'COD',
    shipping_address NVARCHAR(500),
    receiver_name NVARCHAR(100),
    receiver_phone VARCHAR(20),
    note NVARCHAR(500),
    payment_status NVARCHAR(30) DEFAULT 'NOT_REQUIRED',
    payment_confirmed_at DATETIME2 NULL,
    payment_confirmed_by NVARCHAR(100) NULL,
    voucher_id INT NULL, -- Thêm trường lưu ID mã giảm giá
    CONSTRAINT FK_Orders_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id),
    CONSTRAINT FK_Orders_Vouchers FOREIGN KEY (voucher_id) REFERENCES Vouchers(id) -- Khóa ngoại liên kết bảng Vouchers
);

-- Bảng Chi tiết đơn hàng
CREATE TABLE OrderDetails (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_variant_id INT,
    product_name NVARCHAR(200),
    price DECIMAL(18,2),
    quantity INT NOT NULL CHECK (quantity > 0),
    total DECIMAL(18,2),
    CONSTRAINT FK_OrderDetails_Orders FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_OrderDetails_Variants FOREIGN KEY (product_variant_id) REFERENCES ProductVariants(id)
);

-- Bảng Giỏ hàng
CREATE TABLE Carts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    account_id INT NOT NULL,
    product_variant_id INT NOT NULL,
    quantity INT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Carts_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE,
    CONSTRAINT FK_Carts_Variants FOREIGN KEY (product_variant_id) REFERENCES ProductVariants(id) ON DELETE CASCADE
);

-- Bảng Đánh giá
CREATE TABLE ProductReviews (
    id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL,
    account_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(1000),
    review_date DATETIME DEFAULT GETDATE(),
    order_id INT NULL,
    media_url NVARCHAR(2000) NULL,
    CONSTRAINT FK_Reviews_Products FOREIGN KEY (product_id) REFERENCES Products(id),
    CONSTRAINT FK_Reviews_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id),
    CONSTRAINT FK_ProductReviews_Orders FOREIGN KEY (order_id) REFERENCES Orders(id)
);
GO

--new
-- 1. Tạo bảng InventoryLogs (Lịch sử Nhập/Xuất kho)
CREATE TABLE InventoryLogs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    timestamp DATETIME2 NOT NULL,
    variant_id INT NOT NULL,
    type VARCHAR(10) NOT NULL,
    old_quantity INT NOT NULL,
    change_amount INT NOT NULL,
    new_quantity INT NOT NULL,
    note NVARCHAR(500),
    account_id INT NOT NULL,
    CONSTRAINT FK_InventoryLog_Variant FOREIGN KEY (variant_id) REFERENCES ProductVariants(id),
    CONSTRAINT FK_InventoryLog_Account FOREIGN KEY (account_id) REFERENCES Accounts(id)
);
GO

CREATE TABLE LookbookPosts (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    account_id      INT NOT NULL,
    product_id      INT NOT NULL,
    order_id        INT NOT NULL,
    image_path      NVARCHAR(500) NOT NULL,
    caption         NVARCHAR(500),
    status          NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    like_count      INT DEFAULT 0,
    created_at      DATETIME2 DEFAULT GETDATE(),

    CONSTRAINT FK_Lookbook_Account FOREIGN KEY (account_id) REFERENCES Accounts(id),
    CONSTRAINT FK_Lookbook_Product FOREIGN KEY (product_id) REFERENCES Products(id),
    CONSTRAINT FK_Lookbook_Order   FOREIGN KEY (order_id)   REFERENCES Orders(id)
);
GO

-- 2. Tạo các chỉ mục (indexes) để tăng tốc độ truy vấn
CREATE INDEX idx_invlog_timestamp ON InventoryLogs (timestamp);
CREATE INDEX idx_invlog_variant ON InventoryLogs (variant_id);
GO



-- 3. VIEWS & SP

CREATE VIEW v_DailyRevenue AS
SELECT 
    CAST(order_date AS DATE) as [Date],
    COUNT(id) as TotalOrders,
    SUM(final_amount) as Revenue
FROM Orders
WHERE status = 'COMPLETED'
GROUP BY CAST(order_date AS DATE);
GO

CREATE PROCEDURE sp_GetBestSellers
    @Month INT,
    @Year INT
AS
BEGIN
    SELECT TOP 10
        p.name,
        p.image,
        SUM(od.quantity) as TotalSold,
        SUM(od.total) as TotalRevenue
    FROM OrderDetails od
    JOIN Orders o ON od.order_id = o.id
    JOIN ProductVariants pv ON od.product_variant_id = pv.id
    JOIN Products p ON pv.product_id = p.id
    WHERE o.status = 'COMPLETED' 
      AND MONTH(o.order_date) = @Month 
      AND YEAR(o.order_date) = @Year
    GROUP BY p.id, p.name, p.image
    ORDER BY TotalSold DESC;
END
GO

-- 4. NẠP DỮ LIỆU THẬT (QUAN TRỌNG)

-- A. ROLES & ACCOUNTS
INSERT INTO Roles (name) VALUES ('ADMIN'), ('USER');

INSERT INTO Accounts (username, password, full_name, email, phone, role_id) VALUES
('admin', '$2a$12$JD2.7/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s', N'Quản Trị Viên', 'admin@shopomg.com', '0909000111', 1),
('khachhang', '$2a$12$JD2.7/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s', N'Nguyễn Văn A', 'khach@gmail.com', '0909000222', 2),
('nguyenlam', '$2a$12$JD2.7/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s.v/lX.s', N'Nguyễn Lâm', '240107.lam@gmail.com', '0703017906', 2);

-- B. CATEGORIES (ẢNH THUMBNAIL CHẤT LƯỢNG CAO)
INSERT INTO Categories (name, slug, image, is_active) VALUES
(N'Áo Thun', 'ao-thun', N'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=500&q=60', 1), -- ID 1
(N'Áo Sơ Mi', 'ao-so-mi', N'https://images.unsplash.com/photo-1626497764746-6dc36546b388?auto=format&fit=crop&w=500&q=60', 1), -- ID 2
(N'Quần Jeans', 'quan-jeans', N'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?auto=format&fit=crop&w=500&q=60', 1), -- ID 3
(N'Váy Đầm', 'vay-dam', N'https://images.unsplash.com/photo-1612336307429-8a898d10e223?auto=format&fit=crop&w=500&q=60', 1), -- ID 4
(N'Giày Sneaker', 'giay-sneaker', N'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=500&q=60', 1), -- ID 5
(N'Giày Da', 'giay-da', N'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?auto=format&fit=crop&w=500&q=60', 1), -- ID 6
(N'Túi Xách', 'tui-xach', N'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=500&q=60', 1), -- ID 7
(N'Phụ Kiện', 'phu-kien', N'https://fashionscales.com/wp-content/uploads/2024/08/accessories-make-or-break-1100x7-1-1024x682.jpg', 1), -- ID 8
(N'Áo Khoác', 'ao-khoac', N'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=500&q=60', 1); -- ID 9


go

-- C. PRODUCTS & IMAGES (CHI TIẾT TỪNG SẢN PHẨM)

-- 1. ÁO THUN (Category 1) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Áo Polo Nam Thoáng Khí', 'ao-polo-nam', 320000, 5, N'Nam', 1, N'Cotton cá sấu', N'Việt Nam',
N'Áo mang đến vẻ đẹp nam tính và thanh lịch với thiết kế polo cổ điển. Kiểu dáng tinh tế kết hợp hàng cúc trước ngực giúp chiếc áo giữ được nét năng động nhưng vẫn vô cùng lịch sự.
Chất liệu mềm nhẹ cùng phom áo gọn gàng, mang lại cảm giác thoáng mát cả ngày dài. Dễ dàng kết hợp với quần kaki hay jeans để tạo nên set đồ tinh tế, nam tính.
Phom dáng: Dáng regular fit vừa vặn, tôn dáng.
Chi tiết: Cổ bẻ polo dệt kim, tay ngắn bo chun nhẹ năng động.
Chất liệu: Vải cá sấu cotton cao cấp, thấm hút mồ hôi cực tốt.
Màu sắc: Gam màu trung tính, dễ phối đồ.
Phù hợp: Đi làm, chơi thể thao, dạo phố hoặc gặp gỡ đối tác.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Thun Oversize Streetwear', 'ao-thun-oversize', 280000, 0, N'Unisex', 1, N'Cotton 2 chiều', N'Việt Nam',
N'Áo mang đến phong cách đường phố (streetwear) cá tính, đậm chất trẻ trung với form dáng rộng rãi. Sự lựa chọn hoàn hảo cho những bạn trẻ yêu thích sự tự do và phóng khoáng.
Chất liệu dày dặn nhưng vẫn đảm bảo độ thoáng mát, lên phom cực chuẩn. Gam màu hiện đại giúp chiếc áo trở thành lựa chọn linh hoạt cho những set đồ năng động.
Phom dáng: Dáng Oversize rộng rãi thoải mái, che khuyết điểm tốt.
Chi tiết: Cổ tròn basic, tay lỡ cá tính, đường may tỉ mỉ chắc chắn.
Chất liệu: Cotton 2 chiều dày dặn, không xù lông.
Màu sắc: Đen/Trắng basic streetstyle.
Phù hợp: Đi học, dạo phố, trượt ván hoặc cafe cùng bạn bè; dễ phối cùng quần jeans ống rộng, quần túi hộp.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.'),

(N'Áo Thun Ba Lỗ Thể Thao', 'ao-ba-lo-the-thao', 120000, 0, N'Nam', 1, N'Thun lạnh Spandex', N'Việt Nam',
N'Áo thiết kế sát nách mang lại sự linh hoạt tối đa, là trợ thủ đắc lực cho những buổi tập luyện cường độ cao hoặc mặc ở nhà trong những ngày hè oi bức.
Chất vải siêu nhẹ và nhanh khô, giúp cơ thể luôn mát mẻ và thoải mái khi vận động mạnh. Phom dáng khỏe khoắn tôn lên nét đẹp hình thể.
Phom dáng: Dáng suông, ôm nhẹ theo đường nét cơ thể.
Chi tiết: Khoét nách rộng vừa phải, cổ tròn viền chắc chắn.
Chất liệu: Thun lạnh thể thao spandex, siêu thấm hút và khô tản nhiệt nhanh.
Màu sắc: Các màu sắc năng động, mạnh mẽ.
Phù hợp: Tập gym, chạy bộ, thể thao ngoài trời hoặc mặc nhà.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.'),

(N'Áo Thun Dài Tay Thu Đông', 'ao-thun-dai-tay', 220000, 10, N'Unisex', 1, N'Cotton pha nỉ', N'Việt Nam',
N'Sự giao thoa hoàn hảo giữa thời trang và sự tiện dụng. Chiếc áo tay dài là "must-have item" mang đến vẻ đẹp thanh lịch trong những ngày se lạnh.
Chất liệu nỉ mỏng giữ ấm cơ thể nhưng không gây bí bách. Đồng thời dễ dàng layer cùng áo khoác ngoài hoặc mặc đơn để tạo nên những set đồ chuẩn style Hàn Quốc.
Phom dáng: Dáng suông nhẹ, vừa vặn tôn dáng.
Chi tiết: Cổ tròn, tay dài bo chun nhẹ ở gấu tay giúp giữ ấm hiệu quả.
Chất liệu: Cotton pha nỉ da cá mỏng nhẹ, co giãn thoải mái.
Màu sắc: Các gam màu pastel và trung tính nhẹ nhàng.
Phù hợp: Đi học, đi làm, dạo phố trong thời tiết thu đông; dễ phối cùng áo khoác, quần jeans.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.'),

(N'Áo Croptop Nữ Cá Tính', 'ao-croptop-nu', 180000, 0, N'Nữ', 1, N'Thun tăm co giãn', N'Việt Nam',
N'Item không thể thiếu cho những cô nàng theo đuổi phong cách quyến rũ, hiện đại. Chiếc áo croptop giúp tôn lên vòng eo thon gọn, khoe trọn nét cá tính đầy tự tin.
Chất liệu co giãn tốt ôm sát cơ thể một cách tinh tế. Áo trở nên cực "chất" và thu hút mọi ánh nhìn khi được phối cùng các trang phục cạp cao.
Phom dáng: Dáng ngắn croptop, ôm body nhẹ nhàng.
Chi tiết: Cổ tròn trẻ trung, tay cộc năng động.
Chất liệu: Thun tăm / Cotton spandex co giãn 4 chiều, mềm mại với làn da.
Màu sắc: Nổi bật và cuốn hút.
Phù hợp: Đi chơi, chụp ảnh, dạo phố; cực chuẩn khi mix cùng quần ống rộng cạp cao hoặc chân váy chữ A.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.'),

(N'Áo Thun Kẻ Sọc Ngang', 'ao-thun-ke-soc', 190000, 0, N'Unisex', 1, N'100% Cotton', N'Việt Nam',
N'Chiếc áo kẻ sọc kinh điển không bao giờ lỗi mốt, mang hơi hướng thời trang thanh lịch và hiện đại. Một item cơ bản nhưng luôn tạo hiệu ứng thị giác tuyệt vời.
Họa tiết kẻ sọc ngang giúp thân hình trông đầy đặn và cân đối hơn. Chất liệu êm ái nâng niu làn da, là sự lựa chọn an toàn nhưng luôn hiệu quả cho mọi outfit thường ngày.
Phom dáng: Dáng regular suông vừa vặn thoải mái.
Chi tiết: Họa tiết kẻ sọc đan xen sắc nét, tay ngắn, cổ tròn.
Chất liệu: Cotton mềm mại, thoáng mát, thấm hút tốt.
Màu sắc: Sọc đen trắng / xanh trắng cổ điển.
Phù hợp: Đi học, đi làm, du lịch hoặc dạo phố cuối tuần.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.'),

(N'Áo Thun Raglan', 'ao-thun-raglan', 210000, 5, N'Nam', 1, N'100% Cotton', N'Việt Nam',
N'Lấy cảm hứng từ thời trang bóng chày (baseball t-shirt), áo raglan mang đến vẻ ngoài khỏe khoắn, thể thao và đầy năng lượng cho người mặc.
Thiết kế phần tay áo khác màu đặc trưng tạo điểm nhấn nổi bật trên nền thân áo trơn. Cấu trúc ráp lăng thông minh giúp cử động cánh tay thoải mái tối đa.
Phom dáng: Suông thoải mái, đậm chất thể thao.
Chi tiết: Phần tay áo nối chéo từ cổ áo liền mạch, viền cổ nổi bật.
Chất liệu: 100% Cotton thoáng khí, thân thiện với làn da.
Màu sắc: Thân trắng kết hợp tay áo màu tương phản (đen/xanh/đỏ).
Phù hợp: Hoạt động ngoài trời, dã ngoại, dạo phố hoặc chơi thể thao nhẹ nhàng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình.');

-- 2. ÁO SƠ MI (Category 2) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Áo Sơ Mi Flannel Caro', 'so-mi-flannel', 350000, 0, N'Nam', 2, N'Dạ Flannel', N'Việt Nam',
N'Áo mang đến phong cách cổ điển, bụi bặm nhưng không kém phần trẻ trung. Thiết kế họa tiết caro vượt thời gian luôn là "chân ái" trong tủ đồ của những chàng trai yêu thích sự năng động.
Chất vải dạ mỏng vừa đủ để giữ ấm nhẹ nhàng, rất thích hợp khoác ngoài hoặc mặc đơn trong những ngày thời tiết se lạnh, tạo nên những layer phối đồ đầy phong cách.
Phom dáng: Dáng suông rộng rãi (Relaxed fit) thoải mái cử động.
Chi tiết: Cổ bẻ cổ điển, họa tiết caro sắc nét, có túi ngực tiện lợi, tay dài.
Chất liệu: Dạ flannel mỏng, bề mặt cọ xát mềm mịn, không gây dặm ngứa.
Màu sắc: Phối màu caro đan xen nổi bật.
Phù hợp: Đi dạo phố, đi học, cắm trại; dễ dàng khoác ngoài áo thun basic phối cùng quần jeans.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Trắng Công Sở', 'so-mi-trang-cong-so', 400000, 10, N'Nữ', 2, N'Kate lụa', N'Việt Nam',
N'Biểu tượng của sự thanh lịch và chuyên nghiệp chốn công sở. Chiếc áo sơ mi trắng tinh khôi với thiết kế tối giản, tinh tế giúp các quý cô luôn tự tin và tỏa sáng trong mọi cuộc họp.
Đường may lập thể ôm nhẹ lấy vóc dáng cùng chất liệu chống nhăn hiệu quả giúp trang phục luôn phẳng phiu, chỉn chu từ sáng đến chiều.
Phom dáng: Dáng ôm nhẹ tinh tế, tôn lên vóc dáng thanh mảnh.
Chi tiết: Cổ đức sắc sảo, hàng cúc giấu thanh lịch, tay dài cài măng sét.
Chất liệu: Kate lụa cao cấp, mềm mại, thoáng mát và chống nhăn tự nhiên.
Màu sắc: Trắng tinh khôi, dễ dàng mix & match.
Phù hợp: Đi làm, dự sự kiện, gặp gỡ đối tác; hoàn hảo khi mix cùng chân váy bút chì hoặc quần tây âu.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Denim Bụi Bặm', 'so-mi-denim', 480000, 0, N'Nam', 2, N'Denim wash mềm', N'Việt Nam',
N'Tuyên ngôn cho phong cách nam tính, mạnh mẽ và đầy tự do. Chiếc áo sơ mi denim mang lại diện mạo phong trần, đậm chất đường phố nhưng vẫn vô cùng cuốn hút.
Sử dụng công nghệ wash màu hiện đại tạo nên những vệt xước tự nhiên. Chất liệu vải bò đã qua xử lý làm mềm, đảm bảo sự bền bỉ mà vẫn êm ái khi tiếp xúc với da.
Phom dáng: Dáng regular fit vừa vặn, tôn dáng s-line nam tính.
Chi tiết: Cổ bẻ cứng cáp, nút bấm kim loại dập nổi chắc chắn, hai túi nắp ngực cá tính.
Chất liệu: Vải bò (denim) wash mềm, độ dày dặn vừa phải, đứng form.
Màu sắc: Xanh denim wash xước khỏe khoắn.
Phù hợp: Đi dạo phố, đi phượt, du lịch; có thể khoác ngoài áo thun trắng trơn mặc cùng quần kaki tối màu.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Cộc Tay Mùa Hè', 'so-mi-coc-tay', 250000, 5, N'Nam', 2, N'Lanh/Đũi pha Cotton', N'Việt Nam',
N'Khơi dậy không khí mùa hè sôi động với thiết kế họa tiết nhiệt đới rực rỡ. Chiếc áo mang lại cảm giác phóng khoáng, mát mẻ, xua tan ngay cái oi ả của những ngày nắng nóng.
Thiết kế cổ bẻ kiểu Cuba (Cuban collar) đầy sành điệu kết hợp cùng chất liệu siêu nhẹ và thoáng khí, mang đến sự dễ chịu tuyệt đối trong mọi chuyến đi.
Phom dáng: Dáng suông rộng (Loose fit) tạo sự thoải mái tối đa.
Chi tiết: Cổ áo Cuba sành điệu, tay cộc mát mẻ, họa tiết in chìm sắc nét.
Chất liệu: Vải lanh/đũi pha cotton, siêu nhẹ, thấm mồ hôi và bay hơi nhanh.
Màu sắc: Họa tiết hoa lá nhiệt đới sinh động.
Phù hợp: Đi biển, du lịch, dạo phố ngày hè; cực "cháy" khi phối cùng quần short kaki hoặc quần đũi.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Linen Form Rộng', 'so-mi-linen', 380000, 0, N'Nữ', 2, N'100% Linen tự nhiên', N'Việt Nam',
N'Vẻ đẹp mộc mạc, tự nhiên và bay bổng dành cho các cô nàng yêu thích phong cách thời trang tối giản (Minimalism). Chiếc áo mang lại cảm giác nhẹ tênh, tự do trong từng chuyển động.
Chất liệu linen 100% tự nhiên với đặc trưng là những nếp nhăn lộn xộn đầy nghệ thuật. Gam màu đất nhẹ nhàng dễ dàng chinh phục những tín đồ thời trang tinh tế nhất.
Phom dáng: Dáng Oversize rộng rãi, phóng khoáng, che giấu khuyết điểm vòng 2.
Chi tiết: Cổ bẻ tự nhiên, hàng cúc gỗ mộc mạc, tay dài có thể xắn gập năng động.
Chất liệu: Vải đũi (Linen) tự nhiên cao cấp, thoáng khí và thấm hút cực tốt.
Màu sắc: Gam màu đất (Earth tones) thanh nhã, mộc mạc.
Phù hợp: Dạo phố, cafe cuối tuần, đi biển; cực thơ khi phối cùng quần suông ống rộng hoặc chân váy midi.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Cổ Tàu', 'so-mi-co-tau', 320000, 0, N'Nam', 2, N'Cotton thô lụa', N'Việt Nam',
N'Sự kết hợp hoàn hảo giữa nét Á Đông truyền thống và hơi thở hiện đại. Thiết kế cổ trụ độc đáo mang lại vẻ ngoài lịch lãm, gọn gàng nhưng không quá gò bó như sơ mi cổ đức thông thường.
Item hoàn hảo cho những ngày bạn muốn thay đổi phong cách công sở thường nhật để trở nên lãng tử, nghệ sĩ và khác biệt hơn một chút.
Phom dáng: Regular fit tôn lên vóc dáng nam tính, không quá bó sát.
Chi tiết: Cổ trụ (cổ tàu) ôm vừa vặn, vạt áo bầu tinh tế, tay dài có măng sét.
Chất liệu: Cotton thô lụa, bề mặt nhẵn, chống nhăn tốt, đứng form áo.
Màu sắc: Trơn màu tối giản, sang trọng.
Phù hợp: Đi làm, dự tiệc nhẹ, hẹn hò; mix mượt mà cùng quần âu tây, quần chinos hoặc khoác ngoài áo vest mỏng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Voan Nơ Cổ', 'so-mi-voan-no', 290000, 0, N'Nữ', 2, N'Voan tơ lụa', N'Việt Nam',
N'Hiện thân của sự điệu đà, nữ tính và ngọt ngào. Điểm nhấn dây thắt nơ to bản ở cổ áo giúp tôn lên trọn vẹn nét duyên dáng, yêu kiều của phái đẹp trong mọi khoảnh khắc.
Chất liệu tơ lụa mềm rủ tạo độ bồng bềnh tự nhiên, giúp người mặc trông thật nhẹ nhàng, bay bổng. Một thiết kế sinh ra để biến bạn thành nàng thơ nơi công sở.
Phom dáng: Dáng suông mềm mại, rũ nhẹ theo đường cong cơ thể.
Chi tiết: Cổ phối dây thắt nơ điệu đà, tay dài xếp bồng nhẹ, bo chun ở cổ tay nữ tính.
Chất liệu: Vải voan tơ lụa mỏng nhẹ (có kèm áo lót 2 dây bên trong), mềm mượt mướt da.
Màu sắc: Tông màu pastel ngọt ngào, tươi sáng.
Phù hợp: Đi làm công sở, dự tiệc, hẹn hò lãng mạn; đẹp nhất khi phối cùng chân váy xòe, chân váy chữ A xếp ly.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Đen Slimfit', 'so-mi-den-slimfit', 420000, 0, N'Nam', 2, N'Lụa pha Spandex', N'Việt Nam',
N'Sự lựa chọn tối thượng cho những quý ông theo đuổi phong cách sang trọng và quyền lực. Một chiếc sơ mi đen tuyền luôn chứa đựng sự bí ẩn, thu hút mọi ánh nhìn tại bất kỳ sự kiện nào.
Sự kết hợp hoàn hảo giữa kỹ thuật cắt may lập thể ôm sát và chất liệu vải có độ co giãn tốt, giúp tôn lên hình thể nam tính mà vẫn đảm bảo sự linh hoạt tối đa khi di chuyển.
Phom dáng: Dáng Slimfit ôm sát cơ thể, khoe triệt để hình thể săn chắc.
Chi tiết: Cổ bẻ sắc nét, đường ly áo may tinh xảo, cúc áo tiệp màu áo tạo sự liền mạch.
Chất liệu: Lụa pha spandex cao cấp, bề mặt trơn nhẵn có độ bóng nhẹ, sang trọng.
Màu sắc: Đen tuyền huyền bí, nam tính.
Phù hợp: Tham dự tiệc tối, sự kiện quan trọng, đi bar club; kết hợp xuất sắc cùng quần âu xám/đen và giày Oxford da thật.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- 3. JEANS (Category 3) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Quần Jeans Rách Gối', 'jeans-rach-goi', 550000, 0, N'Nam', 3, N'Denim pha Spandex', N'Việt Nam',
N'Biểu tượng của sự tự do và phong cách đường phố (streetwear) đầy bụi bặm. Chiếc quần jeans rách gối mang đến vẻ ngoài cá tính, phá cách nhưng không hề phô trương.
Những đường rách xước được xử lý thủ công tỉ mỉ kết hợp với công nghệ wash màu hiện đại tạo nên hiệu ứng phai màu tự nhiên. Món đồ không thể thiếu của những chàng trai yêu sự phóng khoáng.
Phom dáng: Slim fit ôm nhẹ chân, tôn vóc dáng khỏe khoắn.
Chi tiết: Rách xước tự nhiên ở gối, 5 túi cổ điển, đinh tán kim loại.
Chất liệu: Vải denim pha spandex co giãn nhẹ, giữ form tốt dù vận động nhiều.
Màu sắc: Xanh nhạt wash xước.
Phù hợp: Dạo phố, đi phượt, tiệc tùng; cực ngầu khi phối cùng áo thun oversize và giày sneaker.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Skinny Jeans Nữ', 'skinny-jeans-nu', 450000, 10, N'Nữ', 3, N'Denim thun siêu co giãn', N'Việt Nam',
N'Tuyệt chiêu "hack dáng" hoàn hảo dành cho phái đẹp. Thiết kế ôm sát tuyệt đối từ eo đến cổ chân giúp khoe trọn đường cong quyến rũ và tạo hiệu ứng đôi chân thon dài miên man.
Sự đột phá trong chất liệu siêu co giãn mang lại cảm giác dễ chịu tối đa, ôm sát nhưng không hề gây cảm giác gò bó, bí bách ngay cả khi bạn mặc cả ngày dài.
Phom dáng: Skinny fit bó sát, cạp cao ôm trọn vòng eo.
Chi tiết: Đường may nhấn vòng 3 giúp tôn dáng (push-up), gấu quần cắt tinh tế.
Chất liệu: Denim thun cao cấp, co giãn 4 chiều cực mạnh, độ đàn hồi xuất sắc không bai nhão.
Màu sắc: Đen/Xanh đậm basic.
Phù hợp: Đi chơi, hẹn hò, đi làm (kết hợp sơ mi); dễ dàng phối với mọi loại giày từ sneaker đến cao gót.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Baggy Jeans', 'baggy-jeans', 420000, 5, N'Unisex', 3, N'Denim thô mềm', N'Việt Nam',
N'Hơi thở thời trang thập niên 90s trở lại mạnh mẽ. Quần Baggy Jeans mang thiết kế rộng rãi ở phần hông, đùi và thuôn dần về gấu quần, tạo nên nét năng động và cực kỳ che khuyết điểm.
Dù bạn có vòng đùi lớn hay bắp chân to, chiếc quần này sẽ "cân" hết. Đây là lựa chọn lý tưởng cho một ngày dài hoạt động mà vẫn muốn giữ sự sành điệu.
Phom dáng: Baggy thụng phần đùi, thon dần về gấu (Tapered fit).
Chi tiết: Cạp chun/khuy cài thoải mái, gấu quần có thể xắn lên năng động.
Chất liệu: Denim thô mềm, độ dày vừa phải, lên form chuẩn.
Màu sắc: Xanh nhạt / Xanh đậm retro.
Phù hợp: Đi học, dạo phố cuối tuần, nhảy hiphop; cực hợp khi đi cùng giày canvas đế bệt.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Short Jeans Nam', 'short-jeans-nam', 320000, 0, N'Nam', 3, N'Denim cotton thoáng khí', N'Việt Nam',
N'Món đồ giải nhiệt hoàn hảo cho mùa hè nhiệt đới. Thiết kế ngắn ngang gối giúp giải phóng đôi chân, mang lại sự mát mẻ và cực kỳ linh hoạt cho các hoạt động dã ngoại ngoài trời.
Cấu trúc cắt may khỏe khoắn kết hợp cùng chất liệu vải bền bỉ nhưng vẫn đủ độ thoáng khí. Đơn giản, nam tính và tiện dụng là những gì mô tả chính xác về chiếc short jeans này.
Phom dáng: Regular fit ống suông nhẹ, chiều dài chạm/ngang gối.
Chi tiết: Xước nhẹ gấu quần, túi mổ hai bên sâu rộng tiện lợi.
Chất liệu: Denim cotton thông thoáng, độ bền cao, không xù lông.
Màu sắc: Xanh biển / Xám khói.
Phù hợp: Du lịch, đi biển, dạo phố mùa hè; mặc cùng áo polo hoặc sơ mi cộc tay họa tiết.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Short Jeans Nữ', 'short-jeans-nu', 280000, 0, N'Nữ', 3, N'Denim mềm co giãn', N'Việt Nam',
N'Vũ khí khoe chân dài và vòng eo thon gọn không thể thiếu trong tủ đồ mùa hè của mọi cô gái. Thiết kế cạp cao (high-waist) thông minh giúp kéo dài tỷ lệ cơ thể một cách ngoạn mục.
Phần ống quần rộng vừa phải không gây gò bó phần đùi, kết hợp với các chi tiết rách tua rua mang đến nét trẻ trung, đáng yêu và đầy cuốn hút.
Phom dáng: Chữ A nhẹ, cạp cao qua rốn che bụng dưới cực tốt.
Chi tiết: Gấu xẻ tà nhẹ hoặc tua rua, cúc kim loại sáng bóng nổi bật.
Chất liệu: Denim mềm co giãn, không gây hằn lên da.
Màu sắc: Xanh sáng tươi trẻ / Trắng tinh khôi.
Phù hợp: Dạo phố, cafe, picnic; hoàn hảo khi đi chung với croptop, áo ống hoặc áo thun giấu quần.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Jeans Đen Trơn', 'jeans-den-tron', 500000, 0, N'Nam', 3, N'Denim than hoạt tính', N'Việt Nam',
N'Bản phối hoàn hảo giữa sự lịch lãm và tính đa dụng. Chiếc quần jeans đen trơn không tỳ vết mang lại vẻ ngoài gọn gàng, nam tính và chững chạc cho người mặc.
Ứng dụng công nghệ nhuộm giữ màu thế hệ mới (Color-Lock) giúp quần duy trì độ đen tuyền qua hàng chục lần giặt mà không lo phai bạc. Một món đồ bạn có thể mặc đi muôn nơi.
Phom dáng: Slim Straight (Ống đứng hơi ôm), thanh lịch tôn dáng.
Chi tiết: Trơn hoàn toàn không xước, đường chỉ may đồng màu tinh tế, túi sâu.
Chất liệu: Denim than hoạt tính, mềm mượt, bền màu và ít bám bụi.
Màu sắc: Đen tuyền sắc nét.
Phù hợp: Môi trường công sở thoải mái, đi học, hẹn hò; siêu dễ phối với áo sơ mi trắng, áo thun màu sắc.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Jeans Trắng', 'jeans-trang', 520000, 15, N'Nữ', 3, N'100% Cotton Denim', N'Việt Nam',
N'Sự xuất hiện của một chiếc jeans trắng luôn mang lại cảm giác bừng sáng, sang trọng và đầy sành điệu. Đây là item giúp nâng tầm mọi outfit thường ngày của bạn lên một đẳng cấp mới.
Quần được dệt từ chất liệu denim dày dặn với mật độ sợi cao, đảm bảo không hề bị lộ hay mỏng manh, mang đến sự tự tin tuyệt đối cho phái đẹp trong mọi hoàn cảnh.
Phom dáng: Ống suông thẳng (Straight leg), hack chân thẳng tắp.
Chi tiết: Cạp cao ôm eo, đường may ẩn sành điệu, không họa tiết rườm rà.
Chất liệu: 100% Cotton Denim dày dặn, đứng form và không thấu quang (không lộ nội y).
Màu sắc: Trắng kem thanh lịch.
Phù hợp: Sự kiện, tiệc trà, dạo phố; mix tuyệt đẹp cùng các tông màu pastel, màu be (beige) hoặc sơ mi voan.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Mom Jeans', 'mom-jeans', 490000, 0, N'Nữ', 3, N'100% Raw Denim', N'Việt Nam',
N'Sự hồi sinh mạnh mẽ của phong cách Retro kinh điển. "Quần của mẹ" (Mom Jeans) nổi bật với phần hông rộng, eo ôm cao sát sườn và ống quần thuôn gọn mang lại vẻ ngoài vintage không thể nhầm lẫn.
Cảm giác mộc mạc và chân thật đến từ chất vải denim nguyên bản không pha thun, giúp quần có độ đứng form hoàn hảo. Một item không thể thiếu với các nàng mê phong cách thời trang thập niên 80s, 90s.
Phom dáng: Mom fit cổ điển: rộng hông, ôm chặt cạp eo và thon dần xuống ống.
Chi tiết: Cạp cực cao (trên rốn), túi vuông lớn phía sau, tem mác da thật.
Chất liệu: 100% Raw Denim thô (không co giãn), bền bỉ, tạo nếp gấp đẹp tự nhiên.
Màu sắc: Xanh wash cổ điển (Vintage Blue).
Phù hợp: Phong cách Vintage, đi dạo, chụp hình film; cực xinh xắn khi sơ vin (đóng thùng) áo thun sọc ngang hoặc thắt thêm thắt lưng da bò.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- 4. VÁY ĐẦM (Category 4) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Váy Maxi Đi Biển', 'vay-maxi', 350000, 0, N'Nữ', 4, N'Voan Chiffon lụa', N'Việt Nam',
N'Mảnh ghép hoàn hảo cho những chuyến du lịch rực rỡ nắng vàng. Chiếc váy maxi mang đến vẻ đẹp thướt tha, bay bổng theo từng bước chân và giúp nàng hóa thân thành một "nàng thơ" thực thụ trên bãi biển.
Thiết kế tinh tế với phần eo chiết cao giúp kéo dài tỷ lệ cơ thể, kết hợp cùng họa tiết hoa nhiệt đới ngập tràn sức sống. Chất liệu mỏng nhẹ, cản gió tốt nhưng vẫn cực kỳ thoáng mát.
Phom dáng: Dáng maxi dài chạm mắt cá chân, xòe rộng bồng bềnh tự nhiên.
Chi tiết: Hai dây mảnh khoe trọn bờ vai thon, xẻ tà nhẹ hai bên đầy quyến rũ.
Chất liệu: Voan (Chiffon) lụa dập ly 2 lớp mềm mại, có lớp lót trong an toàn tuyệt đối.
Màu sắc: Họa tiết hoa lá rực rỡ (đỏ, vàng, xanh dương).
Phù hợp: Đi biển, du lịch sinh thái, chụp ảnh ngoại cảnh; mix thêm mũ cói và sandal đế xuồng là chuẩn bài.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Đầm Bodycon Ôm Sát', 'dam-bodycon', 400000, 0, N'Nữ', 4, N'Thun gân cao cấp', N'Việt Nam',
N'Tuyệt tác tôn vinh trọn vẹn những đường cong thanh xuân. Đầm bodycon là sự lựa chọn không thể chối từ của những quý cô yêu thích vẻ đẹp gợi cảm, sang trọng và đầy quyền lực.
Cấu trúc cắt may tinh xảo ôm sát vào cơ thể nhưng không hề tạo cảm giác phô phang nhờ chất liệu cao cấp dặn dặn, giúp định hình và nâng đỡ vóc dáng cực kỳ hiệu quả.
Phom dáng: Dáng ôm sát (Bodycon) tôn triệt để số đo 3 vòng.
Chi tiết: Cổ chữ U sâu gợi cảm, độ dài trên gối quyến rũ, thiết kế tối giản không họa tiết.
Chất liệu: Thun gân (Ribbed knit) cao cấp, co giãn 4 chiều cực mạnh, không lộ hằn nội y.
Màu sắc: Đen tuyền / Đỏ đô bí ẩn.
Phù hợp: Dự tiệc tối, đi bar club, hẹn hò lãng mạn; lộng lẫy nhất khi đi cùng giày cao gót mũi nhọn.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Chân Váy Xếp Ly', 'chan-vay-xep-ly', 250000, 5, N'Nữ', 4, N'Voan cát lụa', N'Việt Nam',
N'Mang đậm phong cách thanh lịch và dịu dàng của những quý cô Hàn Quốc. Chân váy xếp ly midi là món đồ "đa năng" dễ mặc, che khuyết điểm phần hông và bắp chân cực đỉnh.
Những nếp gấp xếp ly thủ công được dập nhiệt công nghệ cao, đảm bảo không bao giờ mất nếp sau nhiều lần giặt. Chiếc váy chuyển động nhịp nhàng theo từng bước đi của bạn.
Phom dáng: Dáng xòe nhẹ, chiều dài qua gối (Midi).
Chi tiết: Cạp chun sau co giãn thoải mái, các đường dập ly đều đặn sắc nét.
Chất liệu: Vải voan cát lụa bay bổng, ít nhăn, đứng form xếp ly.
Màu sắc: Be (Beige) / Đen thanh lịch.
Phù hợp: Đi làm công sở, dạo phố, đi học; dễ dàng kết hợp cùng áo sơ mi voan, áo thun ôm hoặc áo blazer.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Đầm Công Sở Chữ A', 'dam-cong-so', 450000, 10, N'Nữ', 4, N'Tuytsi pha Spandex', N'Việt Nam',
N'Đại diện cho vẻ đẹp chuyên nghiệp, chỉn chu và trang nhã chốn văn phòng. Thiết kế dáng chữ A kinh điển giúp tôn lên vòng eo nhỏ và khéo léo giấu đi những khuyết điểm ở phần thân dưới.
Sự tinh giản trong thiết kế kết hợp cùng chất liệu vải may âu phục cao cấp giúp chiếc đầm luôn phẳng phiu, đứng form, giữ cho nàng vẻ ngoài hoàn hảo từ lúc đến văn phòng cho tới khi tan sở.
Phom dáng: Dáng chữ A (A-line) ôm nhẹ eo và xòe dần xuống dưới.
Chi tiết: Cổ V thanh lịch, tay lỡ che bắp tay to, kèm đai lưng thắt eo điệu đà.
Chất liệu: Vải tuytsi cao cấp pha spandex, mặt vải lỳ, chống nhăn tuyệt đối.
Màu sắc: Xanh navy / Hồng pastel chuyên nghiệp.
Phù hợp: Đi làm, họp hành, gặp gỡ đối tác; hoàn thiện outfit với một đôi giày gót vuông vừa phải.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Váy Hai Dây Lụa', 'vay-hai-day', 300000, 0, N'Nữ', 4, N'Lụa Satin cao cấp', N'Việt Nam',
N'Sự quyến rũ được thể hiện qua nét mềm mại, mướt mát khó cưỡng. Váy lụa hai dây (Slip dress) là bản tình ca ngọt ngào dành cho làn da, nâng niu vẻ đẹp mỏng manh của người phụ nữ.
Độ bóng tự nhiên của lụa kết hợp với phần cổ đổ hờ hững tạo nên một hiệu ứng thị giác cực kỳ sang trọng. Trông bạn sẽ thật "chill" và nổi bật trong những buổi tối hẹn hò lung linh.
Phom dáng: Suông nhẹ rũ theo cơ thể, chạm đến ngang bắp chân.
Chi tiết: Cổ đổ (cowl neck) hờ hững quyến rũ, dây áo mảnh có thể điều chỉnh độ dài.
Chất liệu: Lụa Satin trứ danh, bề mặt bóng bẩy, trơn trượt và mát lạnh.
Màu sắc: Trắng ngọc trai / Đỏ rượu vang (Wine).
Phù hợp: Tiệc sinh nhật, dạo biển đêm, hẹn hò; có thể mặc layer khoác thêm cardigan mỏng hoặc blazer.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Chân Váy Jean Ngắn', 'chan-vay-jean', 220000, 0, N'Nữ', 4, N'Denim cotton dày dặn', N'Việt Nam',
N'Vũ khí bí mật mang lại nguồn năng lượng tươi trẻ, tinh nghịch cho ngày mới. Chân váy jean ngắn là một item "quốc dân" siêu dễ phối đồ mà mọi cô gái đều cần có trong tủ.
Thiết kế cạp cao (High-waisted) hack chân siêu đỉnh, kết hợp quần bảo hộ khéo léo bên trong giúp nàng thỏa sức vận động, chạy nhảy cả ngày mà không lo tình huống hớ hênh.
Phom dáng: Dáng chữ A vát xéo, chiều dài trên nửa đùi hack dáng.
Chi tiết: Rách xước nhẹ phần gấu xẻ tà, có sẵn quần lót bảo hộ bằng cotton bên trong.
Chất liệu: Denim dày dặn, đứng form không bị móp hay bai nhão.
Màu sắc: Xanh denim nhạt wash xước.
Phù hợp: Dạo phố, cafe, xem phim, picnic; mix đa phong cách cùng áo thun rách, croptop hoặc áo trễ vai.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Đầm Yếm Jean', 'dam-yem', 380000, 0, N'Nữ', 4, N'Jean bò mềm mại', N'Việt Nam',
N'Món bảo bối quay ngược thời gian giúp nàng "ăn gian" tuổi tác cực kỳ hiệu quả. Đầm yếm jean mang hơi hướng thời trang của những cô nàng kẹo ngọt, tinh nghịch và đầy sức sống.
Đặc biệt rất linh hoạt trong cách phối đồ. Bạn có thể thay đổi liên tục phong cách mỗi ngày chỉ bằng cách mix với những chiếc áo mặc trong khác nhau.
Phom dáng: Dáng suông rộng (Oversize) che hoàn toàn bụng dưới, thoải mái.
Chi tiết: Dây yếm có khóa gài kim loại điều chỉnh độ dài, túi ngực vuông vắn to bản siêu cute.
Chất liệu: Vải jean bò mềm mại, đã qua xử lý làm mềm, không gây cứng cộm.
Màu sắc: Xanh cobalt đậm cá tính.
Phù hợp: Đi học, dã ngoại, công viên; siêu hợp khi phối cùng áo thun trắng, áo sọc ngang tay lỡ và giày sneaker.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Váy Vintage Cổ Điển', 'vay-vintage', 420000, 0, N'Nữ', 4, N'Voan đũi tự nhiên', N'Việt Nam',
N'Tái hiện lại vẻ đẹp đài các, mơ màng của thời trang thập niên 60. Váy vintage với những đường nét cổ điển là sự lựa chọn dành riêng cho những "nàng thơ" yêu thích sự nhẹ nhàng, sâu lắng.
Các chi tiết cầu kỳ như tay bồng, cổ vuông pha ren hòa quyện cùng họa tiết in hoa nhí mang đến một tổng thể ngọt ngào, hoài cổ nhưng vẫn bắt kịp xu hướng hiện đại.
Phom dáng: Dáng xòe nhẹ tự nhiên qua gối, chiết eo cao.
Chi tiết: Cổ vuông khoe xương quai xanh xương gầy, tay bồng xếp ly (puff sleeves) che bắp tay.
Chất liệu: Voan đũi hoặc thô lụa, nhẹ tênh, thân thiện với làn da nhạy cảm.
Màu sắc: Nền kem họa tiết hoa nhí / chấm bi (Polka dots).
Phù hợp: Dạo phố mùa thu, viếng thăm bảo tàng, quán cafe vintage; kết hợp cùng giày búp bê Mary Jane và túi cói.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- =====================================================================================
-- 5. GIÀY SNEAKER (Category 5) 
-- =====================================================================================
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Converse Chuck Taylor All Star', 'converse-classic', 1500000, 0, N'Unisex', 5, N'Vải Canvas cao cấp', N'Việt Nam',
N'Huyền thoại của làng thời trang không bao giờ lỗi mốt. Đôi giày là biểu tượng văn hóa đại chúng, mang đến phong cách cá tính, tự do và cực kỳ dễ dàng "mix & match" với mọi loại trang phục trong tủ đồ của bạn.
Thiết kế cổ cao ôm sát cổ chân tạo điểm nhấn cực chất, kết hợp với phần mũi giày cao su đặc trưng giúp bảo vệ ngón chân. Một item mà bất kỳ ai cũng nên sở hữu ít nhất một đôi.
Phom dáng: Cổ cao (High-top) ôm trọn cổ chân, form dáng thon gọn.
Chi tiết: Logo ngôi sao All Star in sắc nét bên hông, mũi giày cao su trắng, lỗ viền kim loại thông thoáng.
Chất liệu: Vải Canvas cao cấp siêu bền bỉ, đế cao su lưu hóa (vulcanized) chống trơn trượt.
Màu sắc: Đen basic (Black/White).
Phù hợp: Đi học, dạo phố, đi làm; phối đẹp với mọi trang phục từ quần jeans, quần short đến các loại váy đầm.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Vans Old Skool', 'vans-old-skool', 1800000, 5, N'Unisex', 5, N'Da lộn phối vải Canvas', N'Việt Nam',
N'Đôi giày biểu tượng gắn liền với dân trượt ván (skater) và văn hóa đường phố. Thiết kế kinh điển với dải sọc Jazz stripe vắt ngang sườn giày mang đến diện mạo khỏe khoắn và đầy năng lượng.
Phần đế bằng bám đường cực tốt kết hợp cùng đệm lót êm ái ở cổ giày giúp bảo vệ mắt cá chân tối đa khi vận động mạnh. Sự kết hợp hoàn hảo giữa da lộn và vải canvas mang lại độ bền vượt trội.
Phom dáng: Cổ thấp (Low-top) năng động, thoải mái.
Chi tiết: Sọc Jazz sành điệu hai bên hông, viền cổ giày lót đệm êm, đường chỉ may kép chắc chắn.
Chất liệu: Da lộn (Suede) phối vải Canvas, mặt đế Waffle bằng cao su nguyên khối tăng độ bám dính.
Màu sắc: Đen viền trắng kinh điển.
Phù hợp: Trượt ván, nhảy hiphop, đi bộ dạo phố; đậm chất street style khi phối cùng quần ống rộng, quần túi hộp.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'New Balance 530', 'new-balance-530', 2800000, 0, N'Unisex', 5, N'Vải lưới Mesh tản nhiệt', N'Việt Nam',
N'Sự hồi sinh rực rỡ của xu hướng thời trang Y2K thập niên 2000. Đôi giày mang phong cách "Dad shoes" hoài cổ nhưng được tinh chỉnh lại đầy hiện đại, là hot-trend càn quét khắp các trang mạng xã hội.
Không chỉ sở hữu vẻ ngoài cực ngầu, New Balance 530 còn được trang bị công nghệ đế giữa hiện đại nhất giúp hấp thụ lực sốc, mang đến trải nghiệm đi lại êm ái như đang bước trên mây.
Phom dáng: Chunky (Dad shoes) hầm hố nhưng lên chân rất gọn, tôn dáng.
Chi tiết: Các mảng da cắt xẻ layer phức tạp, logo chữ N ánh kim nổi bật hai bên.
Chất liệu: Vải lưới (Mesh) siêu thoáng khí kết hợp da tổng hợp, đế giữa đệm ABZORB êm ái.
Màu sắc: Trắng phối ánh bạc (Silver) retro.
Phù hợp: Streetwear, dạo phố, chụp ảnh check-in; cực đỉnh khi mix cùng quần jogger, quần jeans thụng hoặc set đồ nỉ thể thao.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'MLB Chunky Liner', 'mlb-chunky', 3200000, 10, N'Unisex', 5, N'Da tổng hợp Microfiber', N'Việt Nam',
N'Bảo bối "hack dáng" tuyệt đỉnh dành cho các tín đồ đam mê thời trang sành điệu. Đôi giày sở hữu phần đế độn siêu cao, giúp bạn ăn gian thêm ít nhất 5-6cm chiều cao một cách vô cùng tự nhiên và tinh tế.
Họa tiết logo của các đội bóng chày nổi tiếng được in phá cách chìm nổi trên thân giày, biến mỗi bước đi của bạn trở thành một lời khẳng định về gu thời trang đẳng cấp và hiện đại.
Phom dáng: Chunky hầm hố, đế cao, viền lượn sóng sành điệu.
Chi tiết: Logo bóng chày (NY/LA/Boston) in tràn viền sống động, đường line dọc thân giày sắc nét.
Chất liệu: Da tổng hợp (Microfiber) cao cấp dễ vệ sinh lau chùi, đế ngoài bằng cao su lưu hóa siêu nhẹ.
Màu sắc: Trắng viền đen/xanh thanh lịch.
Phù hợp: Chụp ảnh OOTD, đi sự kiện, cafe dạo phố; mặc cùng chân váy xếp ly, quần short hay đồ thể thao đều xuất sắc.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Lười Slip-on', 'giay-slip-on', 500000, 0, N'Nam', 5, N'Vải dệt kim (Knit)', N'Việt Nam',
N'Giải pháp thời trang tối ưu cho những chàng trai yêu thích sự nhanh gọn, tiện lợi mà vẫn giữ được sự lịch sự, phong cách. Chỉ với một thao tác xỏ chân, bạn đã sẵn sàng bắt đầu một ngày mới đầy năng suất.
Lược bỏ hoàn toàn phần dây buộc rườm rà, thay vào đó là cấu trúc chun co giãn thông minh ôm khít mu bàn chân, mang lại cảm giác nhẹ nhàng, linh hoạt trong từng chuyển động.
Phom dáng: Dáng lười xỏ ngón ôm chân, mũi bo tròn gọn gàng.
Chi tiết: Không sử dụng dây buộc (Slip-on), chun co giãn linh hoạt ở sườn giày.
Chất liệu: Vải dệt kim (Knit) hoặc Canvas thông thoáng, lót trong êm ái, đế cao su dẻo uốn cong theo bàn chân.
Màu sắc: Đen / Xanh Navy nhã nhặn.
Phù hợp: Môi trường công sở thoải mái, đi học, lái xe ô tô; hợp nhất với quần kaki xắn gấu, quần short nam.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Chạy Bộ Running', 'giay-chay-bo', 900000, 20, N'Nam', 5, N'Lưới Mesh dệt 3D', N'Việt Nam',
N'Người bạn đồng hành không thể thiếu cho các runner và những người yêu thích tập luyện cường độ cao. Thiết kế khí động học cùng trọng lượng siêu nhẹ giúp tối ưu hóa từng bước chạy của bạn.
Hệ thống đệm hấp thụ xung lực tiên tiến giúp giảm thiểu tối đa áp lực lên đầu gối và gót chân, bảo vệ xương khớp hiệu quả. Lưới tản nhiệt giúp bàn chân luôn khô thoáng dù hoạt động liên tục.
Phom dáng: Ôm sát (Snug fit) hỗ trợ nâng đỡ vòm bàn chân cực tốt.
Chi tiết: Gót giày vát ngược hiện đại, cổ áo ôm sát cổ chân, dây giày dệt tròn không tuột.
Chất liệu: Thân trên là lưới Mesh dệt 3D tản nhiệt siêu tốc, đế giữa Foam bọt biển siêu nhẹ và đàn hồi.
Màu sắc: Đen xám cá tính có phản quang.
Phù hợp: Chạy bộ, tập gym, đạp xe, leo núi trekking nhẹ.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Sneaker High-top', 'sneaker-high-top', 1200000, 0, N'Nam', 5, N'Da tổng hợp phối vải dù', N'Việt Nam',
N'Điểm nhấn nổi loạn và cực kỳ cá tính cho bộ trang phục thường ngày. Đôi sneaker cổ cao mang hơi hướng thời trang của những chiến binh đường phố, mạnh mẽ, góc cạnh và khác biệt.
Phần cổ giày thiết kế cao lên tận mắt cá chân không chỉ mang lại vẻ ngoài "cool ngầu" mà còn có tác dụng giữ ấm và bảo vệ cổ chân an toàn trong những chuyến đi phượt, dã ngoại.
Phom dáng: Cổ cao qua mắt cá, form giày cứng cáp, mạnh mẽ.
Chi tiết: Dây thắt đan chéo trải dài, có khóa kéo phụ (zipper) bên hông hở tiện lợi khi tháo xỏ.
Chất liệu: Da tổng hợp kết hợp chi tiết vải dù, chống bám bụi và chống nước nhẹ.
Màu sắc: Đen nhám bí ẩn.
Phù hợp: Đi phượt, trình diễn đường phố, phong cách Techwear / Darkwear; cực "chất" khi diện chung với quần Cargo (túi hộp).
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Thể Thao Nữ Hồng', 'sneaker-nu-hong', 850000, 0, N'Nữ', 5, N'Da PU mờ phối vải lưới', N'Việt Nam',
N'Mang đến làn gió kẹo ngọt đầy nữ tính cho những cô nàng yêu thích sự vận động nhưng vẫn muốn giữ nét đáng yêu, dịu dàng. Màu hồng pastel sẽ làm bừng sáng cả ngày dài của bạn.
Form giày được nghiên cứu thiết kế riêng cho cấu trúc bàn chân phái đẹp, vô cùng thanh mảnh và nhẹ gọn. Đệm lót bên trong siêu êm ái, chống phồng rộp hoàn toàn dù bạn phải đi bộ cả ngày.
Phom dáng: Gọn gàng, thanh thoát, mũi giày thon mềm mại.
Chi tiết: Gam màu pastel mướt mắt, dây giày đồng màu, lót gót giày bằng đệm xốp siêu mềm.
Chất liệu: Da PU mờ nhám chống trầy kết hợp vải lưới thoáng khí, đế EVA chống trượt.
Màu sắc: Hồng phấn (Pink Pastel) dễ thương.
Phù hợp: Tập yoga, đi dạo, đi học sinh viên, du lịch; mix siêu xinh cùng chân váy tennis, váy suông nhẹ nhàng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- =====================================================================================
-- 6. GIÀY DA (Category 6) 
-- =====================================================================================
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Giày Chelsea Boot Nam', 'chelsea-boot', 1500000, 0, N'Nam', 6, N'Da bò thật nguyên tấm (Full-grain leather)', N'Việt Nam',
N'Biểu tượng của vẻ đẹp nam tính, phong trần nhưng vô cùng lịch lãm. Chelsea Boot là "must-have item" cho những quý ông hiện đại yêu thích sự linh hoạt, dễ dàng chuyển đổi từ phong cách công sở sang dạo phố.
Thiết kế cổ lửng đặc trưng với phần thun co giãn hai bên hông giúp thao tác xỏ chân trở nên cực kỳ tiện lợi và nhanh chóng. Phom giày ôm vừa vặn, tôn lên sự gọn gàng cho tổng thể trang phục.
Phom dáng: Cổ lửng qua mắt cá chân (Ankle boot), mũi giày thon gọn.
Chi tiết: Chun co giãn hai bên hông dẻo dai, có tab kéo (pull-tab) phía sau gót tiện lợi.
Chất liệu: Da bò thật nguyên tấm (Full-grain leather), mặt da trơn nhẵn, đế cao su đúc nguyên khối.
Màu sắc: Đen bóng / Nâu sẫm (Dark Brown).
Phù hợp: Đi làm, đi tiệc, hẹn hò; mix mượt mà cùng quần jeans ôm, quần kaki hoặc cả suit lịch lãm.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Loafer Penny', 'loafer-penny', 1300000, 5, N'Nam', 6, N'Da bò phủ bóng (Polished leather)', N'Việt Nam',
N'Tuyệt tác của sự thanh lịch và tinh tế không cần đến dây buộc rườm rà. Loafer Penny mang đậm hơi thở của giới quý tộc Châu Âu, là minh chứng cho gu thẩm mỹ đĩnh đạc của người đàn ông trưởng thành.
Chi tiết đai đục lỗ hình đồng xu (Penny) vắt ngang thân giày tạo điểm nhấn cổ điển vượt thời gian. Bề mặt da bóng bẩy tự nhiên giúp đôi giày luôn nổi bật và thu hút ánh nhìn.
Phom dáng: Giày lười mũi tròn hơi vát, ôm khít mu bàn chân.
Chi tiết: Đai da cắt rãnh kinh điển vắt ngang mu bàn chân, đường chỉ may viền nổi thủ công tỉ mỉ.
Chất liệu: Da bò phủ bóng (Polished leather), lớp lót trong bằng da lợn thật siêu thấm hút mồ hôi.
Màu sắc: Đen tuyền / Nâu đỏ (Burgundy).
Phù hợp: Môi trường văn phòng, sự kiện sang trọng, tiệc cưới; hoàn hảo cùng quần âu cắt gấu (không mang tất dài).
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Derby Da Lộn', 'derby-da-lon', 1100000, 0, N'Nam', 6, N'Da lộn (Suede leather) cao cấp', N'Việt Nam',
N'Bản hòa ca giữa sự mộc mạc, bụi bặm và nét thanh lịch đương đại. Derby da lộn mang đến một luồng gió mới cho tủ giày của phái mạnh, bớt đi sự trang trọng cứng nhắc để thay bằng nét tự do, phóng khoáng.
Hệ thống viền buộc dây mở (Open lacing) giúp điều chỉnh độ rộng linh hoạt, cực kỳ thoải mái cho những người có mu bàn chân dày. Bề mặt da lộn mềm mịn tạo hiệu ứng thị giác ấm áp và sang trọng.
Phom dáng: Dáng Derby buộc dây linh hoạt, mũi tròn vừa.
Chi tiết: Dây buộc tròn bằng cotton phủ sáp, cấu trúc viền dây mở dễ xỏ chân.
Chất liệu: Da lộn (Suede leather) cao cấp cọ xát mềm mịn, đế cao su ép gỗ chắc chắn.
Màu sắc: Vàng bò / Nâu đất sành điệu.
Phù hợp: Môi trường công sở cởi mở, dạo phố, hẹn hò; đẹp nhất khi phối cùng quần Chinos, quần Kaki màu be hoặc xanh navy.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Monk Strap', 'monk-strap', 1600000, 0, N'Nam', 6, N'Da bò trơn cao cấp', N'Việt Nam',
N'Đỉnh cao của sự sang trọng, độc đáo và đậm chất quý ông Ý (Sprezzatura). Giày Monk Strap thu hút mọi ánh nhìn bởi thiết kế khóa cài kim loại vắt ngang thay cho dây buộc truyền thống.
Phiên bản Double Monk Strap (Khóa gài kép) mang đến vẻ đẹp sắc sảo, nam tính và đầy uy quyền. Đây là đôi giày giúp bạn nâng tầm đẳng cấp ngay lập tức tại những sự kiện đòi hỏi sự chỉn chu tuyệt đối.
Phom dáng: Giày tây trang trọng, mũi giày vát thon thanh lịch.
Chi tiết: Hai đai khóa gài bằng đồng thau chống gỉ (Double Monk), đường viền da được gọt giũa sắc nét.
Chất liệu: Da bò trơn cao cấp, lót da cừu thật bên trong êm ái, cấu trúc đế may chắc chắn.
Màu sắc: Đen sang trọng / Nâu cánh gián.
Phù hợp: Dự tiệc, gặp gặp đối tác lớn, chú rể; cực phẩm khi đi kèm cùng những bộ suit may đo chuẩn mực.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Mọi Lái Xe', 'driving-shoes', 950000, 10, N'Nam', 6, N'Da bò hạt hoặc da lộn siêu mềm', N'Việt Nam',
N'Tuyên ngôn của phong cách sống hưởng thụ và thoải mái. Giày lái xe (Driving shoes) ban đầu được thiết kế chuyên biệt để mang lại cảm giác chân thật nhất khi đạp chân ga, nay đã trở thành biểu tượng của thời trang dạo phố cuối tuần.
Thiết kế không đế cứng mà được thay thế bằng những đệm cao su nhỏ (Pebble) trải dài từ gót đến mũi giày, mang lại sự linh hoạt uốn cong theo từng nhịp bước và độ bám hoàn hảo.
Phom dáng: Dáng giày mọi bệt (Moccasin), ôm sát và nhẹ nhàng như một đôi tất.
Chi tiết: Đường may chun mũi thủ công, đế và gót là các núm cao su nổi chống trượt chuyên dụng.
Chất liệu: Da bò hạt hoặc da lộn siêu mềm mại, không có lót cốt cứng.
Màu sắc: Xanh Navy / Nâu đậm / Đen.
Phù hợp: Lái xe ô tô, đi du lịch nghỉ dưỡng, cafe cuối tuần; kết hợp tuyệt vời với quần linen, quần short dạo biển.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Boot Da Nữ', 'boot-da-nu', 1200000, 0, N'Nữ', 6, N'Da PU cao cấp / Da cừu mềm mại', N'Việt Nam',
N'Vũ khí sắc bén của những cô nàng thành thị sành điệu và cá tính. Đôi giày boot da lửng mang lại diện mạo đầy quyền lực, "chanh sả" và khả năng "hack" chiều cao ngoạn mục.
Sự kết hợp giữa chất liệu da bóng mượt và phần gót nhọn tinh tế giúp đôi chân trông thon thả và miên man hơn. Dễ dàng biến hóa từ nàng thơ kiêu kỳ đến quý cô cá tính nổi loạn.
Phom dáng: Boot cổ ngắn (Ankle boots), ôm gọn cổ chân.
Chi tiết: Mũi nhọn kiêu kỳ, gót nhọn cao 7cm tôn dáng, khóa kéo zip phía sau gót giấu kín tinh tế.
Chất liệu: Da PU cao cấp / Da cừu mềm mại, lót nỉ nhung mỏng bên trong giữ ấm.
Màu sắc: Đen bóng / Be (Beige) sành điệu.
Phù hợp: Tiệc tùng, chụp ảnh thời trang, dạo phố mùa lạnh; phối cực "cháy" cùng chân váy xếp ly, quần skinny da hoặc áo khoác măng tô.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Cao Gót Da Thật', 'cao-got-da', 1400000, 0, N'Nữ', 6, N'Da bò thật 100% mềm mại', N'Việt Nam',
N'Biểu tượng tối cao của sự nữ tính và quyền lực phái đẹp. Đôi giày cao gót thiết kế cổ điển (Classic Pump) là người bạn tri kỷ không thể thiếu trong những dịp quan trọng nhất của mọi cô gái.
Với độ dốc được nghiên cứu kỹ lưỡng cùng đệm lót siêu êm ái, đôi giày nâng đỡ trọn vẹn vòm chân, xua tan nỗi ám ảnh đau nhức mỏi ngay cả khi bạn phải đứng và di chuyển cả ngày dài.
Phom dáng: Dáng Pump truyền thống, mũi nhọn thanh thoát khoe trọn mu bàn chân.
Chi tiết: Gót mảnh gót nhọn (Stiletto) cao 5-7cm, không hở mũi.
Chất liệu: Da bò thật 100% cực kỳ mềm mại, càng đi càng ôm chân, lớp lót cao su non massage lòng bàn chân.
Màu sắc: Đen truyền thống / Trắng tinh khôi.
Phù hợp: Môi trường văn phòng cao cấp, tiệc tối, sự kiện quan trọng; là chân ái của mọi chiếc váy đầm công sở hay trang phục dạ hội.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Sandal Da Nam', 'sandal-da', 650000, 0, N'Nam', 6, N'Quai da bò sáp bụi bặm', N'Việt Nam',
N'Bản nâng cấp sang trọng cho những đôi dép đi mùa hè. Sandal da nam mang lại sự thoáng mát tối đa cho bàn chân nhưng vẫn giữ được vẻ lịch sự, chỉn chu cần thiết trong những buổi hẹn hò, cafe dạo phố.
Những dải da được cắt cúp sắc nét, ôm vừa vặn cấu trúc bàn chân. Phần đế siêu nhẹ có các rãnh chống trượt giúp mỗi bước đi trong những ngày hè oi ả trở nên nhẹ nhàng và vững chãi.
Phom dáng: Sandal xỏ ngón quai đan chéo mạnh mẽ, phom bè thoải mái.
Chi tiết: Quai hậu có móc cài điều chỉnh nấc, các đường rãnh lượn sóng chống trơn trên bề mặt đế.
Chất liệu: Quai da bò sáp bụi bặm, đế PU/Cao su đúc siêu nhẹ, chống nước nhẹ.
Màu sắc: Nâu bò sáp mộc mạc.
Phù hợp: Dạo phố ngày hè, du lịch biển, vi vu cuối tuần; kết hợp hoàn hảo cùng quần short, áo thun ba lỗ hoặc áo sơ mi đũi mát mẻ.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Brogue Đục Lỗ', 'brogue-shoes', 1350000, 0, N'Nam', 6, N'Da bò trơn cao cấp', N'Việt Nam',
N'Một tác phẩm nghệ thuật trên những mảnh da. Giày Brogue nổi bật với những họa tiết đục lỗ tinh xảo chạy dọc các mép da, mang đậm nét cổ điển, quý tộc và một chút sự kiêu kỳ của thời trang nước Anh.
Không chỉ đẹp mắt, những lỗ nhỏ đục trên bề mặt da trước đây từng là công năng thoát nước, nay đã trở thành chi tiết trang trí không thể thiếu để thể hiện sự cầu kỳ và gu thẩm mỹ tinh tế của người mang.
Phom dáng: Giày tây trang trọng, mũi nhọn bo tròn, có các lớp da xếp chồng (Wingtip).
Chi tiết: Họa tiết đục lỗ (Broguing) cầu kỳ hình chữ W ở mũi giày, dải ren lỗ chạy viền sắc nét.
Chất liệu: Da bò trơn cao cấp, lót trong bằng vải dệt kết hợp da thuộc nguyên miếng chống hôi chân.
Màu sắc: Nâu vàng (Tan) đánh patina thủ công loang màu nghệ thuật.
Phù hợp: Tiệc cưới, các buổi lễ trang trọng, phong cách vintage quý ông; hợp tuyệt đối với những bộ suit kẻ sọc, áo khoác Tweed cổ điển.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- =====================================================================================
-- 7. TÚI XÁCH (Category 7) 
-- =====================================================================================
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Balo Da Laptop', 'balo-da', 850000, 0, N'Nam', 7, N'Da PU cao cấp', N'Việt Nam',
N'Sự kết hợp hoàn hảo giữa tính công năng và phong cách hiện đại. Balo da laptop là người bạn đồng hành đáng tin cậy của dân văn phòng, mang đến vẻ ngoài chuyên nghiệp và sang trọng.
Không gian chứa đồ rộng rãi, thiết kế nhiều ngăn thông minh giúp bảo vệ thiết bị điện tử tối đa khỏi những va đập không mong muốn trong quá trình di chuyển.
Phom dáng: Form chữ nhật đứng cứng cáp, quai đeo vai bản rộng giảm áp lực cho lưng.
Chi tiết: Ngăn chống sốc chuyên dụng đựng vừa laptop 15.6 inch, khóa kéo kim loại trơn tru, đệm lưng lưới tổ ong thoáng khí.
Chất liệu: Da PU cao cấp chống thấm nước, lớp lót trong bằng nỉ mềm mịn chống xước máy.
Màu sắc: Đen nhám / Nâu sẫm thanh lịch.
Phù hợp: Đi làm công sở, công tác, đi học sinh viên; cực chuẩn khi mix cùng áo sơ mi, quần tây hoặc trang phục smart casual.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Túi Tote Vải Canvas', 'tui-tote', 150000, 0, N'Unisex', 7, N'Vải Canvas (vải bố)', N'Việt Nam',
N'Đại diện cho phong cách sống tối giản (Minimalism) và thân thiện với môi trường. Chiếc túi tote canvas mang đến diện mạo năng động, trẻ trung và cực kỳ tiện dụng cho nhịp sống hối hả.
Sức chứa "khủng" giúp bạn mang theo cả thế giới bên mình, từ sách vở, tài liệu đến đồ dùng cá nhân. Một item gọn nhẹ không thể thiếu của các bạn trẻ hiện đại.
Phom dáng: Dáng túi hình chữ nhật đứng / ngang rộng rãi, quai xách dài vừa vặn đeo vai.
Chi tiết: Ngăn chính lớn không nắp đậy (có cúc bấm miệng túi), có túi zip nhỏ bên trong đựng chìa khóa, điện thoại an toàn.
Chất liệu: Vải Canvas (vải bố) dày dặn, chịu lực đứt gãy cực tốt, dễ dàng giặt sạch khi bám bẩn.
Màu sắc: Trắng ngà (Beige) in họa tiết nghệ thuật typographic.
Phù hợp: Đi học tập, đi siêu thị mua sắm (hạn chế túi nilon), dạo phố cuối tuần; siêu dễ phối với trang phục thường ngày.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Ví Da Nam Cầm Tay', 'vi-da-cam-tay', 550000, 0, N'Nam', 7, N'Da bò nguyên miếng (Full-grain leather)', N'Việt Nam',
N'Phụ kiện khẳng định đẳng cấp và phong thái thành đạt của phái mạnh. Chiếc clutch (ví cầm tay) mang lại vẻ ngoài lịch lãm, gọn gàng, thay thế hoàn hảo cho những chiếc túi xách cồng kềnh.
Thiết kế thông minh với sức chứa tối ưu, giúp quý ông cất giữ gọn gàng điện thoại cỡ lớn, chìa khóa xe thông minh, thẻ ngân hàng và tiền mặt một cách an toàn và tinh tế.
Phom dáng: Dáng ví dài hình chữ nhật phẳng, kích thước cầm vừa vặn chắc tay.
Chi tiết: Khóa kéo zip bao quanh an toàn, dây da luồn cổ tay chống rơi rớt, nhiều khe cắm thẻ và ngăn kéo khóa chìm bên trong.
Chất liệu: Da bò nguyên miếng (Full-grain leather) dập vân sang trọng, mặt da lỳ chống xước dăm.
Màu sắc: Đen tuyền / Nâu sẫm quyền lực.
Phù hợp: Tham dự tiệc tối, sự kiện ký kết, dạo phố cuối tuần; kết hợp hoàn hảo cùng suit, vest hoặc áo sơ mi đóng thùng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Ví Ngắn Nữ Mini', 'vi-nu-mini', 250000, 0, N'Nữ', 7, N'Da PU lỳ mềm mịn cao cấp', N'Việt Nam',
N'Món phụ kiện nhỏ xinh không thể thiếu trong chiếc túi xách của mọi cô gái. Ví mini mang thiết kế ngọt ngào, tiện dụng, giúp bạn sắp xếp ngăn nắp những vật dụng tài chính cơ bản nhất.
Kích thước siêu nhỏ gọn nằm trọn trong lòng bàn tay, dễ dàng cất vừa vào túi áo khoác, túi quần hay những chiếc túi xách mini (micro bag) đang làm mưa làm gió trên thị trường thời trang.
Phom dáng: Thiết kế gấp ba (Trifold wallet) siêu nhỏ gọn, tiết kiệm diện tích tối đa.
Chi tiết: Cúc bấm kim loại dễ thao tác, ngăn chính đựng tiền trải thẳng, khe cắm thẻ khoa học, có ngăn khóa kéo nhỏ bên ngoài đựng tiền xu.
Chất liệu: Da PU lỳ mềm mịn cao cấp, đường may viền đai tỉ mỉ chống bong tróc keo.
Màu sắc: Tông màu pastel ngọt ngào (Hồng phấn, Xanh bơ, Trắng kem).
Phù hợp: Sử dụng hàng ngày, mang đi chợ, đi siêu thị; vô cùng tiện lợi cho những lúc chỉ cần mang theo giấy tờ tùy thân và ít tiền mặt.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Túi Messenger Đeo Chéo', 'tui-messenger', 450000, 10, N'Nam', 7, N'Vải Canvas pha da sáp ngựa điên', N'Việt Nam',
N'Lấy cảm hứng từ những chiếc túi đưa thư cổ điển, túi Messenger là biểu tượng của phong cách tự do, bụi bặm nhưng vẫn cực kỳ thực dụng cho nhịp sống hiện đại hối hả.
Thiết kế quai đeo chéo giúp giải phóng đôi tay để lái xe hoặc cầm điện thoại. Nắp gập bản lớn an toàn bảo vệ tốt tài liệu và các thiết bị điện tử cá nhân khỏi khói bụi hay những cơn mưa bất chợt.
Phom dáng: Form hình chữ nhật ngang, dáng túi mềm linh hoạt nới rộng không gian.
Chi tiết: Nắp gập to bản với khóa gài nam châm chìm tinh tế, quai đeo chéo to bản bọc vải dù có thể điều chỉnh độ dài.
Chất liệu: Vải Canvas pha da sáp ngựa điên (Crazy horse leather) bụi bặm, chống mài mòn cực tốt.
Màu sắc: Nâu rêu Vintage / Xám than (Charcoal).
Phù hợp: Đi học sinh viên, đi làm công sở cởi mở, chạy xe máy đường dài; mix cực ngầu cùng áo khoác denim và giày boot da.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Túi Du Lịch Cỡ Lớn', 'tui-du-lich', 600000, 0, N'Unisex', 7, N'Vải dù Oxford mật độ cao', N'Việt Nam',
N'Giải pháp chứa đồ tối ưu cho những chuyến công tác ngắn ngày hay những kỳ nghỉ cuối tuần cùng bạn bè. Chiếc túi trống mang diện mạo thời thượng và phong cách thể thao khỏe khoắn.
Không chỉ là một chiếc túi nhồi nhét đồ đạc, đây còn là phụ kiện thời trang nâng tầm phong cách sân bay (Airport fashion) của bạn nhờ thiết kế hiện đại và màu sắc cực kỳ bắt mắt.
Phom dáng: Dáng trống ngang (Duffle bag) siêu rộng rãi, thiết kế bo tròn hai đầu.
Chi tiết: Ngăn đựng giày riêng biệt lót bạc chống bẩn/mùi, khóa kéo đôi (double zip) mở mượt mà, quai xách tay bọc da đệm và đai gài vali kéo tiện dụng.
Chất liệu: Vải dù Oxford mật độ cao trượt nước hoàn toàn, siêu nhẹ và chịu tải trọng lớn.
Màu sắc: Đen nhám / Ghi xám (Grey) thể thao.
Phù hợp: Đi tập gym, thể thao, du lịch phượt 2-3 ngày, công tác ngắn hạn; phù hợp với mọi phong cách thời trang năng động.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Túi Đeo Hông Bao Tử', 'tui-deo-hong', 320000, 0, N'Unisex', 7, N'Vải dù gió / Da PU trơn', N'Việt Nam',
N'Sự hồi sinh mạnh mẽ của phong cách thời trang thập niên 90. Túi bao tử (Bumbag / Crossbody bag) đang càn quét đường phố nhờ sự tiện dụng thao tác và vẻ ngoài cá tính không thể trộn lẫn.
Thiết kế ôm sát cơ thể giúp bảo vệ an toàn tuyệt đối cho các vật dụng quan trọng như điện thoại, ví tiền khi di chuyển ở những nơi đông người, đồng thời tạo điểm nhấn thú vị cho bộ trang phục.
Phom dáng: Dáng bán nguyệt hoặc chữ nhật thuôn dài, vát mỏng áp sát vào ngực hoặc ngang hông.
Chi tiết: Dây đai to bản dệt chữ nổi bật, khóa nhựa bấm (Buckle) thao tác nhả nhanh 1s, ngăn chính rộng kèm ngăn zip phụ giấu kín mặt lưng chống móc túi.
Chất liệu: Vải dù gió chống nước nhẹ / Da PU trơn cao cấp dễ dàng lau chùi sình lầy.
Màu sắc: Đen cá tính / Hologram phản quang nổi bật ban đêm.
Phù hợp: Chạy bộ, đạp xe, dạo phố, đi lễ hội âm nhạc (EDM festival); cực sành điệu khi đeo chéo ngực sát nách.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Cặp Da Công Sở', 'cap-da-cong-so', 1200000, 5, N'Nam', 7, N'Da thật nguyên miếng (Da bò Saffiano)', N'Việt Nam',
N'Biểu tượng vĩnh cửu của sự chuyên nghiệp, đĩnh đạc và quyền lực trong giới doanh nhân. Chiếc cặp táp (Briefcase) là vật bất ly thân giúp quý ông nâng tầm giá trị bản thân trong những cuộc họp mang tính quyết định.
Thiết kế phân ngăn khoa học dành riêng cho laptop 14 inch, tài liệu hợp đồng A4 phẳng phiu và các vật dụng văn phòng. Các góc cạnh sắc nét tôn lên sự chỉn chu và tính nguyên tắc của người sở hữu.
Phom dáng: Hình hộp chữ nhật mỏng, góc cạnh vuông vức, cấu trúc lót cốt cứng cáp không móp méo.
Chi tiết: Khóa gập kim loại mạ tĩnh điện chống gỉ sét, tay cầm bọc da đệm tròn may thủ công, tặng kèm dây đeo vai bọc đệm tháo rời.
Chất liệu: Da thật nguyên miếng (Da bò Saffiano dập vân chéo) cao cấp, chống trầy xước và giữ form vĩnh viễn.
Màu sắc: Đen bóng / Nâu đỏ rượu vang đắt giá.
Phù hợp: Gặp gỡ đối tác lớn, ký kết hợp đồng, giám đốc, cấp quản lý; là mảnh ghép hoàn mỹ cùng những bộ suit may đo chuẩn mực.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Túi Satchel Cổ Điển', 'tui-satchel', 700000, 0, N'Nữ', 7, N'Da PU dập vân xước / Da bò sáp', N'Việt Nam',
N'Mang hơi thở của sự thanh lịch vượt thời gian từ xứ sở sương mù Anh Quốc. Túi Satchel chinh phục các quý cô bởi thiết kế mang đậm nét học viện (Preppy style) thanh nhã, duyên dáng và đầy trí thức.
Thiết kế dáng hộp vuông vức nhưng không hề thô cứng nhờ những đường may bo góc tinh tế. Kích thước lý tưởng: vừa đủ nhỏ nhắn để đeo đi chơi, vừa đủ thanh lịch để mang tới chốn văn phòng.
Phom dáng: Dáng hộp chữ nhật ngang (Satchel bag), phom cứng cáp giữ dáng hoàn hảo ngay cả khi không đựng đồ.
Chi tiết: Nắp gập phong thư cổ điển, đai da cài khóa kim loại giả (có chốt nam châm hít ẩn tiện lợi phía dưới), quai xách tay mảnh mai nữ tính.
Chất liệu: Da PU dập vân xước Crossbody / Da bò sáp xước giữ nguyên bề mặt mộc mạc, bền bỉ phai màu theo năm tháng.
Màu sắc: Nâu da bò (Camel) mộc mạc / Đỏ gạch Vintage.
Phù hợp: Môi trường công sở thanh lịch, giáo viên đi dạy học, dạo phố thu đông; đẹp dịu dàng khi phối cùng áo măng tô dạ, váy xòe vintage hoặc sơ mi lụa tơ tằm.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- =====================================================================================
-- 8. PHỤ KIỆN (Category 8)
-- =====================================================================================
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Thắt Lưng Da Bò', 'that-lung-da', 350000, 0, N'Nam', 8, N'Da bò thật 100% nguyên tấm', N'Việt Nam', 
N'Mảnh ghép cuối cùng để hoàn thiện diện mạo của một quý ông lịch lãm. Chiếc thắt lưng da không chỉ làm tốt nhiệm vụ giữ form quần mà còn là điểm nhấn tinh tế phân chia tỷ lệ cơ thể hoàn hảo.
Bề mặt da được xử lý tỉ mỉ, bóng bẩy tự nhiên kết hợp cùng mặt khóa kim loại chống gỉ mang đến vẻ đẹp sang trọng, nam tính và độ bền đi cùng năm tháng.
Thiết kế: Bản rộng 3.5cm chuẩn công sở, dễ dàng luồn qua mọi đai quần tây và quần jeans.
Chi tiết: Mặt khóa kim loại tự động / khóa kim sắc nét, dễ dàng điều chỉnh kích cỡ vòng eo.
Chất liệu: Da bò thật 100% nguyên tấm, càng dùng càng mềm và bóng, không lo bong tróc gãy gập.
Màu sắc: Đen / Nâu sẫm nam tính.
Phù hợp: Môi trường công sở, dự tiệc, mặc cùng áo sơ mi đóng thùng, suit hoặc quần âu tây.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Mũ Lưỡi Trai NY', 'mu-luoi-trai', 250000, 0, N'Unisex', 8, N'100% Cotton Kaki dày dặn', N'Việt Nam', 
N'Món phụ kiện "quốc dân" mang đậm tinh thần thể thao khỏe khoắn và thời trang đường phố (streetwear). Mũ lưỡi trai giúp bạn che chắn nắng gió hiệu quả đồng thời nâng tầm set đồ casual của bạn lên một nấc thang mới.
Logo thêu nổi 3D sắc nét trên nền vải cotton thoáng khí giúp bạn tự tin thể hiện cá tính và gu thời trang năng động, trẻ trung.
Phom dáng: Lưỡi trai uốn cong ôm trọn vòng đầu, form nón cứng cáp giữ dáng tốt.
Chi tiết: Logo NY thêu nổi 3D sống động, đai cài kim loại/khóa dán phía sau dễ dàng điều chỉnh theo vòng đầu.
Chất liệu: 100% Cotton Kaki dày dặn, thấm hút mồ hôi trán cực tốt, không phai màu.
Màu sắc: Đen / Trắng / Be dễ phối đồ.
Phù hợp: Tập luyện thể thao, du lịch, dạo phố, chống nắng hằng ngày.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Mũ Bucket Vành Tròn', 'mu-bucket', 180000, 0, N'Unisex', 8, N'Vải Kaki / Canvas mềm mại', N'Việt Nam', 
N'Sự trở lại mạnh mẽ của xu hướng thời trang Y2K thập niên 2000. Mũ Bucket (mũ tai bèo) mang đến vẻ ngoài pha chút tinh nghịch, đáng yêu nhưng cũng cực kỳ bụi bặm và sành điệu.
Thiết kế vành nón cụp xuống giúp che chắn nắng toàn diện cho gương mặt và vùng cổ. Đặc biệt dễ dàng gấp gọn nhét vào túi xách hoặc balo mà không sợ mất form.
Phom dáng: Chóp mũ phẳng hoặc tròn nhẹ, vành cụp che nắng hiệu quả.
Chi tiết: Đường chỉ may chần đồng đều trên vành nón tạo độ cứng cáp, kiểu dáng tối giản.
Chất liệu: Vải Kaki / Canvas mềm mại, siêu thoáng khí, dễ dàng giặt ủi và gấp gọn.
Màu sắc: Vàng mustard / Đen / Trắng sữa (Cream).
Phù hợp: Picnic, đi biển, chụp ảnh ngoại cảnh, phong cách đường phố; mix xinh xắn cùng áo thun oversize.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Kính Mát Thời Trang', 'kinh-mat', 450000, 10, N'Unisex', 8, N'Nhựa Acetate / Hợp kim chống gỉ', N'Việt Nam', 
N'Bảo bối thần kỳ giúp che giấu đôi mắt mệt mỏi và nâng cấp thần thái tức thì. Kính mát thời trang không chỉ bảo vệ "cửa sổ tâm hồn" khỏi tác hại của ánh nắng mà còn là mảnh ghép làm nên phong cách siêu sao của bạn.
Tròng kính tích hợp công nghệ phân cực (Polarized) giúp loại bỏ ánh sáng chói, mang lại tầm nhìn dịu mắt và rõ nét trong những ngày nắng gắt hoặc khi lái xe.
Thiết kế: Phom dáng hiện đại (Wayfarer, Aviator hoặc Mắt mèo), ôm gọn khuôn mặt.
Chi tiết: Bản lề hợp kim chắc chắn, ve mũi đúc liền khối hoặc đệm silicon êm ái chống hằn.
Chất liệu: Gọng nhựa Acetate siêu nhẹ / Hợp kim chống gỉ, tròng kính phủ lớp chống tia UV400 chuẩn quốc tế.
Màu sắc: Gọng đen tròng đen / Gọng kim loại tròng trà.
Phù hợp: Đi biển, lái xe, du lịch, dạo phố dưới nắng gắt.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Đồng Hồ Dây Da', 'dong-ho-da', 1500000, 20, N'Nam', 8, N'Vỏ thép 316L, Dây da bê thật', N'Nhật Bản', 
N'Tuyên ngôn về sự đúng giờ và phong thái trưởng thành của một người đàn ông. Đồng hồ dây da mang nét đẹp cổ điển, sang trọng, là điểm nhấn thu hút nơi cổ tay giúp quý ông khẳng định giá trị bản thân.
Sự kết hợp giữa bộ máy hoạt động bền bỉ, chính xác và mặt kính chống xước mang lại một cỗ máy thời gian hoàn hảo, đồng hành cùng bạn qua từng cột mốc thành công.
Thiết kế: Mặt tròn cổ điển, thiết kế tối giản thanh lịch (Dress watch).
Chi tiết: Kim chỉ giờ vuốt nhọn sắc sảo, cọc số vạch đơn giản, tích hợp ô hiển thị ngày lịch tiện ích.
Chất liệu: Mặt kính khoáng (Mineral Crystal) chống va đập, vỏ thép không gỉ 316L, dây da bê thật mềm mại, máy Quartz Nhật Bản bền bỉ.
Màu sắc: Viền vàng hồng (Rose gold) mặt trắng / Dây nâu sẫm.
Phù hợp: Tham dự sự kiện, họp hành, môi trường công sở; kết hợp hoàn hảo với áo sơ mi tay dài xắn gấu.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Set 3 Đôi Tất Cổ Cao', 'tat-co-cao', 99000, 0, N'Unisex', 8, N'80% Cotton tự nhiên', N'Việt Nam', 
N'Phụ kiện tuy nhỏ nhưng đóng vai trò quan trọng trong việc bảo vệ đôi chân và hoàn thiện phong cách Sneakerhead của bạn. Tất cổ cao mang lại vẻ đẹp năng động, đậm chất thể thao.
Chất liệu dệt kim cao cấp giúp duy trì sự khô thoáng cho lòng bàn chân suốt cả ngày dài, ngăn chặn vi khuẩn gây mùi và chống phồng rộp khi vận động mạnh.
Thiết kế: Chiều dài cổ tất qua mắt cá chân (Crew socks), ôm sát bảo vệ gót chân.
Chi tiết: Cổ tất bo chun dệt gân co giãn tốt không gây hằn ngứa, gót và mũi tất dệt đệm xốp êm ái.
Chất liệu: 80% Cotton tự nhiên, 15% Spandex co giãn, 5% sợi kháng khuẩn khử mùi.
Màu sắc: Set mix Trắng / Đen / Xám basic.
Phù hợp: Tập gym, chạy bộ, chơi bóng rổ, đi học; cực sành điệu khi mang chung với giày Sneaker Chunky hoặc ván trượt.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Khăn Choàng Cổ Len', 'khan-choang', 220000, 0, N'Nữ', 8, N'Sợi len lông cừu tổng hợp', N'Việt Nam', 
N'Bản giao hưởng ấm áp giữa mùa đông giá lạnh. Khăn choàng cổ len không chỉ làm nhiệm vụ giữ ấm tuyệt đối cho vùng cổ và ngực mà còn là một item thời trang tạo điểm nhấn đầy lãng mạn cho phái đẹp.
Những sợi len được dệt tinh xảo, xốp nhẹ và êm ái chạm vào làn da, mang lại cảm giác dễ chịu, che chở và không hề gây dặm ngứa tấy đỏ.
Thiết kế: Bản rộng, kích thước dài có thể quàng nhiều vòng hoặc khoác nhẹ quanh vai.
Chi tiết: Dệt họa tiết xương cá / trơn màu tinh tế, phần tua rua ở hai đầu vạt khăn điệu đà.
Chất liệu: Sợi len lông cừu tổng hợp siêu mềm, xốp nhẹ, giữ nhiệt đỉnh cao.
Màu sắc: Đỏ đô / Xám tro / Be ấm áp.
Phù hợp: Dạo phố mùa đông, đi làm ngày gió lạnh, du lịch Đà Lạt/Sapa; đẹp mơ màng khi phối với áo măng tô hoặc áo len cổ lọ.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Cà Vạt Lụa Cao Cấp', 'ca-vat', 150000, 0, N'Nam', 8, N'Lụa tơ tằm nhân tạo (Silk / Microfiber)', N'Việt Nam', 
N'Vũ khí tối thượng của sự lịch lãm chốn thương trường. Một chiếc cà vạt lụa thắt ngay ngắn dưới vòm cổ áo sơ mi chính là "con dấu" chứng nhận cho sự chuyên nghiệp, tôn trọng đối tác và quyền lực của phái mạnh.
Độ bóng tự nhiên rũ nhẹ của chất liệu lụa mang lại sự sang trọng không thể chối từ, giúp tổng thể bộ Suit của bạn trở nên sắc sảo và hoàn mỹ.
Thiết kế: Bản vừa (Kích thước 7-8cm) chuẩn mực, không quá to già dặn cũng không quá nhỏ trẻ con.
Chi tiết: Lớp lót định hình bên trong giúp giữ form cà vạt và nút thắt luôn đứng vát chữ V.
Chất liệu: Lụa tơ tằm nhân tạo (Silk / Microfiber), bề mặt bóng nhẹ, dệt vân chìm tinh xảo, ít nhăn.
Màu sắc: Xanh Navy trơn / Đỏ mận họa tiết kẻ chéo.
Phù hợp: Họp mặt quan trọng, gặp đối tác, đi làm ngân hàng, dự tiệc cưới; kết hợp chuẩn nhất với áo sơ mi trắng và Suit cùng tông màu.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Vòng Tay Bạc', 'vong-tay', 550000, 0, N'Nữ', 8, N'Bạc Ý 925 nguyên chất', N'Việt Nam', 
N'Món trang sức lấp lánh nâng niu cổ tay thanh mảnh của phái đẹp. Vòng tay bạc mang đến vẻ đẹp tinh khiết, nhẹ nhàng và nữ tính, thu hút mọi ánh nhìn theo từng cử động nhỏ nhất của bạn.
Được chế tác từ kim loại quý an toàn cho sức khỏe, đây không chỉ là phụ kiện làm đẹp mà còn là món quà ý nghĩa, một điểm nhấn lấp lánh giúp bộ trang phục trở nên đắt giá hơn.
Thiết kế: Dáng lắc tay thanh mảnh, nhẹ nhàng, tôn lên xương cổ tay.
Chi tiết: Mắt xích đan tinh xảo, có đính đá CZ (Cubic Zirconia) lấp lánh như kim cương nhân tạo, chốt khóa càng cua an toàn.
Chất liệu: Bạc Ý 925 nguyên chất, mạ vàng trắng (Rhodium) tăng độ sáng bóng và chống xỉn màu, an toàn tuyệt đối cho da nhạy cảm.
Màu sắc: Trắng bạc lấp lánh.
Phù hợp: Đi dự tiệc, hẹn hò, đi làm hàng ngày; tăng thêm phần quyến rũ khi mặc những chiếc đầm hở tay hay váy hai dây.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Nơ Cài Áo Vest', 'no-cai-ao', 120000, 0, N'Nam', 8, N'Lụa phi bóng / Nhung cao cấp', N'Việt Nam', 
N'Điểm nhấn cổ điển và cực kỳ bảnh bao dành cho những sự kiện mang tính nghi thức cao. Nơ cài áo mang lại diện mạo hào hoa, đậm chất quý tộc và phá cách hơn so với chiếc cà vạt truyền thống.
Một phụ kiện nhỏ gọn nhưng có sức mạnh biến người đàn ông trở thành tâm điểm của buổi tiệc sang trọng, toát lên sự tinh tế và gu thưởng thức nghệ thuật sâu sắc.
Thiết kế: Nơ cánh bướm gập sẵn (Pre-tied bow tie), phom dáng cân đối và vuông vắn.
Chi tiết: Có dải băng vòng quanh cổ áo và móc gài kim loại ẩn, dễ dàng điều chỉnh kích thước vừa vặn cổ mà không gây ngạt.
Chất liệu: Lụa phi bóng / Nhung cao cấp, mặt vải bắt sáng tốt dưới ánh đèn tiệc.
Màu sắc: Đen tuyền / Đỏ đô / Trắng.
Phù hợp: Dạ tiệc tối (Black-tie event), chú rể ngày cưới, Mc dẫn chương trình; phối đẹp nhất với sơ mi cổ cánh dơi (Wing collar) và bộ Tuxedo sành điệu.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.')

-- =====================================================================================
-- 9. ÁO KHOÁC (Category 9) 
-- =====================================================================================
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

(N'Áo Hoodie Basic', 'ao-hoodie', 380000, 0, N'Unisex', 9, N'Nỉ bông dệt chéo', N'Việt Nam',
N'Mang đến sự ấm áp và phong cách năng động cho những ngày trở gió. Áo hoodie basic là "must-have item" trong tủ đồ của giới trẻ nhờ sự tiện dụng và khả năng giữ ấm tuyệt vời.
Thiết kế có mũ trùm đầu rộng rãi cùng túi sưởi ấm tay phía trước. Chất liệu nỉ bông dày dặn nhưng siêu nhẹ, giúp cản gió và giữ nhiệt cực kỳ hiệu quả mà không gây nặng nề.
Phom dáng: Dáng suông rộng (Oversize) thoải mái, năng động.
Chi tiết: Mũ trùm đầu có dây rút bọc nhựa, túi chéo phía trước (túi kangaroo), bo chun tay và gấu áo chắc chắn.
Chất liệu: Nỉ bông dệt chéo, mặt trong cào bông mềm mịn, không xù lông.
Màu sắc: Xám tiêu / Đen / Be.
Phù hợp: Đi học, dạo phố, mặc nhà mùa đông; siêu dễ phối cùng quần jeans, quần nỉ thể thao.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sweater Trơn', 'ao-sweater', 350000, 5, N'Unisex', 9, N'100% Cotton da cá', N'Việt Nam',
N'Chiếc áo chui đầu kinh điển mang lại vẻ ngoài gọn gàng, thanh lịch nhưng vẫn vô cùng trẻ trung. Áo sweater trơn màu là giải pháp mặc đẹp nhanh chóng trong những ngày se lạnh.
Cấu trúc dệt da cá (French Terry) thông minh giúp áo có độ thở, không gây hầm bí khi mặc cả ngày. Thiết kế tối giản giúp dễ dàng layering (mặc chồng lớp) với áo sơ mi bên trong.
Phom dáng: Regular fit vừa vặn, không quá thùng thình, lên form chuẩn.
Chi tiết: Cổ tròn truyền thống, tay và gấu áo bo thun gân (ribbing) chống giãn.
Chất liệu: 100% Cotton da cá cao cấp, co giãn nhẹ 4 chiều, thấm hút mồ hôi cực tốt.
Màu sắc: Các gam màu trung tính và pastel nhẹ nhàng.
Phù hợp: Môi trường đi học, đi làm thoải mái; đẹp chuẩn "nam thần/tỷ tỷ" Hàn Quốc khi mặc ngoài sơ mi cổ đức.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Khoác Bomber', 'bomber-jacket', 550000, 0, N'Nam', 9, N'Vải dù gió (Polyester)', N'Việt Nam',
N'Lấy cảm hứng từ những chiếc áo của phi công chiến đấu thời chiến, áo khoác Bomber mang đến diện mạo cực kỳ mạnh mẽ, bụi bặm và nam tính cho người mặc.
Thiết kế cản gió xuất sắc với bo thun kín ở cổ, tay và gấu áo. Phom dáng phồng nhẹ đặc trưng giúp phần thân trên trông vạm vỡ, đầy đặn và khỏe khoắn hơn.
Phom dáng: Dáng lửng ôm vừa hông, phần tay áo phồng nhẹ cá tính.
Chi tiết: Cổ chui viền bo thun thấp không gây cộm, khóa kéo kim loại mượt mà, túi chéo hai bên có nút bấm, túi phụ gắn khóa zip ở bắp tay áo.
Chất liệu: Vải dù gió (Polyester) chống thấm nước nhẹ, lót dù trơn bên trong cản gió cực tốt.
Màu sắc: Xanh rêu (Army Green) / Đen huyền bí.
Phù hợp: Dạo phố đêm, đi phượt, chạy xe máy; cực cool ngầu khi mix cùng áo thun trắng trơn và quần cargo túi hộp.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Khoác Denim', 'denim-jacket', 600000, 0, N'Unisex', 9, N'Denim thô (Raw Denim)', N'Việt Nam',
N'Biểu tượng bất diệt của phong cách thời trang đường phố tự do và phóng khoáng. Một chiếc áo khoác denim (áo khoác bò) càng mặc lâu càng mang đậm dấu ấn cá nhân của người sở hữu.
Chất liệu denim dày dặn chịu được mọi điều kiện thời tiết và hoạt động mạnh. Các đường may rập chắc chắn kết hợp hiệu ứng wash màu tạo nên vẻ ngoài bụi bặm, "chất lừ" không thể nhầm lẫn.
Phom dáng: Oversize form rộng rãi mượn phong cách thập niên 90s.
Chi tiết: Khuy cài bằng đồng đúc dập nổi thương hiệu, hai túi ngực có nắp gập an toàn, đường may chỉ nổi tương phản cổ điển.
Chất liệu: Denim thô (Raw Denim) pha chút spandex co giãn nhẹ, bền bỉ chống mài mòn tuyệt đối.
Màu sắc: Xanh nhạt (Light Blue Wash) / Xanh đậm (Dark Indigo).
Phù hợp: Phối đồ layer 4 mùa, đi dạo, đi chơi xa; là "người bạn thân" của mọi chiếc áo thun basic.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Blazer Hàn Quốc', 'ao-blazer', 750000, 10, N'Nam', 9, N'Cotton pha linen hoặc lụa tuytsi', N'Việt Nam',
N'Sự lựa chọn tối ưu cho những ngày bạn cần sự chỉn chu, lịch sự nhưng không muốn bị cứng nhắc như một bộ suit hoàn chỉnh. Áo Blazer mang lại diện mạo đĩnh đạc, hiện đại mang hơi hướng thời trang Hàn Quốc.
Cấu trúc vai mềm (Soft shoulder) lược bỏ hoàn toàn lớp đệm dày cộm, giúp áo nhẹ nhàng ôm theo đường nét tự nhiên của cơ thể. Một chiếc áo đa năng có thể mặc đi làm và cả đi chơi.
Phom dáng: Dáng suông nhẹ (Relaxed fit), thanh thoát, không ôm sát cơ thể.
Chi tiết: Cổ vest ve vếch (Notch lapel) thanh lịch, 1 hoặc 2 khuy cài đơn tinh tế, túi ốp (patch pocket) trẻ trung ở hai bên.
Chất liệu: Cotton pha linen hoặc lụa tuytsi, vô cùng mỏng nhẹ, thoáng mát, chống nhăn tốt.
Màu sắc: Xám lông chuột / Ghi nhạt / Be (Beige) thanh nhã.
Phù hợp: Môi trường công sở hiện đại, hẹn hò, dự tiệc nhẹ; phối cùng áo thun trơn (T-shirt) hoặc áo polo đều đẹp xuất sắc.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Vest Công Sở', 'ao-vest', 1200000, 0, N'Nam', 9, N'Vải Wool (len tuyết)', N'Việt Nam',
N'Đại diện cho sự đẳng cấp, quyền lực và phong thái chuyên nghiệp của người đàn ông thành đạt. Áo Vest (Suit Jacket) được cắt may lập thể theo tỷ lệ vàng, tôn vinh tuyệt đối hình thể nam tính.
Khác với blazer thông thường, áo vest công sở được dựng phom cứng cáp với lớp lót đệm ngực và vai (canvas) sắc nét, giúp duy trì dáng vẻ thẳng thớm, phẳng phiu trong bất kỳ hoàn cảnh nào.
Phom dáng: Slim fit ôm gọn cơ thể, chiết eo nhẹ tôn lên bờ vai rộng.
Chi tiết: Cổ chữ V sâu ve nhọn (Peak lapel) quyền lực, cấu trúc độn vai cứng cáp, lót trong bằng lụa satin mượt mà giấu kín những đường may.
Chất liệu: Vải Wool (len tuyết) cao cấp pha sợi nhân tạo, chống nhăn, ít bám bụi, giữ nhiệt tốt.
Màu sắc: Đen tuyền / Xanh Navy (Xanh đen) lịch lãm.
Phù hợp: Hội nghị cấp cao, ký kết hợp đồng, MC sự kiện, chú rể; bắt buộc kết hợp cùng quần âu đồng bộ, cà vạt và sơ mi dress shirt.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Cardigan Len', 'ao-cardigan', 320000, 0, N'Nữ', 9, N'Len lông cừu nhân tạo', N'Việt Nam',
N'Nét điểm xuyết dịu dàng, thơ mộng cho tiết trời vào thu. Áo khoác len mỏng Cardigan mang đến cho phái đẹp vẻ ngoài nữ tính, ngọt ngào và vô cùng duyên dáng.
Thiết kế mở cài nút phía trước dễ dàng khoác vội để che chắn gió lùa hoặc cởi ra khi nhiệt độ thay đổi. Chất len mềm xốp tựa như một vòng tay êm ái, nâng niu làn da nhạy cảm.
Phom dáng: Dáng lửng ngang hông (Cropped) hack chiều cao hoặc dáng dài rũ điệu đà.
Chi tiết: Cổ tim khoét sâu tôn vòng một, hàng khuy cài bằng nhựa giả sừng hoặc ngọc trai nhân tạo, dệt họa tiết vặn thừng bắt mắt.
Chất liệu: Len lông cừu nhân tạo siêu mềm mịn, không gây dặm ngứa tấy đỏ, cách nhiệt tốt.
Màu sắc: Các tông màu ấm áp như Trắng kem, Đỏ đô, Nâu đất.
Phù hợp: Dạo phố mùa thu, khoác nhẹ chống lạnh trong môi trường điều hòa văn phòng; khoác ngoài váy hai dây lụa vô cùng yêu kiều.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Khoác Da Biker', 'ao-khoac-da', 1500000, 0, N'Nam', 9, N'Da PU cao cấp / Da cừu thật', N'Việt Nam',
N'Vũ khí thời trang bứt phá mọi giới hạn an toàn. Áo khoác da Biker mang trong mình DNA của sự nổi loạn, tốc độ và một phong thái cực "ngầu" không thể trộn lẫn dành cho đấng mày râu.
Thiết kế bất đối xứng với vô số khóa kéo zip kim loại tạo điểm nhấn góc cạnh. Chất liệu da bóng bẩy giúp bạn trở thành tâm điểm chú ý và thể hiện khí chất vương giả ở mọi nơi bạn xuất hiện.
Phom dáng: Dáng ôm sát (Slim fit), form ngắn ngang thắt lưng giúp kéo dài tỷ lệ đôi chân.
Chi tiết: Cổ áo ve to bẻ bạt sang hai bên sành điệu, khóa kéo chéo bất đối xứng, đai thắt lưng da ở gấu áo có khóa cài kim loại.
Chất liệu: Da PU cao cấp công nghệ mới chống nổ gãy / Da cừu thật nguyên miếng siêu mềm mại, chống gió chống nước tuyệt đối.
Màu sắc: Đen bóng bẩy sắc lạnh.
Phù hợp: Trình diễn, đi bar club, cưỡi xe phân khối lớn, street style cá tính; mix bạo dạn với quần skinny jeans rách gối, boot da và kính đen.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Măng Tô Dạ', 'ao-mang-to', 1800000, 15, N'Nữ', 9, N'Dạ lông cừu ép hai da', N'Việt Nam',
N'Tuyên ngôn thời trang sang trọng, đẳng cấp mỗi khi đông về. Áo măng tô dạ (Overcoat/Trench coat dạ) mang đến vẻ đẹp đài các, kiêu sa như bước ra từ những bộ phim truyền hình Hàn Quốc lãng mạn.
Thiết kế phom dài không chỉ giữ ấm cơ thể từ đầu đến chân mà còn tạo hiệu ứng quyền lực, sang chảnh. Đây là một khoản đầu tư vô giá cho tủ đồ, vượt qua mọi thách thức của thời gian và xu hướng.
Phom dáng: Dáng suông dài qua đầu gối, rộng rãi che khuyết điểm toàn thân cực tốt.
Chi tiết: Ve cổ áo to bản sành điệu, có đai thắt eo tạo tỷ lệ đồng hồ cát, hai hàng khuy (Double-breasted) cổ điển, xẻ tà nhẹ phía sau giúp dễ di chuyển.
Chất liệu: Dạ lông cừu ép hai da cao cấp, cực kỳ dày dặn, đứng form và cản lạnh đỉnh cao.
Màu sắc: Nâu tây (Camel) / Be (Beige) / Đen sang trọng.
Phù hợp: Đi làm mùa đông, du lịch các nước ôn đới có tuyết, dạ tiệc sang trọng; phối vô cùng ăn ý với áo len cổ lọ và boot đùi gót nhọn.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');

-- =====================================================================================
-- 10. THÊM ĐỂ ĐỦ 100 PRODUCTS (TỔNG HỢP)
-- =====================================================================================
INSERT INTO Products (name, slug, price, discount, gender, category_id, material, origin, description) VALUES

-- 1. Áo Thun
(N'Áo Thun Polo Basic', 'ao-thun-polo-basic', 250000, 0, N'Nam', 1, N'100% Cotton pha Spandex', N'Việt Nam', 
N'Item cơ bản không thể thiếu vắng trong tủ đồ của một người đàn ông hiện đại. Thiết kế tối giản, nam tính giúp bạn dễ dàng ứng dụng trong mọi hoàn cảnh từ đi làm đến đi chơi.
Chất liệu dệt cao cấp mang lại sự thoải mái tối đa, giữ form dáng chuẩn chỉnh dù trải qua nhiều lần giặt, luôn giữ cho bạn diện mạo chỉn chu nhất.
Phom dáng: Regular fit ôm nhẹ nhàng, tôn dáng vạm vỡ.
Chi tiết: Cổ bẻ dệt kim chắc chắn, cúc gài tiệp màu áo.
Chất liệu: 100% Cotton pha sợi Spandex, co giãn 4 chiều cực kỳ thoải mái.
Màu sắc: Đen / Trắng / Xanh Navy cơ bản.
Phù hợp: Môi trường công sở thoải mái, cafe dạo phố, thể thao nhẹ nhàng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Thun Graphic Tee', 'ao-thun-graphic-tee', 220000, 5, N'Unisex', 1, N'Cotton 100% định lượng cao', N'Việt Nam', 
N'Biến chiếc áo thun trở thành bức tranh thể hiện cá tính và gu thẩm mỹ riêng biệt của bạn. Họa tiết Graphic in nổi bật mang đậm hơi thở nghệ thuật đương đại và văn hóa đường phố.
Sử dụng công nghệ in ấn tiên tiến giúp hình in bền màu, sắc nét và không bị nứt gãy. Một chiếc áo "nói thay tiếng lòng" của thế hệ Z năng động.
Phom dáng: Oversize form rộng rãi, tay lỡ che khuyết điểm.
Chi tiết: Cổ tròn bo gân, hình in Typography hoặc Graphic nghệ thuật sắc nét.
Chất liệu: Cotton 100% định lượng cao (250gsm) đứng form, không bai dão.
Màu sắc: Nền đen / trắng nổi bật họa tiết.
Phù hợp: Dạo phố, trượt ván, gặp gỡ bạn bè, đi concert âm nhạc.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Thun Tay Lỡ Form Rộng', 'ao-thun-tay-lo', 195000, 0, N'Unisex', 1, N'Cotton thun dày dặn', N'Việt Nam', 
N'Món đồ chuẩn phong cách thời trang ulzzang Hàn Quốc đang làm mưa làm gió. Thiết kế tay lỡ mang lại sự thoải mái tuyệt đối và che khéo khuyết điểm bắp tay to.
Chất vải dày dặn nhưng vẫn đảm bảo độ thoáng khí, giúp chiếc áo luôn giữ được độ phồng tự nhiên, không bị rũ hay bám dính vào cơ thể.
Phom dáng: Dáng rộng (Loose fit), tay áo lửng qua nách.
Chi tiết: Cổ tròn cơ bản, viền may chắc chắn, thiết kế trơn không họa tiết.
Chất liệu: Cotton thun dày dặn, thấm hút mồ hôi tốt.
Màu sắc: Trắng / Xám xỉn (Charcoal) / Xanh rêu.
Phù hợp: Mặc hàng ngày, đi học, đi chơi; dễ dàng mix cùng quần short, quần jean ống rộng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 2. Áo Sơ Mi
(N'Áo Sơ Mi Oxford Nam', 'so-mi-oxford-nam', 450000, 10, N'Nam', 2, N'Vải Oxford cao cấp', N'Việt Nam', 
N'Sự giao thoa giữa nét thanh lịch công sở và tính tiện dụng thường ngày (Smart-Casual). Sơ mi Oxford luôn là điểm tựa vững chắc cho phong cách của một quý ông lịch thiệp.
Cấu trúc dệt chéo đặc trưng của vải Oxford mang lại bề mặt nhám nhẹ tinh tế, độ bền cao và đặc biệt rất ít nhăn, giúp tiết kiệm tối đa thời gian là ủi mỗi sáng.
Phom dáng: Slim fit ôm vừa vặn, vạt bầu nam tính.
Chi tiết: Cổ bẻ có cúc gài (Button-down collar) giữ cổ luôn đứng form, có túi ngực nhỏ.
Chất liệu: Vải Oxford cao cấp (Cotton pha Polyester), đứng form, thoáng khí.
Màu sắc: Trắng / Xanh nhạt (Light blue) kinh điển.
Phù hợp: Đi làm, đi hẹn hò, đi học; cực đẹp khi mặc phối layer trong áo len cổ tim.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Kiểu Nữ Voan', 'so-mi-voan-nu', 320000, 0, N'Nữ', 2, N'Vải voan lụa mướt mịn', N'Việt Nam', 
N'Gói gọn sự nữ tính, nhẹ nhàng và thanh tao trong một thiết kế duyên dáng. Sơ mi voan cách điệu là "vũ khí" giúp các cô nàng văn phòng luôn tỏa sáng một cách dịu dàng nhất.
Chất liệu voan cát mềm mại rủ xuống tự nhiên kết hợp cùng các chi tiết nhấn nhá điệu đà tạo hiệu ứng thị giác bay bổng, xua tan đi sự cứng nhắc của thời trang công sở truyền thống.
Phom dáng: Dáng suông nhẹ, tạo độ rủ mềm mại.
Chi tiết: Cổ bèo nhún điệu đà hoặc thắt nơ nhẹ, tay bồng nữ tính, cúc ngọc trai tinh tế.
Chất liệu: Vải voan lụa mướt mịn, thoáng mát, nhanh khô.
Màu sắc: Tông màu pastel ngọt ngào (Hồng, Trắng sữa).
Phù hợp: Môi trường công sở, gặp mặt đối tác, dạo phố; mix tuyệt đẹp cùng chân váy bút chì hoặc quần tây.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Sơ Mi Kaki Túi Hộp', 'so-mi-kaki-tui-hop', 380000, 5, N'Nam', 2, N'Vải Kaki thô 100% cotton', N'Việt Nam', 
N'Mang đậm tinh thần của những chiến binh mạnh mẽ. Thiết kế sơ mi Kaki túi hộp mang phong cách quân đội (Military) giúp nam giới thể hiện sự phong trần, bụi bặm và nam tính tuyệt đối.
Chất liệu Kaki dày dặn, bền bỉ chống mài mòn, vô cùng thích hợp cho những hoạt động ngoài trời, dã ngoại hoặc làm áo khoác ngoài (overshirt) đầy cá tính.
Phom dáng: Dáng suông rộng (Regular) tạo cảm giác khỏe khoắn.
Chi tiết: Hai túi hộp lớn ở ngực có nắp gài, cúc bấm kim loại tiện dụng, đường may rập đôi chắc chắn.
Chất liệu: Vải Kaki thô 100% cotton, thấm hút tốt, chịu lực chà xát cao.
Màu sắc: Xanh rêu (Olive) / Vàng cát (Khaki).
Phù hợp: Đi phượt, du lịch, dạo phố; khoác ngoài áo thun basic cực chất.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 3. Jeans
(N'Quần Jeans Ống Suông Nam', 'jeans-ong-suong-nam', 580000, 0, N'Nam', 3, N'Denim 100% cotton cao cấp', N'Việt Nam', 
N'Giải phóng đôi chân khỏi sự gò bó chật chội. Quần jeans ống suông là sự lựa chọn lên ngôi trong xu hướng thời trang đương đại, mang lại vẻ ngoài "cool" ngầu, thoải mái và vô cùng thời thượng.
Đường cắt ống thẳng tắp từ đùi xuống gấu quần giúp che đi mọi khuyết điểm của đôi chân. Một item cân được mọi phong cách từ vintage cổ điển đến streetwear hiện đại.
Phom dáng: Ống suông rộng (Straight/Wide leg), tạo sự phóng khoáng.
Chi tiết: Thiết kế 5 túi truyền thống, wash màu đồng đều, gấu quần rộng phủ lên giày.
Chất liệu: Denim 100% cotton cao cấp, dày dặn, đứng form và không bai dão.
Màu sắc: Xanh Denim truyền thống.
Phù hợp: Dạo phố, đi học, nhảy Hip-hop; mix cùng áo thun oversize và giày Chunky sneaker.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Shorts Jean Rách Nữ', 'short-jean-rach-nu', 290000, 0, N'Nữ', 3, N'Jean cotton co giãn nhẹ', N'Việt Nam', 
N'Mảnh ghép sôi động thổi bùng ngọn lửa nhiệt huyết của mùa hè. Chiếc short jean rách mang lại diện mạo táo bạo, quyến rũ và tràn đầy năng lượng cho những ngày đầy nắng.
Thiết kế cạp cao ăn gian chiều dài đôi chân kết hợp cùng các chi tiết cắt xẻ, cào rách có chủ đích tạo nên sự phá cách đầy cuốn hút nhưng vẫn đảm bảo sự tinh tế cần thiết.
Phom dáng: Cạp cao (High-waisted) hack dáng, ống rộng nhẹ hình chữ A.
Chi tiết: Mài xước nổi bật ở mặt trước, gấu quần cắt tua rua tự nhiên, có túi lớn tiện dụng.
Chất liệu: Jean cotton co giãn nhẹ, giữ form tốt khi giặt máy.
Màu sắc: Xanh dương nhạt (Light blue) cá tính.
Phù hợp: Đi biển, cafe dạo phố, hội hè picnic; mix vô cùng đẹp cùng áo trễ vai hoặc bikini đi biển.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Quần Jeans Slim Fit Xám', 'jeans-slim-fit-xam', 520000, 15, N'Nam', 3, N'Denim pha spandex (thun)', N'Việt Nam', 
N'Bản hòa tấu của sự hiện đại, thanh lịch và nam tính. Gam màu xám khói phá vỡ đi sự nhàm chán của sắc xanh truyền thống, mang lại cho bạn một diện mạo mới mẻ và vô cùng sành điệu.
Thiết kế ôm dọc theo đường viền chân giúp tôn lên vóc dáng khỏe khoắn, đồng thời chất liệu siêu co giãn đảm bảo bạn luôn linh hoạt trong mọi hoạt động thường nhật.
Phom dáng: Dáng Slim Fit ôm vừa, không quá bó sát sát như skinny.
Chi tiết: Wash nhẹ vùng đùi tạo hiệu ứng thon gọn, đường chỉ may tiệp màu đen xám.
Chất liệu: Denim pha spandex (thun), đàn hồi cực tốt, giữ form đầu gối.
Màu sắc: Xám khói (Grey) hiện đại.
Phù hợp: Đi làm (Smart-casual), dạo phố, hẹn hò; mix mượt mà cùng áo sơ mi đen hoặc áo thun trắng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 4. Váy đầm
(N'Đầm Suông Chữ A Tối Giản', 'dam-suong-chu-a', 360000, 0, N'Nữ', 4, N'Vải lụa cát hoặc linen lụa', N'Việt Nam', 
N'Đỉnh cao của sự thanh lịch đến từ những điều giản đơn nhất. Thiết kế tối giản không chi tiết thừa giúp tôn vinh trọn vẹn khí chất thanh nhã của người mặc, bất chấp mọi xu hướng thời trang.
Phom dáng chữ A suông nhẹ là "cứu tinh" giấu dáng hoàn hảo, mang lại sự thoải mái tuyệt đối cho những ngày bạn không biết phải mặc gì nhưng vẫn muốn mình trông thật chỉn chu.
Phom dáng: Dáng chữ A suông nhẹ, chiều dài vừa chạm gối.
Chi tiết: Cổ thuyền hoặc cổ tròn tinh tế, không chít eo bó sát, thiết kế trơn mộc mạc.
Chất liệu: Vải lụa cát hoặc linen lụa cao cấp, độ rủ tốt, không nhăn.
Màu sắc: Đen quyền lực / Be thanh lịch.
Phù hợp: Đi làm văn phòng, dạo phố, đi lễ chùa; hợp với giày búp bê hoặc gót thấp vuông.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Chân Váy Jean Dáng Dài', 'chan-vay-jean-dai', 310000, 5, N'Nữ', 4, N'Denim dệt chéo mềm mại', N'Việt Nam', 
N'Mang đậm tinh thần hoài cổ của những thập niên trước nhưng lại đang là "Hot trend" không thể bỏ lỡ. Chân váy jean midi giúp nàng biến hóa thành cô nàng thanh lịch, đậm chất Retro.
Thiết kế hàng cúc kim loại trải dài dọc thân trước không chỉ là điểm nhấn độc đáo mà còn giúp kéo dài hiệu ứng thị giác, khiến đôi chân trông thon thả và miên man hơn.
Phom dáng: Dáng suông chữ A nhẹ, chiều dài qua bắp chân (Midi).
Chi tiết: Hàng cúc dọc cài phía trước cá tính, có đường xẻ tà đằng sau giúp dễ di chuyển.
Chất liệu: Denim dệt chéo mềm mại, đứng form không bị nhão.
Màu sắc: Xanh đậm (Dark Blue) vintage.
Phù hợp: Dạo phố mùa thu, đi làm, đi chơi; siêu xinh khi phối cùng áo len mỏng, áo thun tay dài sơ vin.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Đầm Dự Tiệc Trễ Vai', 'dam-du-tiec-tre-vai', 550000, 10, N'Nữ', 4, N'Lụa Satin hoặc Phi lụa', N'Việt Nam', 
N'Tâm điểm của mọi sự chú ý trong những đêm tiệc sang trọng. Thiết kế trễ vai khoe khéo xương quai xanh quyến rũ và bờ vai trần hờ hững, giúp nàng tỏa sáng như một nữ thần.
Chất liệu lụa mướt mát kết hợp kỹ thuật xếp nếp tinh xảo tôn lên những đường cong đắt giá của phái đẹp. Sự lựa chọn hoàn mỹ cho những khoảnh khắc cần sự lộng lẫy và kiêu kỳ nhất.
Phom dáng: Dáng ôm nhẹ (Bodycon) hoặc đuôi cá tôn vòng 3.
Chi tiết: Trễ vai bệt quyến rũ, xẻ tà cao khoe chân thon, xếp nếp (ruched) bắt mắt ở phần eo giấu bụng.
Chất liệu: Lụa Satin hoặc Phi lụa cao cấp, độ bóng sang trọng, bắt sáng rực rỡ dưới ánh đèn.
Màu sắc: Đỏ rượu (Wine) / Trắng tinh khiết.
Phù hợp: Dạ hội, tiệc cưới, sự kiện quan trọng; kết hợp cùng trang sức lấp lánh và giày gót nhọn.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 5. Sneakers
(N'Giày Sneaker Canvas Trắng', 'sneaker-canvas-trang', 450000, 0, N'Unisex', 5, N'Vải Canvas bền bỉ', N'Việt Nam', 
N'Sự tinh khiết, trẻ trung và tính ứng dụng vượt trội. Đôi giày canvas trắng là biểu tượng bất diệt của sự năng động, một nền tảng hoàn hảo làm nổi bật bất kỳ bộ trang phục nào bạn khoác lên người.
Thiết kế cổ điển tối giản không bao giờ lỗi mốt. Chất vải mộc mạc cùng đế cao su bền bỉ đồng hành cùng bạn trên mọi nẻo đường từ trường học đến những chuyến đi xa.
Phom dáng: Cổ thấp basic, phom gọn gàng ôm chân.
Chi tiết: Trắng trơn hoàn toàn (All white), lỗ xỏ dây viền kim loại, đường chỉ dập đôi chắc chắn.
Chất liệu: Vải Canvas bền bỉ chịu ma sát tốt, đế cao su lưu hóa linh hoạt.
Màu sắc: Trắng tinh khôi.
Phù hợp: Học sinh sinh viên, dạo phố, trang phục thường ngày; phù hợp mix với MỌI phong cách.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Sneaker Da Lộn Retro', 'sneaker-da-lon-retro', 1250000, 10, N'Unisex', 5, N'Da lộn phối lưới nilon', N'Việt Nam', 
N'Gợi nhớ về thời kỳ hoàng kim của thể thao thập niên 70, 80. Đôi sneaker mang phong cách hoài cổ (Retro) đem lại diện mạo cực "chill", phong trần và đầy tính nghệ thuật.
Sự kết hợp hoàn hảo giữa các mảng da lộn phối màu độc đáo và phần đế cao su rãnh sâu bám đường. Một đôi giày kể câu chuyện về gu thời trang tinh tế và hoài niệm của người mang.
Phom dáng: Form dáng thon gọn cổ điển (Classic runner).
Chi tiết: Các mảng da cắt xẻ đan xen, logo sọc bên hông sắc nét, lưỡi gà lót mút xốp vintage.
Chất liệu: Da lộn (Suede) phối lưới nilon thoáng khí, đế giữa EVA hấp thụ lực, đế ngoài cao su gai.
Màu sắc: Xanh viền vàng / Đỏ đô vintage.
Phù hợp: Dạo phố, cafe, phong cách vintage/indie; cực hợp khi mang cùng tất cổ cao và quần nhung tăm.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Sneaker Đế Bánh Mì Nữ', 'sneaker-banh-mi-nu', 850000, 0, N'Nữ', 5, N'Da PU cao cấp chống bẩn', N'Việt Nam', 
N'Vũ khí tối mật giúp các nàng "nấm lùn" nâng tầm chiều cao và sự tự tin ngay tức thì. Thiết kế đế bánh mì (Platform) siêu dày nhưng lại vô cùng nhẹ nhàng, không gây mỏi khi di chuyển.
Sở hữu vẻ ngoài kẹo ngọt, xinh xắn và vô cùng dễ thương. Đôi giày là điểm nhấn đáng yêu hoàn thiện những bộ trang phục trẻ trung, năng động của phái nữ.
Phom dáng: Đế bằng siêu cao (4-5cm), mũi giày bầu bĩnh đáng yêu.
Chi tiết: Viền đế dập vân lượn sóng sành điệu, dây giày bản to thắt nơ nữ tính.
Chất liệu: Da PU cao cấp chống bám bẩn, đế Phylon siêu nhẹ như xốp, bám đường cực tốt.
Màu sắc: Trắng sữa / Be nữ tính.
Phù hợp: Đi học, dạo phố, hack dáng chụp hình; mix cực xinh với chân váy xếp ly, quần short.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 6. Leather Shoes
(N'Giày Oxford Classic Nam', 'giay-oxford-classic', 1850000, 0, N'Nam', 6, N'Da bò nguyên tấm', N'Việt Nam', 
N'Chuẩn mực tối thượng của sự sang trọng và trang trọng (Formal wear). Oxford là đôi giày danh giá mà mọi quý ông đều phải sở hữu để chuẩn bị cho những sự kiện mang tính bước ngoặt của cuộc đời.
Thiết kế hệ thống dây buộc khép kín (Closed lacing) mang lại phom dáng cực kỳ thon gọn, sắc sảo. Được chế tác từ những lớp da nguyên miếng thượng hạng nhất tạo nên độ bóng bẩy, đẳng cấp không thể chối từ.
Phom dáng: Giày tây trang trọng, mũi giày thon dài thanh lịch, ôm sát mu bàn chân.
Chi tiết: Hệ thống viền dây kín chuẩn mực (Balmoral), cấu trúc Cap-toe đục lỗ họa tiết nhẹ nhàng tinh tế.
Chất liệu: Da bò nguyên tấm (Full-grain calfskin) đánh bóng thủ công, lót trong bằng da bê êm ái, đế da ép kết hợp cao su chống trượt.
Màu sắc: Đen bóng (Polished Black).
Phù hợp: Chú rể tiệc cưới, hội nghị cấp cao, CEO, Giám đốc; chỉ kết hợp cùng Suit / Tuxedo trang trọng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Giày Mules Da Nữ', 'giay-mules-nu', 650000, 5, N'Nữ', 6, N'Da Microfiber hoặc PU', N'Việt Nam', 
N'Giao điểm của sự thanh lịch kiêu kỳ và tính tiện dụng tuyệt đối. Giày Mules hở gót giải phóng đôi chân khỏi sự bí bách nhưng vẫn giữ được vẻ ngoài lịch sự, chuyên nghiệp của những đôi giày bít mũi.
Sự lựa chọn hoàn hảo cho những cô nàng công sở bận rộn. Chỉ cần một thao tác xỏ chân, bạn đã có ngay một diện mạo thời thượng để sải bước đến văn phòng hay đi dạo phố cuối tuần.
Phom dáng: Dáng sục hở gót (Mules), mũi nhọn dài thanh mảnh kiêu kỳ.
Chi tiết: Lược bỏ hoàn toàn phần quai hậu đằng sau, đính khóa kim loại chữ H hoặc nơ trang trí ở mũi giày.
Chất liệu: Da Microfiber hoặc PU cao cấp mềm mại, ôm form chân, lót đệm xốp êm ái chống mỏi.
Màu sắc: Be (Beige) / Đen / Trắng kem.
Phù hợp: Dân văn phòng, hẹn hò, cafe; dễ dàng kết hợp cùng quần ống rộng culottes, váy suông.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 7. Bags
(N'Balo Thời Trang Mini Nữ', 'balo-mini-nu', 420000, 0, N'Nữ', 7, N'Da PU cao cấp kháng nước', N'Việt Nam', 
N'Trẻ trung, nữ tính và cực kỳ linh hoạt. Balo mini là phụ kiện tạo điểm nhấn đáng yêu cho các cô gái, mang đến diện mạo tinh nghịch, tươi mới như những nữ sinh Hàn Quốc.
Dù có kích thước nhỏ gọn nhưng không gian bên trong lại vô cùng tối ưu, chứa đủ những "vật bất ly thân" của phái đẹp từ son môi, đồ trang điểm, điện thoại đến iPad mini hay sổ tay.
Phom dáng: Kích thước mini siêu gọn nhẹ, form đứng cứng cáp không bị bẹp móp.
Chi tiết: Dây đeo mảnh mai có thể điều chỉnh hoặc tháo rời biến thành túi xách tay, nắp gập khóa cài kim loại bắt mắt.
Chất liệu: Da PU cao cấp kháng nước bám bụi, lót trong vải poly dễ vệ sinh.
Màu sắc: Trắng sữa / Đen basic.
Phù hợp: Dạo phố, cafe cuối tuần, chụp hình; phối cực xinh với váy yếm, áo croptop.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Túi Đeo Chéo Canvas Unisex', 'tui-deo-cheo-canvas', 280000, 5, N'Unisex', 7, N'Vải Canvas (vải bố)', N'Việt Nam', 
N'Chân ái của những tâm hồn năng động và thực tế. Túi đeo chéo bằng vải bố mang tinh thần đường phố bụi bặm, bền bỉ và sẵn sàng cùng bạn "quăng quật" trên mọi nẻo đường.
Thiết kế nhiều ngăn kéo từ trong ra ngoài giúp phân loại đồ đạc cực kỳ khoa học. Thao tác lấy và cất đồ chưa bao giờ dễ dàng và nhanh chóng đến thế.
Phom dáng: Dáng chữ nhật ngang mỏng nhẹ, ôm sát hông linh hoạt.
Chi tiết: Nhiều ngăn khóa zip phía trước và sau tiện dụng, dây đeo dù to bản điều chỉnh nấc êm ái chống hằn vai.
Chất liệu: Vải Canvas (vải bố) nguyên bản siêu bền, khó bị vật nhọn làm rách, dễ dàng giặt máy.
Màu sắc: Đen / Xanh rêu / Vàng bò mộc mạc.
Phù hợp: Đi làm tự do, đi học thêm, chạy xe máy dạo phố; mix sành điệu với thời trang Streetwear.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 8. Accessories
(N'Kính Mát Phi Công', 'kinh-mat-phi-cong', 350000, 0, N'Unisex', 8, N'Hợp kim siêu nhẹ', N'Việt Nam', 
N'Món phụ kiện "huyền thoại" bừng sáng cá tính, mạnh mẽ và ngông cuồng. Thiết kế lấy cảm hứng từ những phi công không quân Mỹ chưa bao giờ hết hot, phù hợp hoàn hảo với mọi dáng khuôn mặt.
Không chỉ sở hữu vẻ ngoài ngầu lòi, tròng kính còn được tráng lớp gương phân cực, bảo vệ đôi mắt tối đa khỏi các tia UV độc hại, giúp tầm nhìn luôn dịu mát và chân thực nhất.
Thiết kế: Dáng mắt giọt lệ (Aviator) kinh điển, gọng đôi chắc chắn ở phần cầu mũi.
Chi tiết: Ve mũi silicon trong suốt mềm mại, càng kính bọc đệm êm ái chống đau vành tai.
Chất liệu: Gọng hợp kim siêu nhẹ chống gỉ sét mạ điện mướt mắt, tròng kính Polycarbonate chống vỡ, chống tia UV400 chuẩn quốc tế.
Màu sắc: Gọng bạc tròng đen / Gọng vàng tròng xanh rêu.
Phù hợp: Lái xe ngoài trời râm mát, du lịch biển, dạo phố, chụp hình lookbook cực ngầu.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Thắt Lưng Vải Canvas', 'that-lung-vai-canvas', 120000, 0, N'Nam', 8, N'Vải bạt Canvas dệt kim', N'Việt Nam', 
N'Điểm nhấn phá cách mang đậm tinh thần tự do, trẻ trung và khỏe khoắn. Thắt lưng vải bố sinh ra để làm bạn đồng hành cùng những chiếc quần short năng động hay quần ống rộng đường phố.
Cơ chế khóa D-ring trượt vô cấp siêu thông minh, không cần đục lỗ gò bó, cho phép bạn điều chỉnh vòng eo linh hoạt chính xác đến từng milimet, đảm bảo sự thoải mái tuyệt đối sau mỗi bữa ăn.
Thiết kế: Bản vải rộng 3-4cm, thân thắt lưng dài có thể thả buông thõng đầu dây tạo phong cách Hiphop.
Chi tiết: Khóa cài đôi chữ D (D-ring) bằng hợp kim chống gỉ, đầu dây bọc da/kim loại chống tưa chỉ.
Chất liệu: Vải bạt Canvas dệt kim dày dặn, chịu lực kéo cực lớn, không phai màu.
Màu sắc: Đen / Kẻ sọc màu sắc nổi bật.
Phù hợp: Phong cách Casual năng động, mặc cùng quần short, quần túi hộp Cargo, ván trượt.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

-- 9. Jackets
(N'Áo Khoác Gió Chống Nước', 'ao-khoac-gio-chong-nuoc', 450000, 10, N'Unisex', 9, N'Vải dù gió Micro-Polyester', N'Việt Nam', 
N'Lá chắn thép bảo vệ bạn trước những sự đỏng đảnh thất thường của thời tiết nhiệt đới. Áo khoác gió siêu nhẹ là trang bị không thể thiếu cho các tín đồ mê xê dịch, thể thao ngoài trời.
Công nghệ vải trượt nước (Water-repellent) giúp những hạt mưa dễ dàng lăn dài trôi đi. Thiết kế lót lưới tản nhiệt bên trong ngăn chặn cảm giác dính nhớp mồ hôi, giúp cơ thể luôn khô thoáng, dễ chịu.
Phom dáng: Dáng suông thể thao bo nhẹ gấu, cực kỳ linh hoạt cho vận động.
Chi tiết: Mũ trùm che mưa có khóa kéo tháo rời, cổ cao cản gió, túi khóa kéo ép nhiệt chống thấm nước an toàn.
Chất liệu: Vải dù gió Micro-Polyester cản gió cản nước xuất sắc, lớp lót trong bằng lưới Mesh siêu thoáng khí.
Màu sắc: Xanh Navy / Đen / Vàng chanh dạ quang.
Phù hợp: Đi phượt bằng xe máy, chạy bộ sáng sớm, đạp xe, leo núi trekking.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Khoác Jean Oversize', 'ao-khoac-jean-oversize', 620000, 0, N'Unisex', 9, N'Denim thô dày dặn', N'Việt Nam', 
N'Bản tuyên ngôn bất diệt của tuổi trẻ, sự tự do và phong cách đường phố bụi bặm. Chiếc áo khoác bò form rộng mang lại diện mạo sành điệu, "cool ngầu" chỉ trong tích tắc cho cả nam và nữ.
Thiết kế form thùng thình cá tính không chỉ giúp bạn giấu đi mọi khuyết điểm hình thể mà còn cực kỳ dễ dàng layering (mặc chồng lớp) với áo hoodie dày cộm bên trong vào những ngày đại hàn.
Phom dáng: Oversize rộng rãi xé bỏ ranh giới giới tính (Unisex), vai xệ (Drop shoulder) trễ nải cá tính.
Chi tiết: Cổ bẻ cứng cáp, khuy đồng đúc sắc nét, hai túi ốp ngực nắp gập và hai túi sườn nhét tay tiện dụng.
Chất liệu: Denim thô dày dặn, cầm nặng tay, đứng form, cực kỳ bền bỉ chống mài mòn.
Màu sắc: Xanh Wash nhạt bụi bặm / Đen than (Charcoal).
Phù hợp: Street style đường phố, đi học, cafe chụp hình; mix cực cháy với áo thun trơn, quần thụng vành rộng.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.'),

(N'Áo Khoác Dù 2 Lớp', 'ao-khoac-du-2-lop', 390000, 5, N'Nam', 9, N'Vải dù (Nylon)', N'Việt Nam', 
N'Vật bất ly thân đồng hành cùng những chàng trai nắng sương trên mọi nẻo đường. Áo khoác dù mang đến sự tiện lợi tối đa: cản nắng rát vào mùa hè, chắn gió rét vào mùa đông.
Thiết kế tinh giản không phô trương với cấu trúc 2 lớp thông minh. Một chiếc áo nhẹ tênh có thể dễ dàng cuộn tròn nhét gọn vào cốp xe máy, luôn sẵn sàng khi bạn cần đến.
Phom dáng: Regular fit ôm vừa phải, khỏe khoắn năng động.
Chi tiết: Cổ đứng có khóa kéo kín cổ cản gió lùa, bo chun viền tay áo và gấu áo ôm sát cơ thể.
Chất liệu: Lớp ngoài vải dù (Nylon) bề mặt trơn mịn, lớp lót dù gió mỏng nhẹ bên trong, cản bụi xuất sắc.
Màu sắc: Đen basic / Ghi xám / Xanh đen.
Phù hợp: Di chuyển ngoài trời bằng xe máy, chống nắng hằng ngày, khoác nhẹ ban đêm.
Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp và màu sắc hiển thị qua màn hình máy tính/ điện thoại.');


go

-- D. TẠO BIẾN THỂ (KHO HÀNG)
DECLARE @p_id INT;
DECLARE @c_id INT;
DECLARE cursor_product CURSOR FOR SELECT id, category_id FROM Products;

OPEN cursor_product;
FETCH NEXT FROM cursor_product INTO @p_id, @c_id;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Quần áo
    IF @c_id IN (1, 2, 3, 4, 9)
    BEGIN
        INSERT INTO ProductVariants (product_id, color, size, quantity, sku) VALUES
        (@p_id, N'Đen', 'S', 20, 'SKU-' + CAST(@p_id AS VARCHAR) + '-B-S'),
        (@p_id, N'Đen', 'M', 30, 'SKU-' + CAST(@p_id AS VARCHAR) + '-B-M'),
        (@p_id, N'Trắng', 'M', 25, 'SKU-' + CAST(@p_id AS VARCHAR) + '-W-M');
    END
    -- Giày
    ELSE IF @c_id IN (5, 6)
    BEGIN
        INSERT INTO ProductVariants (product_id, color, size, quantity, sku) VALUES
        (@p_id, N'Tiêu chuẩn', '40', 15, 'SKU-' + CAST(@p_id AS VARCHAR) + '-40'),
        (@p_id, N'Tiêu chuẩn', '41', 15, 'SKU-' + CAST(@p_id AS VARCHAR) + '-41');
    END
    -- Túi/PK
    ELSE
    BEGIN
        INSERT INTO ProductVariants (product_id, color, size, quantity, sku) VALUES
        (@p_id, N'Mặc định', 'Free', 50, 'SKU-' + CAST(@p_id AS VARCHAR) + '-FREE');
    END
    FETCH NEXT FROM cursor_product INTO @p_id, @c_id;
END;
CLOSE cursor_product;
DEALLOCATE cursor_product;

-- E. TẠO ẢNH GALLERY (LOGIC THÔNG MINH)
-- 1. Insert ảnh chính vào gallery trước
INSERT INTO ProductImages (product_id, image_url) SELECT id, image FROM Products;

-- 2. Insert thêm ảnh phụ theo từng loại (URL Ảnh thật)
-- Áo thun nam (ID 1)
--INSERT INTO ProductImages (product_id, image_url) VALUES 
--(1, N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/October2024/ao-thun-nam-cotton-coolmate-basics-200gsm-mau-trang-2.jpg'),
--(1, N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/October2024/ao-thun-nam-cotton-coolmate-basics-200gsm-mau-trang-3.jpg');

-- Giày Nike (ID 8) - Ví dụ ID 8 là Nike AF1
--INSERT INTO ProductImages (product_id, image_url)
--SELECT id, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/4f37fca8-6bce-43e7-ad07-f57ae3c13142/air-force-1-07-shoes-WrLlWX.png'
--FROM Products WHERE slug = 'nike-af1';

-- F. VOUCHER
INSERT INTO Vouchers (code, discount_percent, start_date, end_date, quantity) VALUES
('OPENING', 20, GETDATE(), DATEADD(day, 30, GETDATE()), 100),
('FREESHIP', 100, GETDATE(), DATEADD(day, 7, GETDATE()), 50);

-- G. ĐƠN HÀNG TEST
INSERT INTO Orders (account_id, status, total_amount, receiver_name, receiver_phone, shipping_address) VALUES
(2, 'PENDING', 598000, N'Nguyễn Văn A', '0909000222', N'123 Đường Láng, Hà Nội');

PRINT '✅ DATABASE SHOPOMG FINAL (REAL IMAGES) DEPLOYED SUCCESSFULLY!';
GO

USE ShopOMG;
GO

ALTER TABLE Accounts
ADD birth_date DATE NULL,
    gender NVARCHAR(10) NULL;
GO

-- Cập nhật lại ảnh chính cho sản phẩm 
UPDATE p
SET p.image = data.new_path
FROM Products p
INNER JOIN (VALUES 
    (1, '/images/products/polo-nam-den.webp'),
    (2, '/images/products/ao-thun-nam-den.jfif'),
    (3, '/images/products/ao-3-lo-nam-den.jpg'),
	(4, '/images/products/ao-thun-dai-tay-unisex-den.jpg'),
	(5, '/images/products/croptop-nu-den.jpg'),
	(6, '/images/products/thun-soc-unisex-den.jpg'),
	(7, '/images/products/thun-raglan-nam-den.jpg'),
	(8, '/images/products/flannel-caro-nam-den.png'),
	(9, '/images/products/cong-so-nu-trang.jpg'),
	(10, '/images/products/denim-nam-den.jfif'),
	(11, '/images/products/so-mi-mua-he-nam-trang.jpg'),
	(12, '/images/products/linen-nu-den.jpg'),
	(13, '/images/products/so-mi-co-tau-nam-den.webp'),
	(14, '/images/products/so-mi-voan-no-co-nu-den.jpg'),
	(15, '/images/products/slimfit-nam-trang.jpg'),
	(16, '/images/products/jean-rach-goi-nam-den.jpg'),
	(17, '/images/products/skinny-jean-nu-den.webp'),
	(18, '/images/products/baggy-jeans-unisex-den.webp'),
	(19, '/images/products/short-jeans-nam-den.jpg'),
	(20, '/images/products/short-jeans-nu-den.jfif'),
	(21, '/images/products/jeans-den-tron-nam.jpg'),
	(22, '/images/products/jeans-trang-nu.jpg'),
	(23, '/images/products/mom-jeans-nu-den.jpg'),
	(24, '/images/products/maxi-di-bien-nu-den.jpg'),
	(25, '/images/products/dam-body-nu-trang.webp'),
	(26, '/images/products/vay-xep-ly-nu-trang.jfif'),
	(27, '/images/products/dam-cong-so-chu-A-nu-den.webp'),
	(28, '/images/products/vay-2-day-lua-nu-trang.jfif'),
	(29, '/images/products/chan-vay-jeans-nu-den.jpg'),
	(30, '/images/products/dam-yem-jean-nu-den.jpg'),
	(31, '/images/products/dam-vintage-nu-den.jpg'),
	(32, '/images/products/converse-chuck-taylor-all-star-1.webp'),
	(33, '/images/products/vans-old-skool-jpeg-1.jpeg'),
	(34, '/images/products/New-balance-1.webp'),
	(35, '/images/products/MLB-Chunky-Liner-1.jpg'),
	(36, '/images/products/Slip-on-1.jpg'),
	(37, '/images/products/running-1.jpg'),
	(38, '/images/products/sneaker-high-top-1.jpg'),
	(39, '/images/products/the-thao-nu-1.webp'),
	(40, '/images/products/chelsea-boot-nam-1.png')
) AS data(id, new_path) ON p.id = data.id;
go
UPDATE p
SET p.image = data.new_path
FROM Products p
INNER JOIN (VALUES 
	(41, '/images/products/loafer-penny-1.jpg'),
	(42, '/images/products/derby-da-lon-1.webp'),
	(43, '/images/products/monk-strap-1.jpg'),
	(44, '/images/products/giay-moi-lai-xe-1.jfif'),
	(45, '/images/products/boot-da-nu-1.jpg'),
	(46, '/images/products/cao-got-da-that-1.jpg'),
	(47, '/images/products/sandal-da-nam-1.jfif'),
	(48, '/images/products/brogue-duc-lo-1.jfif'),
	(49, '/images/products/balo-da-1.webp'),
	(50, '/images/products/tote-vai-canvas-1.webp'),
	(51, '/images/products/vi-da-nam-1.jpg'),
	(52, '/images/products/vi-ngan-nu-1.jfif'),
	(53, '/images/products/tui-messenger-deo-cheo-1.webp'),
	(54, '/images/products/tui-du-lich-1.jfif'),
	(55, '/images/products/tui-deo-hong-bao-tu-1.jpg'),
	(56, '/images/products/cap-da-cong-so-1.webp'),
	(57, '/images/products/satchel-co-dien-1.jpg'),
	(58, '/images/products/that-lung-da-bo-1.jpg'),
	(59, '/images/products/mu-luoi-trai-NY-1.jpg'),
	(60, '/images/products/mu-bucket-1.jpg')

) AS data(id, new_path) ON p.id = data.id;

go
UPDATE p
SET p.image = data.new_path
FROM Products p
INNER JOIN (VALUES 
	(61, '/images/products/kinh-mat-1.webp'),
	(62, '/images/products/dong-ho-day-da-1.webp'),
	(63, '/images/products/tat-co-cao-1.webp'),
	(64, '/images/products/khan-choang-co-len-1.jpg'),
	(65, '/images/products/ca-vat-lua-1.webp'),
	(66, '/images/products/vong-tay-bac-1.webp'),
	(67, '/images/products/no-cai-ao-vest-1.jpg'),
	(68, '/images/products/hoodie-basic-unisex-den.webp'),
	(69, '/images/products/sweater-tron-unisex-trang.webp'),
	(70, '/images/products/bomber-nam-den.jfif'),
	(71, '/images/products/denim-unisex-den.jpg'),
	(72, '/images/products/blazer-nam-den.jpg'),
	(73, '/images/products/vest-cong-so-nam-den.jpg'),
	(74, '/images/products/cardigan-len-nu-den.jpg'),
	(75, '/images/products/biker-nam-den.webp'),
	(76, '/images/products/ao-mang-nu-den.jpg'),
	(77, '/images/products/polo-basic-nam-den.jpg'),
	(78, '/images/products/graphic-tee-unisex-den.jpg'),
	(79, '/images/products/ao-thun-form-rong-unisex-den.jpg'),
	(80, '/images/products/oxford-nam-den.jpg')

) AS data(id, new_path) ON p.id = data.id;

go
UPDATE p
SET p.image = data.new_path
FROM Products p
INNER JOIN (VALUES 
	(81, '/images/products/so-mi-voan-nu-den.jpg'),
	(82, '/images/products/so-mi-kaki-nam-den.jpg'),
	(83, '/images/products/jeans-ong-suong-nam-den.webp'),
	(84, '/images/products/short-jeans-rach-nu-den.jpg'),
	(85, '/images/products/jeans-slim-fit-nam-den.webp'),
	(86, '/images/products/dam-suong-chu-A-trang.jpeg'),
	(87, '/images/products/vay-jeans-dang-dai.webp'),
	(88, '/images/products/dam-du-tiec-trang.webp'),
	(89, '/images/products/sneaker-canvas-trang.jfif'),
	(90, '/images/products/sneaker-retro-1.webp'),
	(91, '/images/products/sneaker-de-banh-mi-1.webp'),
	(92, '/images/products/oxford-classic-1.jpg'),
	(93, '/images/products/mules-da-1.jpg'),
	(94, '/images/products/balo-nu-mini-1.jpg'),
	(95, '/images/products/tui-deo-cheo-canvas-1.jfif'),
	(96, '/images/products/kinh-mat-phi-cong-1.jpg'),
	(97, '/images/products/that-lung-canvas-1.png'),
	(98, '/images/products/ao-khoac-gio-den.jpg'),
	(99, '/images/products/ao-khoac-jean-trang.jpg'),
	(100, '/images/products/ao-khoac-du-trang.jpeg')

) AS data(id, new_path) ON p.id = data.id;

go
-- Cập nhật lại ảnh phụ cho sản phẩm 
INSERT INTO ProductImages (product_id, image_url)
VALUES 
    (1, '/images/products/polo-nam-trang.webp'),
    (2, '/images/products/ao-thun-nam-trang.jfif'),
    (3, '/images/products/ao-3-lo-nam-trang.webp'),
	(4, '/images/products/ao-thun-dai-tay-unisex-trang.jpg'),
	(5, '/images/products/croptop-nu-trang.jpg'),
	(6, '/images/products/thun-soc-unisex-trang.webp'),
	(7, '/images/products/thun-raglan-nam-trang.webp'),
	(8, '/images/products/flannel-caro-nam-trang.jpg'),
	(9, '/images/products/cong-so-nu-den.webp'),
	(10, '/images/products/denim-nam-trang.jpg'),
	(11, '/images/products/so-mi-mua-he-nam-den.jpeg'),
	(12, '/images/products/linen-nu-trang.jpg'),
	(13, '/images/products/so-mi-co-tau-nam-trang.jpg'),
	(14, '/images/products/so-mi-voan-no-co-nu-trang.jpg'),
	(15, '/images/products/slimfit-nam-den.jfif'),
	(16, '/images/products/jean-rach-goi-nam-trang.jpg'),
	(17, '/images/products/skinny-jean-nu-trang.jpg'),
	(18, '/images/products/baggy-jeans-unisex-trang.webp'),
	(19, '/images/products/short-jeans-nam-trang.webp'),
	(20, '/images/products/short-jeans-nu-trang.jfif'),
	(21, '/images/products/jeans-trang-tron-nam.jfif'),
	(22, '/images/products/jeans-den-nu.jpg'),
	(23, '/images/products/mom-jeans-nu-trang.jpg'),
	(24, '/images/products/maxi-di-bien-nu-trang.jpg'),
	(25, '/images/products/dam-body-nu-den.webp'),
	(26, '/images/products/vay-xep-ly-nu-den.webp'),
	(27, '/images/products/dam-cong-so-chu-A-nu-trang.webp'),
	(28, '/images/products/vay-2-day-lua-nu-den.jpg'),
	(29, '/images/products/chan-vay-jeans-nu-trang.jpg'),
	(30, '/images/products/dam-yem-jean-nu-trang.jpg'),
	(31, '/images/products/dam-vintage-nu-trang.jpg'),
	(32, '/images/products/converse-chuck-taylor-all-star-2.jfif'),
	(32, '/images/products/converse-chuck-taylor-all-star-3.jpg'),
	(33, '/images/products/vans-old-skool-jpeg-2.webp'),
	(33, '/images/products/vans-old-skool-jpeg-3.webp'),
	(34, '/images/products/New-balance-2.webp'),
	(34, '/images/products/New-balance-3.jpeg'),
	(35, '/images/products/MLB-Chunky-Liner-2.webp'),
	(35, '/images/products/MLB-Chunky-Liner-3.webp'),
	(36, '/images/products/Slip-on-2.jpg'),
	(36, '/images/products/Slip-on-1.jpg'),
	(37, '/images/products/running-2.webp'),
	(38, '/images/products/sneaker-high-top-2.jfif'),
	(38, '/images/products/sneaker-high-top-3.jfif'),
	(39, '/images/products/the-thao-nu-2.jpg'),
	(39, '/images/products/the-thao-nu-3.jpg'),
	(40, '/images/products/chelsea-boot-nam-2.jfif'),
	(40, '/images/products/chelsea-boot-nam-3.webp')
;
-- Cập nhật lại ảnh phụ cho sản phẩm 
INSERT INTO ProductImages (product_id, image_url)
VALUES 
    (41, '/images/products/loafer-penny-2.jpg'),
	(41, '/images/products/loafer-penny-3.jpg'),
	(42, '/images/products/derby-da-lon-2.jfif'),
	(42, '/images/products/derby-da-lon-3.jpg'),
	(43, '/images/products/monk-strap-2.jpg'),
	(43, '/images/products/monk-strap-3.jpg'),
	(44, '/images/products/giay-moi-lai-xe-2.jpg'),
	(44, '/images/products/giay-moi-lai-xe-3.jpg'),
	(45, '/images/products/boot-da-nu-2.jfif'),
	(45, '/images/products/boot-da-nu-3.jpeg'),
	(46, '/images/products/cao-got-da-that-2.png'),
	(46, '/images/products/cao-got-da-that-3.jfif'),
	(47, '/images/products/sandal-da-nam-2.jpg'),
	(47, '/images/products/sandal-da-nam-3.jpg'),
	(48, '/images/products/brogue-duc-lo-2.jfif'),
	(48, '/images/products/brogue-duc-lo-3.png'),
	(49, '/images/products/balo-da-2.jpg'),
	(49, '/images/products/balo-da-3.jpg'),
	(50, '/images/products/tote-vai-canvas-2.jpg'),
	(50, '/images/products/tote-vai-canvas-3.jfif'),
	(51, '/images/products/vi-da-nam-2.jpg'),
	(51, '/images/products/vi-da-nam-3.jfif'),
	(52, '/images/products/vi-ngan-nu-2.webp'),
	(52, '/images/products/vi-ngan-nu-3.jfif'),
	(53, '/images/products/tui-messenger-deo-cheo-2.jfif'),
	(53, '/images/products/tui-messenger-deo-cheo-3.webp'),
	(54, '/images/products/tui-du-lich-2.jpg'),
	(54, '/images/products/tui-du-lich-3.jpg'),
	(55, '/images/products/tui-deo-hong-bao-tu-2.jpg'),
	(55, '/images/products/tui-deo-hong-bao-tu-3.jpg'),
	(56, '/images/products/cap-da-cong-so-2.jpg'),
	(56, '/images/products/cap-da-cong-so-3.jpg'),
	(57, '/images/products/satchel-co-dien-2.jfif'),
	(57, '/images/products/satchel-co-dien-3.jpg'),
	(58, '/images/products/that-lung-da-bo-2.jfif'),
	(58, '/images/products/that-lung-da-bo-3.jfif'),
	(59, '/images/products/mu-luoi-trai-NY-2.jpg'),
	(59, '/images/products/mu-luoi-trai-NY-3.jpg'),
	(60, '/images/products/mu-bucket-2.jpg'),
	(60, '/images/products/mu-bucket-3.jpg')
;

-- Cập nhật lại ảnh phụ cho sản phẩm 
INSERT INTO ProductImages (product_id, image_url)
VALUES 
    (61, '/images/products/kinh-mat-2.jpg'),
	(61, '/images/products/kinh-mat-3.jfif'),
	(62, '/images/products/dong-ho-day-da-2.jpg'),
	(62, '/images/products/dong-ho-day-da-3.jpg'),
	(63, '/images/products/tat-co-cao-2.png'),
	(63, '/images/products/tat-co-cao-3.webp'),
	(64, '/images/products/khan-choang-co-len-2.jfif'),
	(64, '/images/products/khan-choang-co-len-3.jpg'),
	(65, '/images/products/ca-vat-lua-2.jpg'),
	(65, '/images/products/ca-vat-lua-3.jpg'),
	(66, '/images/products/vong-tay-bac-2.jpg'),
	(66, '/images/products/vong-tay-bac-3.jpg'),
	(67, '/images/products/no-cai-ao-vest-2.jfif'),
	(67, '/images/products/no-cai-ao-vest-3.jpg'),
	(68, '/images/products/hoodie-basic-unisex-trang.jpg'),
	(69, '/images/products/sweater-tron-unisex-den.webp'),	
	(70, '/images/products/bomber-nam-trang.webp'),
	(71, '/images/products/denim-unisex-trang.webp'),
	(72, '/images/products/blazer-nam-trang.jpg'),
	(73, '/images/products/vest-cong-so-nam-trang.jpg'),
	(74, '/images/products/cardigan-len-nu-trang.jfif'),
	(75, '/images/products/biker-nam-trang.jpg'),
	(76, '/images/products/ao-mang-nu-trang.jpg'),
	(77, '/images/products/polo-basic-nam-trang.jpg'),
	(78, '/images/products/graphic-tee-unisex-trang.png'),
	(79, '/images/products/ao-thun-form-rong-unisex-trang.jpg'),
	(80, '/images/products/oxford-nam-trang.png')
;
INSERT INTO ProductImages (product_id, image_url)
VALUES 
    (81, '/images/products/so-mi-voan-nu-trang.jpg'),
	(82, '/images/products/so-mi-kaki-nam-trang.webp'),
	(83, '/images/products/jeans-ong-suong-nam-trang.jfif'),
	(84, '/images/products/short-jeans-rach-nu-trang.jpg'),
	(85, '/images/products/jeans-slim-fit-nam-trang.jpg'),
	(86, '/images/products/dam-suong-chu-A-den.jfif'),
	(87, '/images/products/vay-jeans-dang-dai-den.webp'),
	(88, '/images/products/dam-du-tiec-den.webp'),
	(89, '/images/products/sneaker-canvas-trang-2.jpg'),	
	(89, '/images/products/sneaker-canvas-trang-3.jpg'),	
	(90, '/images/products/sneaker-retro-2.jpg'),
	(90, '/images/products/sneaker-retro-3.jpg'),
	(91, '/images/products/sneaker-de-banh-mi-2.webp'),
	(91, '/images/products/sneaker-de-banh-mi-3.webp'),
	(92, '/images/products/oxford-classic-2.jpg'),
	(92, '/images/products/oxford-classic-3.jpeg'),
	(93, '/images/products/mules-da-2.webp'),
	(93, '/images/products/mules-da-3.jpg'),
	(94, '/images/products/balo-nu-mini-2.png'),
	(94, '/images/products/balo-nu-mini-3.jpg'),
	(95, '/images/products/tui-deo-cheo-canvas-2.jpg'),
	(95, '/images/products/tui-deo-cheo-canvas-3.jpg'),
	(96, '/images/products/kinh-mat-phi-cong-2.webp'),
	(96, '/images/products/kinh-mat-phi-cong-3.jpg'),
	(97, '/images/products/that-lung-canvas-2.webp'),
	(97, '/images/products/that-lung-canvas-3.jpg'),
	(98, '/images/products/ao-khoac-gio-trang.jpg'),
	(99, '/images/products/ao-khoac-jean-den.jpg'),
	(100, '/images/products/ao-khoac-du-den.webp')
;
/*
===========================================================================
   MIGRATION: Thêm Tính Năng Email Verification
   DATE: 05/01/2026
   DESCRIPTION: 
   - Thêm field email_verified vào bảng Accounts
   - Tạo bảng EmailVerificationTokens để quản lý token xác thực email
===========================================================================
*/

GO
-- 1. Thêm field email_verified vào bảng Accounts
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'email_verified')
BEGIN
    ALTER TABLE Accounts ADD email_verified BIT DEFAULT 0;
    PRINT '✅ Đã thêm field email_verified vào bảng Accounts!';
    
    -- Sử dụng SQL động để tránh lỗi biên dịch (Invalid column name)
    EXEC('UPDATE Accounts SET email_verified = 1');
    PRINT '✅ Đã cập nhật email_verified = 1 cho tất cả account hiện tại!';
END
ELSE
BEGIN
    PRINT '⚠️ Field email_verified đã tồn tại, bỏ qua.';
END
GO

-- 2. Tạo bảng EmailVerificationTokens
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'EmailVerificationTokens')
BEGIN
    CREATE TABLE EmailVerificationTokens (
        id INT IDENTITY(1,1) PRIMARY KEY,
        account_id INT NOT NULL,
        token VARCHAR(255) NOT NULL UNIQUE,
        expiry_date DATETIME NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_EmailVerificationTokens_Accounts 
            FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE
    );
    
    PRINT '✅ Đã tạo bảng EmailVerificationTokens thành công!';
END
ELSE
BEGIN
    PRINT '⚠️ Bảng EmailVerificationTokens đã tồn tại, bỏ qua.';
END
GO

-- 3. Tạo index để tăng tốc độ query
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_EmailVerificationTokens_Token')
BEGIN
    CREATE INDEX IX_EmailVerificationTokens_Token ON EmailVerificationTokens(token);
    PRINT '✅ Đã tạo index cho token!';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_EmailVerificationTokens_AccountId')
BEGIN
    CREATE INDEX IX_EmailVerificationTokens_AccountId ON EmailVerificationTokens(account_id);
    PRINT '✅ Đã tạo index cho account_id!';
END
GO

PRINT '🎉 Migration Email Verification hoàn tất!';
GO

/*
===========================================================================
   MIGRATION: Thêm Tính Năng Quên Mật Khẩu
   DATE: 05/01/2026
   DESCRIPTION: Tạo bảng PasswordResetTokens để quản lý token reset password
===========================================================================
*/

GO

-- Tạo bảng PasswordResetTokens
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PasswordResetTokens')
BEGIN
    CREATE TABLE PasswordResetTokens (
        id INT IDENTITY(1,1) PRIMARY KEY,
        account_id INT NOT NULL,
        token VARCHAR(255) NOT NULL UNIQUE,
        expiry_date DATETIME NOT NULL,
        used BIT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_PasswordResetTokens_Accounts 
            FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE
    );
    
    PRINT '✅ Đã tạo bảng PasswordResetTokens thành công!';
END
ELSE
BEGIN
    PRINT '⚠️ Bảng PasswordResetTokens đã tồn tại, bỏ qua.';
END
GO

-- Tạo index để tăng tốc độ query
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PasswordResetTokens_Token')
BEGIN
    CREATE INDEX IX_PasswordResetTokens_Token ON PasswordResetTokens(token);
    PRINT '✅ Đã tạo index cho token!';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PasswordResetTokens_AccountId')
BEGIN
    CREATE INDEX IX_PasswordResetTokens_AccountId ON PasswordResetTokens(account_id);
    PRINT '✅ Đã tạo index cho account_id!';
END
GO

PRINT '🎉 Migration hoàn tất!';
GO

/*
===========================================================================
   MIGRATION: Thêm Tính Năng Login Attempt Limiting (Chống Brute-Force)
   DATE: 05/01/2026
   DESCRIPTION: 
   - Thêm các fields tracking login attempts vào bảng Accounts
   - Giới hạn 5 lần đăng nhập sai, khóa tài khoản 15 phút
===========================================================================
*/

GO

-- 1. Thêm field failed_login_attempts
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'failed_login_attempts')
BEGIN
    ALTER TABLE Accounts ADD failed_login_attempts INT DEFAULT 0;
    PRINT '✅ Đã thêm field failed_login_attempts vào bảng Accounts!';

    -- Cập nhật tất cả account hiện tại về 0
    UPDATE Accounts SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
    PRINT '✅ Đã cập nhật failed_login_attempts = 0 cho tất cả account!';
END
ELSE
BEGIN
    PRINT '⚠️ Field failed_login_attempts đã tồn tại, bỏ qua.';
END
GO

-- 2. Thêm field account_locked_until
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'account_locked_until')
BEGIN
    ALTER TABLE Accounts ADD account_locked_until DATETIME NULL;
    PRINT '✅ Đã thêm field account_locked_until vào bảng Accounts!';
END
ELSE
BEGIN
    PRINT '⚠️ Field account_locked_until đã tồn tại, bỏ qua.';
END
GO

-- 3. Thêm field last_login
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'last_login')
BEGIN
    ALTER TABLE Accounts ADD last_login DATETIME NULL;
    PRINT '✅ Đã thêm field last_login vào bảng Accounts!';
END
ELSE
BEGIN
    PRINT '⚠️ Field last_login đã tồn tại, bỏ qua.';
END
GO

-- 4. Tạo index để tăng tốc độ query
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Accounts_AccountLockedUntil')
BEGIN
    CREATE INDEX IX_Accounts_AccountLockedUntil ON Accounts(account_locked_until);
    PRINT '✅ Đã tạo index cho account_locked_until!';
END
GO

PRINT '🎉 Migration Login Attempt Limiting hoàn tất!';
GO

/*
===========================================================================
   MIGRATION: Tính Năng Bảo Mật Toàn Diện (Security Features)
   DATE: 12/01/2026
   VERSION: 1.0
   DESCRIPTION: 
   - Thêm tính năng Email Verification (Xác thực email)
   - Thêm tính năng Forgot Password (Quên mật khẩu)
   - Thêm tính năng Login Attempt Limiting (Chống Brute-Force)
   
   FEATURES:
   1. Email Verification: Xác thực email khi đăng ký tài khoản mới
   2. Password Reset: Cho phép người dùng reset mật khẩu qua email
   3. Login Security: Giới hạn số lần đăng nhập sai, khóa tài khoản tạm thời
   
   AUTHOR: ShopOMG Development Team
===========================================================================
*/

USE ShopOMG;
GO

PRINT '========================================';
PRINT 'BẮT ĐẦU MIGRATION: SECURITY FEATURES';
PRINT '========================================';
GO

-- ============================================================================
-- PHẦN 1: CẬP NHẬT BẢNG ACCOUNTS (Thêm các fields bảo mật)
-- ============================================================================

PRINT '';
PRINT '--- PHẦN 1: Cập nhật bảng Accounts ---';
GO

-- 1.1. Thêm field email_verified (Email Verification)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'email_verified')
BEGIN
    ALTER TABLE Accounts ADD email_verified BIT DEFAULT 0;
    PRINT '✅ [1/6] Đã thêm field email_verified vào bảng Accounts';
    
    -- Cập nhật tất cả account hiện tại đã được verify (tránh ảnh hưởng user cũ)
    EXEC('UPDATE Accounts SET email_verified = 1');
    PRINT '   ℹ️  Đã cập nhật email_verified = 1 cho tất cả account hiện tại';
END
ELSE
BEGIN
    PRINT '⚠️  [1/6] Field email_verified đã tồn tại, bỏ qua';
END
GO

-- 1.2. Thêm field failed_login_attempts (Login Attempt Limiting)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'failed_login_attempts')
BEGIN
    ALTER TABLE Accounts ADD failed_login_attempts INT DEFAULT 0;
    PRINT '✅ [2/6] Đã thêm field failed_login_attempts vào bảng Accounts';
    
    -- Cập nhật tất cả account hiện tại về 0
    UPDATE Accounts SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
    PRINT '   ℹ️  Đã cập nhật failed_login_attempts = 0 cho tất cả account';
END
ELSE
BEGIN
    PRINT '⚠️  [2/6] Field failed_login_attempts đã tồn tại, bỏ qua';
END
GO

-- 1.3. Thêm field account_locked_until (Login Attempt Limiting)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'account_locked_until')
BEGIN
    ALTER TABLE Accounts ADD account_locked_until DATETIME NULL;
    PRINT '✅ [3/6] Đã thêm field account_locked_until vào bảng Accounts';
END
ELSE
BEGIN
    PRINT '⚠️  [3/6] Field account_locked_until đã tồn tại, bỏ qua';
END
GO

-- 1.4. Thêm field last_login (Login Tracking)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'last_login')
BEGIN
    ALTER TABLE Accounts ADD last_login DATETIME NULL;
    PRINT '✅ [4/6] Đã thêm field last_login vào bảng Accounts';
END
ELSE
BEGIN
    PRINT '⚠️  [4/6] Field last_login đã tồn tại, bỏ qua';
END
GO

-- ============================================================================
-- PHẦN 2: TẠO BẢNG EMAIL VERIFICATION TOKENS
-- ============================================================================

PRINT '';
PRINT '--- PHẦN 2: Tạo bảng EmailVerificationTokens ---';
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'EmailVerificationTokens')
BEGIN
    CREATE TABLE EmailVerificationTokens (
        id INT IDENTITY(1,1) PRIMARY KEY,
        account_id INT NOT NULL,
        token VARCHAR(255) NOT NULL UNIQUE,
        expiry_date DATETIME NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_EmailVerificationTokens_Accounts 
            FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE
    );
    
    PRINT '✅ [5/6] Đã tạo bảng EmailVerificationTokens thành công';
    PRINT '   ℹ️  Cấu trúc: id, account_id, token, expiry_date, created_at';
END
ELSE
BEGIN
    PRINT '⚠️  [5/6] Bảng EmailVerificationTokens đã tồn tại, bỏ qua';
END
GO

-- ============================================================================
-- PHẦN 3: TẠO BẢNG PASSWORD RESET TOKENS
-- ============================================================================

PRINT '';
PRINT '--- PHẦN 3: Tạo bảng PasswordResetTokens ---';
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PasswordResetTokens')
BEGIN
    CREATE TABLE PasswordResetTokens (
        id INT IDENTITY(1,1) PRIMARY KEY,
        account_id INT NOT NULL,
        token VARCHAR(255) NOT NULL UNIQUE,
        expiry_date DATETIME NOT NULL,
        used BIT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_PasswordResetTokens_Accounts 
            FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE
    );
    
    PRINT '✅ [6/6] Đã tạo bảng PasswordResetTokens thành công';
    PRINT '   ℹ️  Cấu trúc: id, account_id, token, expiry_date, used, created_at';
END
ELSE
BEGIN
    PRINT '⚠️  [6/6] Bảng PasswordResetTokens đã tồn tại, bỏ qua';
END
GO

-- ============================================================================
-- PHẦN 4: TẠO INDEXES ĐỂ TĂNG HIỆU SUẤT QUERY
-- ============================================================================

PRINT '';
PRINT '--- PHẦN 4: Tạo Indexes để tối ưu hiệu suất ---';
GO

-- 4.1. Index cho EmailVerificationTokens
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_EmailVerificationTokens_Token')
BEGIN
    CREATE INDEX IX_EmailVerificationTokens_Token ON EmailVerificationTokens(token);
    PRINT '✅ Đã tạo index IX_EmailVerificationTokens_Token';
END
ELSE
BEGIN
    PRINT '⚠️  Index IX_EmailVerificationTokens_Token đã tồn tại';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_EmailVerificationTokens_AccountId')
BEGIN
    CREATE INDEX IX_EmailVerificationTokens_AccountId ON EmailVerificationTokens(account_id);
    PRINT '✅ Đã tạo index IX_EmailVerificationTokens_AccountId';
END
ELSE
BEGIN
    PRINT '⚠️  Index IX_EmailVerificationTokens_AccountId đã tồn tại';
END
GO

-- 4.2. Index cho PasswordResetTokens
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PasswordResetTokens_Token')
BEGIN
    CREATE INDEX IX_PasswordResetTokens_Token ON PasswordResetTokens(token);
    PRINT '✅ Đã tạo index IX_PasswordResetTokens_Token';
END
ELSE
BEGIN
    PRINT '⚠️  Index IX_PasswordResetTokens_Token đã tồn tại';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PasswordResetTokens_AccountId')
BEGIN
    CREATE INDEX IX_PasswordResetTokens_AccountId ON PasswordResetTokens(account_id);
    PRINT '✅ Đã tạo index IX_PasswordResetTokens_AccountId';
END
ELSE
BEGIN
    PRINT '⚠️  Index IX_PasswordResetTokens_AccountId đã tồn tại';
END
GO

-- 4.3. Index cho Accounts (Login Attempt Limiting)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Accounts_AccountLockedUntil')
BEGIN
    CREATE INDEX IX_Accounts_AccountLockedUntil ON Accounts(account_locked_until);
    PRINT '✅ Đã tạo index IX_Accounts_AccountLockedUntil';
END
ELSE
BEGIN
    PRINT '⚠️  Index IX_Accounts_AccountLockedUntil đã tồn tại';
END
GO

-- ============================================================================
-- PHẦN 5: THỐNG KÊ VÀ XÁC NHẬN
-- ============================================================================

PRINT '';
PRINT '========================================';
PRINT 'MIGRATION HOÀN TẤT THÀNH CÔNG! 🎉';
PRINT '========================================';
PRINT '';
PRINT '📊 THỐNG KÊ CẤU TRÚC DATABASE:';
PRINT '----------------------------';

-- Kiểm tra các fields đã thêm vào Accounts
DECLARE @emailVerified BIT = 0;
DECLARE @failedAttempts BIT = 0;
DECLARE @lockedUntil BIT = 0;
DECLARE @lastLogin BIT = 0;

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'email_verified')
    SET @emailVerified = 1;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'failed_login_attempts')
    SET @failedAttempts = 1;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'account_locked_until')
    SET @lockedUntil = 1;
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'last_login')
    SET @lastLogin = 1;

PRINT '✓ Accounts.email_verified: ' + CASE WHEN @emailVerified = 1 THEN 'OK' ELSE 'MISSING' END;
PRINT '✓ Accounts.failed_login_attempts: ' + CASE WHEN @failedAttempts = 1 THEN 'OK' ELSE 'MISSING' END;
PRINT '✓ Accounts.account_locked_until: ' + CASE WHEN @lockedUntil = 1 THEN 'OK' ELSE 'MISSING' END;
PRINT '✓ Accounts.last_login: ' + CASE WHEN @lastLogin = 1 THEN 'OK' ELSE 'MISSING' END;

-- Kiểm tra các bảng
DECLARE @emailTable BIT = 0;
DECLARE @passwordTable BIT = 0;

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'EmailVerificationTokens')
    SET @emailTable = 1;
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'PasswordResetTokens')
    SET @passwordTable = 1;

PRINT '✓ EmailVerificationTokens table: ' + CASE WHEN @emailTable = 1 THEN 'OK' ELSE 'MISSING' END;
PRINT '✓ PasswordResetTokens table: ' + CASE WHEN @passwordTable = 1 THEN 'OK' ELSE 'MISSING' END;

PRINT '';
PRINT '🔐 TÍNH NĂNG BẢO MẬT ĐÃ ĐƯỢC KÍCH HOẠT:';
PRINT '----------------------------';
PRINT '1. ✓ Email Verification - Xác thực email khi đăng ký';
PRINT '2. ✓ Password Reset - Quên mật khẩu qua email';
PRINT '3. ✓ Login Attempt Limiting - Chống brute-force (5 lần sai = khóa 15 phút)';
PRINT '';
PRINT '📝 LƯU Ý:';
PRINT '- Tất cả account hiện tại đã được đánh dấu email_verified = 1';
PRINT '- Các account mới sẽ cần xác thực email trước khi sử dụng';
PRINT '- Token reset password có thời hạn (thường 1 giờ)';
PRINT '- Token email verification có thời hạn (thường 24 giờ)';
PRINT '';
GO

/*
===========================================================================
   UPDATE ADMIN ACCOUNTS - Set email_verified = 1
   Date: 2026-01-29
   Description: Ensure all admin accounts can login without email verification
===========================================================================
*/

GO

-- Update all ADMIN accounts to have email_verified = 1 (optional, vì code đã bỏ qua check)
-- Nhưng tốt nhất vẫn nên set = 1 cho consistency
UPDATE Accounts
SET email_verified = 1
WHERE role_id = (SELECT id FROM Roles WHERE name = 'ADMIN');
GO

-- Verify the update
SELECT 
    a.id,
    a.username,
    a.email,
    r.name as role,
    a.is_active,
    a.email_verified,
    a.account_locked_until
FROM Accounts a
JOIN Roles r ON a.role_id = r.id
WHERE r.name = 'ADMIN';
GO

PRINT '✅ All ADMIN accounts updated successfully!';
PRINT '🔓 Admin accounts can now login without email verification.';
GO

/*
===========================================================================
   COMPLETE DATABASE MIGRATION FOR ADMIN FEATURES
   Date: 2026-01-29
   Description: Add ALL missing columns to Accounts table
===========================================================================
*/

GO

PRINT '🔧 Starting database migration...';
GO

-- Add account_locked_until column
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'Accounts') 
               AND name = 'account_locked_until')
BEGIN
    ALTER TABLE Accounts ADD account_locked_until DATETIME NULL;
    PRINT '✅ Added account_locked_until column';
END
ELSE
    PRINT 'ℹ️  account_locked_until already exists';
GO

-- Add email_verified column
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'Accounts') 
               AND name = 'email_verified')
BEGIN
    ALTER TABLE Accounts ADD email_verified BIT DEFAULT 0;
    PRINT '✅ Added email_verified column';
END
ELSE
    PRINT 'ℹ️  email_verified already exists';
GO

-- Add failed_login_attempts column
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'Accounts') 
               AND name = 'failed_login_attempts')
BEGIN
    ALTER TABLE Accounts ADD failed_login_attempts INT DEFAULT 0;
    PRINT '✅ Added failed_login_attempts column';
END
ELSE
    PRINT 'ℹ️  failed_login_attempts already exists';
GO

-- Add last_login column
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'Accounts') 
               AND name = 'last_login')
BEGIN
    ALTER TABLE Accounts ADD last_login DATETIME NULL;
    PRINT '✅ Added last_login column';
END
ELSE
    PRINT 'ℹ️  last_login already exists';
GO

-- Verify all columns exist
PRINT '';
PRINT '📋 Current Accounts table structure:';
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Accounts'
ORDER BY ORDINAL_POSITION;
GO

PRINT '';
PRINT '✅ Database migration completed successfully!';
PRINT '🚀 You can now restart your Spring Boot application.';
GO


UPDATE Accounts
SET email_verified = 1
WHERE role_id = (SELECT id FROM Roles WHERE name = 'ADMIN');
GO

-- Verify the update
SELECT 
    a.id,
    a.username,
    a.email,
    r.name as role,
    a.is_active,
    a.email_verified,
    a.account_locked_until
FROM Accounts a
JOIN Roles r ON a.role_id = r.id
WHERE r.name = 'ADMIN';
GO

PRINT '✅ All ADMIN accounts updated successfully!';
PRINT '🔓 Admin accounts can now login without email verification.';
GO

-- 12/3 --
-- Thêm cột media_url vào bảng ProductReviews (chạy 1 lần duy nhất)
ALTER TABLE ProductReviews ADD media_url NVARCHAR(2000) NULL;

go 

-- =============================================
-- Migration: Add QR Payment Tracking to Orders
-- Date: 2026-03-12
-- Description: Adds payment_status, payment_confirmed_at,
--              and payment_confirmed_by columns to Orders table
--              to support admin QR payment confirmation workflow.
-- =============================================

-- Step 1: Add payment_status column
-- Values: NOT_REQUIRED (COD), QR_PENDING, QR_CONFIRMED, QR_REJECTED
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Orders' AND COLUMN_NAME = 'payment_status'
)
BEGIN
    ALTER TABLE Orders ADD payment_status NVARCHAR(30) DEFAULT 'NOT_REQUIRED';
    PRINT 'Added column: payment_status';
END
GO

-- Step 2: Add payment_confirmed_at timestamp
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Orders' AND COLUMN_NAME = 'payment_confirmed_at'
)
BEGIN
    ALTER TABLE Orders ADD payment_confirmed_at DATETIME2;
    PRINT 'Added column: payment_confirmed_at';
END
GO

-- Step 3: Add payment_confirmed_by (admin username)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Orders' AND COLUMN_NAME = 'payment_confirmed_by'
)
BEGIN
    ALTER TABLE Orders ADD payment_confirmed_by NVARCHAR(100);
    PRINT 'Added column: payment_confirmed_by';
END
GO

-- Step 4: Backfill existing QR orders (if any) to QR_PENDING
UPDATE Orders 
SET payment_status = 'QR_PENDING' 
WHERE payment_method = 'QR' 
  AND payment_status = 'NOT_REQUIRED'
  AND status NOT IN ('COMPLETED', 'CANCELLED');
GO

-- Step 5: Set NOT_REQUIRED for all non-QR orders
UPDATE Orders 
SET payment_status = 'NOT_REQUIRED' 
WHERE payment_status IS NULL;
GO

PRINT 'Migration complete: QR Payment Tracking columns added to Orders table.';


go 

-- =============================================
-- THÊM TÀI KHOẢN NHÂN VIÊN BÁN HÀNG & KHO
-- Password mặc định: 123456 (đã BCrypt)
-- =============================================

-- BƯỚC 1: Đảm bảo đủ 4 Roles
IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'SALES')
    INSERT INTO Roles (name) VALUES ('SALES');

IF NOT EXISTS (SELECT 1 FROM Roles WHERE name = 'WAREHOUSE')
    INSERT INTO Roles (name) VALUES ('WAREHOUSE');

-- =============================================
-- BƯỚC 2: Thêm tài khoản Nhân viên Bán hàng
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Accounts WHERE email = 'sales@shopomg.com')
BEGIN
    INSERT INTO Accounts (
        username, password, full_name, email, phone,
        avatar, role_id, is_active, email_verified
    )
    VALUES (
        'sales',
        -- BCrypt của: Sales@123
        '$2a$10$Nn3d5cKDr7YiVyLdO4PpoOtf5gq2GSOXHmBnE.vBbzTIY7LkyA0DC',
        N'Nhân Viên Bán Hàng',
        'sales@shopomg.com',
        '0901000001',
        N'https://ui-avatars.com/api/?background=4CAF50&color=fff&name=Sales',
        (SELECT id FROM Roles WHERE name = 'SALES'),
        1, 1
    );
    PRINT '✅ Đã tạo tài khoản Nhân viên Bán hàng: sales@shopomg.com / 123456';
END
ELSE
    PRINT '⚠️ Tài khoản sales@shopomg.com đã tồn tại, bỏ qua.';

-- =============================================
-- BƯỚC 3: Thêm tài khoản Nhân viên Kho
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Accounts WHERE email = 'warehouse@shopomg.com')
BEGIN
    INSERT INTO Accounts (
        username, password, full_name, email, phone,
        avatar, role_id, is_active, email_verified
    )
    VALUES (
        'warehouse',
        -- BCrypt của: Warehouse@123
        '$2a$10$Nn3d5cKDr7YiVyLdO4PpoOtf5gq2GSOXHmBnE.vBbzTIY7LkyA0DC',
        N'Nhân Viên Kho',
        'warehouse@shopomg.com',
        '0901000002',
        N'https://ui-avatars.com/api/?background=2196F3&color=fff&name=Kho',
        (SELECT id FROM Roles WHERE name = 'WAREHOUSE'),
        1, 1
    );
    PRINT '✅ Đã tạo tài khoản Nhân viên Kho: warehouse@shopomg.com / 123456';
END
ELSE
    PRINT '⚠️ Tài khoản warehouse@shopomg.com đã tồn tại, bỏ qua.';

-- =============================================
-- KIỂM TRA KẾT QUẢ
-- =============================================
SELECT
    a.id,
    a.username,
    a.full_name,
    a.email,
    r.name AS role,
    a.is_active
FROM Accounts a
JOIN Roles r ON a.role_id = r.id
WHERE r.name IN ('SALES', 'WAREHOUSE', 'ADMIN')
ORDER BY r.name, a.id;

DELETE FROM Accounts WHERE id IN (9, 10);

SELECT password FROM Accounts WHERE username = 'admin';

go 

-- ============================================================
-- DỮ LIỆU MẪU ĐÁNH GIÁ SẢN PHẨM - SHOP OMG!
-- Mỗi sản phẩm 2-3 đánh giá, mix cao thấp
-- Account IDs: 1=admin, 2=khachhang, 3=nguyenlam
-- ============================================================

INSERT INTO ProductReviews (product_id, account_id, rating, comment, review_date) VALUES

-- ── SẢN PHẨM 1: Áo Polo Nam Thoáng Khí ──
(1, 2, 5, N'Vải cá sấu rất đẹp, mặc mát, thấm mồ hôi tốt. Rất hài lòng!', '2026-01-10 09:15:00'),
(1, 3, 4, N'Chất lượng ổn, size đúng như mô tả. Giao hàng nhanh.', '2026-01-15 14:30:00'),

-- ── SẢN PHẨM 2: Áo Thun Oversize Streetwear ──
(2, 2, 5, N'Form rộng đúng ý, mặc rất thoải mái. Màu đẹp, không phai.', '2026-01-12 10:00:00'),
(2, 3, 3, N'Vải hơi mỏng hơn mong đợi nhưng form ổn.', '2026-01-20 16:45:00'),

-- ── SẢN PHẨM 3: Áo Thun Ba Lỗ Thể Thao ──
(3, 2, 5, N'Mặc tập gym rất thoáng, co giãn tốt. Giá rẻ mà chất!', '2026-01-08 08:00:00'),
(3, 3, 4, N'Tốt cho giá tiền này. Sẽ mua thêm màu khác.', '2026-01-25 11:20:00'),
(3, 1, 2, N'Size hơi nhỏ, nên lấy to hơn 1 size.', '2026-02-01 09:00:00'),

-- ── SẢN PHẨM 4: Áo Thun Dài Tay Thu Đông ──
(4, 3, 5, N'Mặc se lạnh rất ấm, chất cotton dày dặn. Rất thích!', '2026-01-05 20:00:00'),
(4, 2, 4, N'Đẹp, ấm, giao hàng nhanh. Chỉ hơi khó giặt sạch.', '2026-01-18 13:00:00'),

-- ── SẢN PHẨM 5: Áo Croptop Nữ Cá Tính ──
(5, 2, 5, N'Mặc cùng quần jeans cực cute! Chất liệu mềm mại.', '2026-01-11 15:30:00'),
(5, 3, 4, N'Đẹp như hình, size vừa. Hơi ngắn một chút nhưng chấp nhận được.', '2026-01-22 17:00:00'),
(5, 1, 1, N'Màu không đúng như ảnh, nhận được màu nhạt hơn nhiều.', '2026-02-03 10:30:00'),

-- ── SẢN PHẨM 6: Áo Thun Kẻ Sọc Ngang ──
(6, 2, 4, N'Họa tiết đẹp, không nhòe màu sau khi giặt. Giao hàng cẩn thận.', '2026-01-14 09:45:00'),
(6, 3, 5, N'Mặc đi chơi cực đẹp, nhiều người hỏi mua ở đâu!', '2026-01-28 14:00:00'),

-- ── SẢN PHẨM 7: Áo Thun Raglan ──
(7, 3, 5, N'Phối màu tay áo độc đáo, mặc nổi bật. Chất cotton dày.', '2026-01-09 11:00:00'),
(7, 2, 3, N'Ổn thôi, không đặc biệt lắm nhưng đáng tiền.', '2026-01-30 16:00:00'),

-- ── SẢN PHẨM 8: Áo Sơ Mi Flannel Caro ──
(8, 2, 5, N'Vải mềm mại, họa tiết caro rất cổ điển. Mặc thu đông cực hợp!', '2026-01-06 10:30:00'),
(8, 3, 4, N'Chất lượng tốt, đường may sắc sảo. Giao hàng nhanh hơn dự kiến.', '2026-01-19 15:45:00'),
(8, 1, 2, N'Vải hơi cứng ban đầu, phải giặt nhiều lần mới mềm.', '2026-02-05 08:30:00'),

-- ── SẢN PHẨM 9: Áo Sơ Mi Trắng Công Sở ──
(9, 2, 5, N'Màu trắng tinh tế, chất liệu không nhăn. Mặc đi làm rất chuyên nghiệp!', '2026-01-07 09:00:00'),
(9, 3, 5, N'Shop đóng gói cẩn thận, áo không bị nhăn. Rất hài lòng!', '2026-01-21 14:15:00'),

-- ── SẢN PHẨM 10: Áo Sơ Mi Denim Bụi Bặm ──
(10, 3, 4, N'Chất bò mềm, mặc phong trần. Hơi nóng mùa hè.', '2026-01-13 11:30:00'),
(10, 2, 5, N'Rất thích kiểu dáng này! Mặc với quần đen là chuẩn.', '2026-01-26 16:30:00'),

-- ── SẢN PHẨM 11: Áo Sơ Mi Cộc Tay Mùa Hè ──
(11, 2, 4, N'Mát, thoáng, phù hợp mùa hè. Họa tiết đẹp mắt.', '2026-01-10 13:00:00'),
(11, 3, 3, N'Chất liệu ổn nhưng size hơi lớn hơn thông thường.', '2026-01-27 10:00:00'),

-- ── SẢN PHẨM 12: Áo Sơ Mi Linen Form Rộng ──
(12, 3, 5, N'Vải đũi rất nhẹ, mặc mùa hè cực mát. Màu đẹp tự nhiên.', '2026-01-08 16:00:00'),
(12, 2, 4, N'Đúng chất linen như mô tả. Cần là phẳng trước khi mặc.', '2026-01-24 12:30:00'),

-- ── SẢN PHẨM 13: Áo Sơ Mi Cổ Tàu ──
(13, 2, 5, N'Thiết kế độc đáo, mặc sự kiện rất nổi bật. Chất vải tốt.', '2026-01-11 10:45:00'),
(13, 3, 2, N'Cổ áo hơi cứng, khó chịu khi mặc lâu.', '2026-01-29 15:00:00'),

-- ── SẢN PHẨM 14: Áo Sơ Mi Voan Nơ Cổ ──
(14, 2, 5, N'Cực kỳ nữ tính và điệu đà! Mặc dự tiệc được luôn.', '2026-01-12 14:00:00'),
(14, 3, 4, N'Vải voan nhẹ, nhìn thanh lịch. Cần giặt tay.', '2026-01-23 11:00:00'),

-- ── SẢN PHẨM 15: Áo Sơ Mi Đen Slimfit ──
(15, 3, 5, N'Ôm dáng chuẩn, mặc tiệc tối rất sang. Đường may tỉ mỉ!', '2026-01-09 19:00:00'),
(15, 2, 4, N'Đẹp, lịch sự. Chỉ cần chú ý size vì ôm khá sát.', '2026-01-25 13:30:00'),
(15, 1, 3, N'Chất liệu bình thường, hơi nhăn sau khi giặt.', '2026-02-02 09:15:00'),

-- ── SẢN PHẨM 16: Quần Jeans Rách Gối ──
(16, 2, 5, N'Rách đúng vị trí, vải dày, không bị phai màu. Rất bụi!', '2026-01-10 15:00:00'),
(16, 3, 4, N'Mặc đi chơi cực ngầu. Giao hàng đúng hẹn.', '2026-01-20 10:30:00'),

-- ── SẢN PHẨM 17: Quần Skinny Jeans Nữ ──
(17, 2, 5, N'Ôm chân rất đẹp, tôn dáng cực tốt. Mặc với áo crop top là tuyệt!', '2026-01-07 11:00:00'),
(17, 3, 5, N'Chất jeans dày, co giãn tốt. Mặc nguyên ngày không mỏi.', '2026-01-22 16:00:00'),
(17, 1, 2, N'Màu xanh nhạt hơn ảnh, hơi thất vọng.', '2026-02-04 10:00:00'),

-- ── SẢN PHẨM 18: Quần Baggy Jeans ──
(18, 3, 5, N'Form rộng thoải mái, mặc cả ngày không khó chịu. Xu hướng cực hot!', '2026-01-08 14:30:00'),
(18, 2, 4, N'Đẹp, hợp mốt. Vải dày dặn, bền.', '2026-01-26 11:00:00'),

-- ── SẢN PHẨM 19: Quần Short Jeans Nam ──
(19, 2, 4, N'Mặc mùa hè rất mát, năng động. Đường may chắc chắn.', '2026-01-13 09:30:00'),
(19, 3, 5, N'Vừa vặn, mặc đi biển cực đẹp. Giá hợp lý!', '2026-01-28 15:30:00'),

-- ── SẢN PHẨM 20: Quần Short Jeans Nữ ──
(20, 2, 5, N'Cạp cao hack dáng cực tốt! Chất jeans mềm, không cứng.', '2026-01-09 10:00:00'),
(20, 3, 3, N'Ổn nhưng size hơi nhỏ, nên mua to hơn 1 size.', '2026-01-24 14:00:00'),

-- ── SẢN PHẨM 21: Quần Jeans Đen Trơn ──
(21, 3, 5, N'Màu đen đậm, không phai. Phối được với tất cả mọi thứ!', '2026-01-11 11:30:00'),
(21, 2, 4, N'Chất lượng tốt, form đẹp. Sẽ mua thêm.', '2026-01-27 16:00:00'),
(21, 1, 4, N'Bền màu sau nhiều lần giặt. Rất hài lòng.', '2026-02-03 13:00:00'),

-- ── SẢN PHẨM 22: Quần Jeans Trắng ──
(22, 2, 5, N'Trắng sáng, trẻ trung. Mặc mùa hè rất nổi!', '2026-01-06 15:00:00'),
(22, 3, 2, N'Vải hơi mỏng, dễ ố vàng. Cần chú ý khi giặt.', '2026-01-21 10:00:00'),

-- ── SẢN PHẨM 23: Quần Mom Jeans ──
(23, 3, 5, N'Dáng cổ điển nhưng rất trendy hiện tại. Mặc cực đẹp!', '2026-01-10 16:30:00'),
(23, 2, 4, N'Chất jeans tốt, form đứng dáng. Giao hàng nhanh.', '2026-01-25 12:00:00'),

-- ── SẢN PHẨM 24: Váy Maxi Đi Biển ──
(24, 2, 5, N'Họa tiết hoa rất đẹp, vải nhẹ bay trong gió. Mặc đi biển là chuẩn!', '2026-01-08 10:00:00'),
(24, 3, 4, N'Đẹp như hình, size vừa. Hơi dài với người thấp.', '2026-01-22 15:00:00'),

-- ── SẢN PHẨM 25: Đầm Bodycon Ôm Sát ──
(25, 2, 5, N'Tôn đường cong cực tốt! Mặc dạ tiệc được luôn. Rất thích!', '2026-01-07 19:30:00'),
(25, 3, 4, N'Đẹp, quyến rũ. Co giãn tốt nên mặc thoải mái.', '2026-01-20 14:00:00'),
(25, 1, 1, N'Chất liệu kém, bị nhăn ngay sau lần giặt đầu tiên.', '2026-02-01 11:00:00'),

-- ── SẢN PHẨM 26: Chân Váy Xếp Ly ──
(26, 3, 5, N'Phong cách Hàn Quốc rất xinh! Vải dày, không bị lộ trong.', '2026-01-09 11:30:00'),
(26, 2, 5, N'Mặc đi học, đi chơi đều đẹp. Rất hài lòng với sản phẩm!', '2026-01-23 10:30:00'),

-- ── SẢN PHẨM 27: Đầm Công Sở Chữ A ──
(27, 2, 5, N'Lịch sự, kín đáo, mặc đi làm rất chuyên nghiệp. Chất liệu cao cấp!', '2026-01-06 09:00:00'),
(27, 3, 3, N'Mẫu đẹp nhưng đường may hơi thô. Chấp nhận được.', '2026-01-19 16:00:00'),

-- ── SẢN PHẨM 28: Váy Hai Dây Lụa ──
(28, 3, 5, N'Vải lụa mềm mại, mặc mùa hè cực mát và gợi cảm!', '2026-01-11 15:00:00'),
(28, 2, 4, N'Đẹp, nhẹ nhàng. Cần mặc kèm áo khoác khi ra ngoài.', '2026-01-26 11:30:00'),

-- ── SẢN PHẨM 29: Chân Váy Jean Ngắn ──
(29, 2, 5, N'Năng động, phối với áo thun là tuyệt! Chất jeans đẹp.', '2026-01-10 14:00:00'),
(29, 3, 2, N'Size không chuẩn, nhỏ hơn thực tế. Phải đổi size.', '2026-01-28 09:30:00'),

-- ── SẢN PHẨM 30: Đầm Yếm Jean ──
(30, 3, 5, N'Cực cute và hack tuổi! Mặc mùa hè rất thoải mái.', '2026-01-08 16:30:00'),
(30, 2, 4, N'Đẹp, trendy. Chất jeans mềm, không cứng.', '2026-01-24 13:00:00'),

-- ── SẢN PHẨM 31: Váy Vintage Cổ Điển ──
(31, 2, 5, N'Phong cách retro rất đặc biệt, mặc chụp ảnh đẹp cực kỳ!', '2026-01-09 10:30:00'),
(31, 3, 4, N'Đúng phong cách vintage, vải tốt. Giao hàng cẩn thận.', '2026-01-25 15:30:00'),

-- ── SẢN PHẨM 32: Converse Chuck Taylor All Star ──
(32, 2, 5, N'Giày Converse chính hãng, chất lượng tuyệt vời! Đi cực êm.', '2026-01-07 10:00:00'),
(32, 3, 5, N'Màu đen basic phối được với mọi outfit. Rất hài lòng!', '2026-01-21 14:30:00'),
(32, 1, 4, N'Đẹp, bền. Hơi cứng ban đầu nhưng đi quen là êm.', '2026-02-02 10:00:00'),

-- ── SẢN PHẨM 33: Vans Old Skool ──
(33, 3, 5, N'Vans Old Skool classic! Đế bằng đi cực thoải mái, không mỏi chân.', '2026-01-08 11:00:00'),
(33, 2, 4, N'Giày đẹp, đúng hàng. Giao hàng nhanh, đóng gói cẩn thận.', '2026-01-22 16:30:00'),

-- ── SẢN PHẨM 34: New Balance 530 ──
(34, 2, 5, N'Dad shoes cực trendy! Êm chân, mặc với mọi outfit đều đẹp.', '2026-01-06 14:00:00'),
(34, 3, 5, N'Chất lượng xứng đáng với giá tiền. Rất thích thiết kế này!', '2026-01-20 11:00:00'),

-- ── SẢN PHẨM 35: MLB Chunky Liner ──
(35, 3, 5, N'Đế chunky cực hầm hố! Mặc với đồ streetwear là đỉnh.', '2026-01-09 15:30:00'),
(35, 2, 4, N'Đẹp, đứng form. Hơi nặng nhưng chấp nhận được.', '2026-01-24 10:00:00'),
(35, 1, 3, N'Giá hơi cao nhưng chất lượng ổn. Khó giặt sạch.', '2026-02-03 14:00:00'),

-- ── SẢN PHẨM 36: Giày Lười Slip-on ──
(36, 2, 5, N'Xỏ chân vào là đi, không cần buộc dây. Tiện lợi cực kỳ!', '2026-01-11 09:00:00'),
(36, 3, 4, N'Đi học, đi chơi đều ổn. Chất da nhân tạo khá tốt.', '2026-01-27 14:30:00'),

-- ── SẢN PHẨM 37: Giày Chạy Bộ Running ──
(37, 3, 5, N'Siêu nhẹ, đế đệm tốt. Chạy bộ mỗi sáng rất êm chân!', '2026-01-07 07:00:00'),
(37, 2, 5, N'Tốt nhất trong tầm giá! Chân không đau sau khi chạy dài.', '2026-01-21 08:30:00'),

-- ── SẢN PHẨM 38: Giày Sneaker High-top ──
(38, 2, 4, N'Cổ cao cá tính, mặc streetwear rất ăn. Đế êm.', '2026-01-10 16:00:00'),
(38, 3, 5, N'Thiết kế độc đáo, nổi bật khi ra đường. Rất thích!', '2026-01-25 11:30:00'),
(38, 1, 2, N'Cổ giày hơi cứng, khó xỏ vào. Cần thời gian để làm mềm.', '2026-02-04 09:00:00'),

-- ── SẢN PHẨM 39: Giày Thể Thao Nữ Hồng ──
(39, 2, 5, N'Màu hồng pastel cực dễ thương! Đi nhẹ, không mỏi chân.', '2026-01-09 14:00:00'),
(39, 3, 4, N'Giày đẹp, màu đúng như ảnh. Giao hàng nhanh.', '2026-01-23 16:00:00'),

-- ── SẢN PHẨM 40: Giày Chelsea Boot Nam ──
(40, 2, 5, N'Chất lượng sản phẩm tương đối tốt. Giao hàng nhanh đóng gói cẩn thận.', '2020-06-19 10:21:00'),
(40, 3, 5, N'Hàng giao nhanh chóng đúng mẫu mã như quảng cáo của nhà sản xuất.', '2026-01-04 20:36:00'),
(40, 1, 4, N'Giày đẹp lắm, chất liệu da tốt, đi êm chân. Rất hài lòng!', '2025-12-29 16:30:00'),

-- ── SẢN PHẨM 41: Giày Loafer Penny ──
(41, 3, 5, N'Da bóng sáng, mặc đi làm rất sang trọng. Không cần đánh xi mà vẫn đẹp!', '2026-01-08 10:00:00'),
(41, 2, 4, N'Giày da tốt, không dây tiện lợi. Đi quen là rất êm.', '2026-01-22 15:00:00'),

-- ── SẢN PHẨM 42: Giày Derby Da Lộn ──
(42, 2, 4, N'Phong cách bụi bặm đúng chất. Vải da lộn đẹp, không dễ bẩn.', '2026-01-11 11:00:00'),
(42, 3, 5, N'Giày chắc chắn, kiểu dáng độc. Nhiều người khen khi mặc!', '2026-01-26 14:30:00'),

-- ── SẢN PHẨM 43: Giày Monk Strap ──
(43, 3, 5, N'Khóa gài kép rất cổ điển và sang trọng! Đi tiệc, đi làm đều hợp.', '2026-01-07 09:30:00'),
(43, 2, 3, N'Giày đẹp nhưng hơi nặng. Đế cứng cần thời gian làm quen.', '2026-01-20 15:00:00'),

-- ── SẢN PHẨM 44: Giày Mọi Lái Xe ──
(44, 2, 5, N'Đế mềm êm tuyệt vời, lái xe cả ngày không mỏi chân!', '2026-01-09 10:30:00'),
(44, 3, 4, N'Thoải mái, nhẹ nhàng. Thiết kế đơn giản dễ phối đồ.', '2026-01-24 11:30:00'),

-- ── SẢN PHẨM 45: Giày Boot Da Nữ ──
(45, 2, 5, N'Gót nhọn tôn dáng cực tốt! Đứng cả tiệc không mỏi.', '2026-01-08 20:00:00'),
(45, 3, 4, N'Đẹp, sang trọng. Cần đi quen một chút với gót cao.', '2026-01-23 14:00:00'),
(45, 1, 2, N'Gót cao khó đi, không thoải mái cho người mới tập.', '2026-02-02 11:30:00'),

-- ── SẢN PHẨM 46: Giày Cao Gót Da Thật ──
(46, 3, 5, N'Da thật mềm mại, đi êm dù gót cao. Xứng đáng từng đồng!', '2026-01-06 15:30:00'),
(46, 2, 5, N'Chất da cao cấp, bền đẹp. Mặc đi sự kiện cực sang!', '2026-01-19 16:00:00'),

-- ── SẢN PHẨM 47: Sandal Da Nam ──
(47, 2, 4, N'Thoáng mát mùa hè, da mềm không trầy chân. Phối đồ dễ.', '2026-01-11 10:00:00'),
(47, 3, 5, N'Sandal da xịn, đi biển hay dạo phố đều ổn!', '2026-01-27 15:00:00'),

-- ── SẢN PHẨM 48: Giày Brogue Đục Lỗ ──
(48, 3, 5, N'Họa tiết đục lỗ tinh tế, nhìn rất sang trọng và cổ điển!', '2026-01-10 09:00:00'),
(48, 2, 3, N'Đẹp nhưng hơi khó vệ sinh phần đục lỗ. Chất da ổn.', '2026-01-25 13:00:00'),

-- ── SẢN PHẨM 49: Balo Da Laptop ──
(49, 2, 5, N'Đựng vừa laptop 15 inch kèm nhiều phụ kiện. Chất da đẹp, bền!', '2026-01-07 11:00:00'),
(49, 3, 4, N'Balo chắc chắn, nhiều ngăn tiện dụng. Dây đeo êm vai.', '2026-01-21 15:30:00'),

-- ── SẢN PHẨM 50: Túi Tote Vải Canvas ──
(50, 2, 5, N'Giá rẻ mà chất! Đựng sách, laptop đều ổn. Mua thêm cho bạn bè!', '2026-01-09 14:30:00'),
(50, 3, 4, N'Vải canvas dày, không bị rách. Màu đẹp, phối đồ dễ.', '2026-01-24 10:30:00'),
(50, 1, 3, N'Quai hơi mỏng, nặng quá thì khó chịu. Nhưng giá rẻ nên thôi.', '2026-02-03 09:30:00'),

-- ── SẢN PHẨM 51: Ví Da Nam Cầm Tay ──
(51, 3, 5, N'Ví da sang trọng, nhiều ngăn đựng thẻ tiện lợi. Tặng bố rất hợp!', '2026-01-08 10:00:00'),
(51, 2, 4, N'Chất da tốt, đường may đẹp. Giá hợp lý cho chất lượng này.', '2026-01-22 14:00:00'),

-- ── SẢN PHẨM 52: Ví Ngắn Nữ Mini ──
(52, 2, 5, N'Nhỏ gọn xinh xắn! Đủ ngăn đựng thẻ và tiền. Màu sắc cute!', '2026-01-11 15:00:00'),
(52, 3, 4, N'Đẹp, đúng như hình. Chất da tổng hợp khá tốt.', '2026-01-26 11:00:00'),

-- ── SẢN PHẨM 53: Túi Messenger Đeo Chéo ──
(53, 3, 5, N'Phong cách đưa thư cực cool! Đựng đủ đồ đi học cả ngày.', '2026-01-10 09:30:00'),
(53, 2, 4, N'Túi chắc chắn, khóa kéo trơn tru. Giao hàng nhanh.', '2026-01-25 14:00:00'),

-- ── SẢN PHẨM 54: Túi Du Lịch Cỡ Lớn ──
(54, 2, 5, N'Túi lớn đựng được rất nhiều đồ, dây đeo chắc. Đi du lịch tiện lợi!', '2026-01-06 16:00:00'),
(54, 3, 3, N'Ổn cho giá tiền nhưng không có bánh xe. Hơi nặng khi đầy đồ.', '2026-01-20 10:00:00'),

-- ── SẢN PHẨM 55: Túi Đeo Hông Bao Tử ──
(55, 3, 5, N'Trendy cực kỳ! Đeo đi festival, dạo phố cực phong cách.', '2026-01-09 11:00:00'),
(55, 2, 4, N'Nhỏ gọn tiện dụng. Chất liệu bền, khóa kéo tốt.', '2026-01-23 15:30:00'),

-- ── SẢN PHẨM 56: Cặp Da Công Sở ──
(56, 2, 5, N'Cặp da sang trọng, chuyên nghiệp. Đựng laptop và tài liệu thoải mái!', '2026-01-07 09:00:00'),
(56, 3, 4, N'Chất da đẹp, nhiều ngăn tiện lợi. Hơi nặng khi đầy đồ.', '2026-01-21 14:00:00'),
(56, 1, 2, N'Khóa hơi khó đóng mở. Chất da không bằng ảnh mô tả.', '2026-02-02 10:30:00'),

-- ── SẢN PHẨM 57: Túi Satchel Cổ Điển ──
(57, 3, 5, N'Kiểu hộp cứng rất sang, đựng đồ mà không bị biến dạng. Thích lắm!', '2026-01-10 16:00:00'),
(57, 2, 4, N'Đẹp, cổ điển. Chất da tổng hợp nhưng trông rất giống da thật.', '2026-01-25 11:00:00'),

-- ── SẢN PHẨM 58: Thắt Lưng Da Bò ──
(58, 2, 5, N'Da bò thật, rất bền và đẹp. Khóa kim loại chắc chắn!', '2026-01-08 11:30:00'),
(58, 3, 4, N'Chất da tốt, không bị bong tróc. Tặng bạn trai rất hợp.', '2026-01-22 15:30:00'),

-- ── SẢN PHẨM 59: Mũ Lưỡi Trai NY ──
(59, 3, 5, N'Logo NY thêu sắc nét, vải dày. Che nắng tốt, thời trang!', '2026-01-09 10:00:00'),
(59, 2, 4, N'Mũ đẹp, size vừa đầu. Phối được với nhiều outfit.', '2026-01-23 14:30:00'),

-- ── SẢN PHẨM 60: Mũ Bucket Vành Tròn ──
(60, 2, 5, N'Phong cách đường phố cực cool! Mặc mùa hè che nắng tốt.', '2026-01-11 09:30:00'),
(60, 3, 3, N'Đẹp nhưng vành hơi nhỏ, che nắng không nhiều lắm.', '2026-01-26 13:00:00'),

-- ── SẢN PHẨM 61: Kính Mát Thời Trang ──
(61, 3, 5, N'Chống UV tốt, tròng kính không bị trầy. Kiểu dáng rất thời trang!', '2026-01-07 14:00:00'),
(61, 2, 4, N'Đeo nhẹ thoải mái, không bị đau tai. Màu đẹp, nhiều mẫu.', '2026-01-20 16:30:00'),

-- ── SẢN PHẨM 62: Đồng Hồ Dây Da ──
(62, 2, 5, N'Máy Quartz Nhật Bản chạy chính xác. Dây da mềm, đẹp. Xứng giá!', '2026-01-06 11:00:00'),
(62, 3, 5, N'Đồng hồ sang trọng, tặng bạn trai rất ý nghĩa. Đóng gói đẹp!', '2026-01-19 15:00:00'),
(62, 1, 4, N'Chạy chính xác, mặt kính chống trầy tốt. Rất hài lòng.', '2026-02-01 13:00:00'),

-- ── SẢN PHẨM 63: Set 3 Đôi Tất Cổ Cao ──
(63, 3, 5, N'3 đôi tất chất lượng tốt, cotton thấm hút mồ hôi. Giá rẻ bất ngờ!', '2026-01-10 10:00:00'),
(63, 2, 4, N'Mềm, thoáng, không bị hôi chân. Sẽ mua thêm.', '2026-01-25 14:30:00'),

-- ── SẢN PHẨM 64: Khăn Choàng Cổ Len ──
(64, 2, 5, N'Len mềm mịn, quàng vào cổ rất ấm. Màu đẹp, tặng người yêu được!', '2026-01-08 16:00:00'),
(64, 3, 4, N'Ấm áp, nhẹ nhàng. Không bị ngứa như len thường.', '2026-01-22 11:00:00'),

-- ── SẢN PHẨM 65: Cà Vạt Lụa Cao Cấp ──
(65, 3, 5, N'Lụa mềm mại, màu sắc sang trọng. Thắt cà vạt rất đẹp với vest!', '2026-01-09 09:30:00'),
(65, 2, 4, N'Chất lụa tốt, không bị nhăn. Giá hợp lý cho chất này.', '2026-01-24 14:00:00'),

-- ── SẢN PHẨM 66: Vòng Tay Bạc ──
(66, 2, 5, N'Bạc 925 sáng bóng, không bị đen sau thời gian dài. Tặng mình cực đẹp!', '2026-01-07 15:30:00'),
(66, 3, 5, N'Chất lượng bạc tốt, thiết kế tinh tế. Mua làm quà rất ý nghĩa!', '2026-01-21 10:30:00'),

-- ── SẢN PHẨM 67: Nơ Cài Áo Vest ──
(67, 3, 4, N'Điểm nhấn đẹp cho bộ vest. Cài chắc, không bị tuột.', '2026-01-10 11:00:00'),
(67, 2, 5, N'Nhỏ nhắn mà tạo điểm nhấn tuyệt vời! Mua thêm màu khác.', '2026-01-25 15:00:00'),

-- ── SẢN PHẨM 68: Áo Hoodie Basic ──
(68, 2, 5, N'Nỉ bông dày, mặc mùa đông rất ấm. Mũ hoodie đứng form cực đẹp!', '2026-01-06 10:00:00'),
(68, 3, 5, N'Basic nhưng không chán, phối đồ cực dễ. Mua 2 màu luôn!', '2026-01-20 15:30:00'),
(68, 1, 4, N'Chất nỉ tốt, không xù lông sau giặt. Rất hài lòng.', '2026-02-02 09:00:00'),

-- ── SẢN PHẨM 69: Áo Sweater Trơn ──
(69, 3, 4, N'Vải da cá mềm mại, dễ phối đồ. Mặc mùa thu rất hợp.', '2026-01-09 14:00:00'),
(69, 2, 5, N'Màu đẹp, chất tốt. Mặc với quần jeans là chuẩn outfit!', '2026-01-24 11:30:00'),

-- ── SẢN PHẨM 70: Áo Khoác Bomber ──
(70, 2, 5, N'Phong cách phi công rất ngầu! Vải dày, mặc thu đông ấm áp.', '2026-01-08 15:00:00'),
(70, 3, 4, N'Đẹp, nhiều túi tiện dụng. Khóa kéo trơn tru.', '2026-01-22 10:00:00'),

-- ── SẢN PHẨM 71: Áo Khoác Denim ──
(71, 3, 5, N'Denim bền đẹp, phong cách bụi bặm cực chất. Mặc hoài không chán!', '2026-01-10 09:00:00'),
(71, 2, 3, N'Vải hơi cứng ban đầu, mặc vài lần mới mềm ra.', '2026-01-26 14:00:00'),

-- ── SẢN PHẨM 72: Áo Blazer Hàn Quốc ──
(72, 2, 5, N'Khoác blazer Hàn Quốc cực lịch sự! Mặc đi học, đi làm đều đẹp.', '2026-01-07 10:30:00'),
(72, 3, 4, N'Form đẹp, đứng dáng. Chất vải tốt, không nhăn.', '2026-01-21 15:00:00'),
(72, 1, 2, N'Size không chuẩn với người to vai, phải đổi size lớn hơn.', '2026-02-03 11:00:00'),

-- ── SẢN PHẨM 73: Áo Vest Công Sở ──
(73, 3, 5, N'Form vest chuẩn, đứng dáng tuyệt vời. Mặc phỏng vấn cực tự tin!', '2026-01-08 09:00:00'),
(73, 2, 5, N'Chất vải cao cấp, đường may đẹp. Xứng đáng với giá tiền!', '2026-01-22 14:00:00'),

-- ── SẢN PHẨM 74: Áo Cardigan Len ──
(74, 2, 5, N'Len mềm mịn, mặc mùa thu rất nhẹ nhàng và nữ tính!', '2026-01-09 16:00:00'),
(74, 3, 4, N'Đẹp, ấm vừa phải. Phối được với nhiều loại quần.', '2026-01-24 12:30:00'),

-- ── SẢN PHẨM 75: Áo Khoác Da Biker ──
(75, 3, 5, N'Da PU cao cấp, cứng cáp ngầu! Mặc đi xe máy cực chất.', '2026-01-07 15:00:00'),
(75, 2, 4, N'Đẹp, dày dặn. Hơi nặng nhưng bù lại rất ấm.', '2026-01-20 11:30:00'),
(75, 1, 3, N'Da PU chứ không phải da thật nhưng trông khá giống. Chấp nhận được.', '2026-02-01 14:00:00'),

-- ── SẢN PHẨM 76: Áo Măng Tô Dạ ──
(76, 2, 5, N'Dài qua gối, vải dạ dày rất ấm. Mặc mùa đông cực sang trọng!', '2026-01-06 16:30:00'),
(76, 3, 5, N'Đẳng cấp! Màu đẹp, form chuẩn. Tặng mẹ nhân dịp Tết.', '2026-01-19 10:00:00');

go
ALTER TABLE Vouchers ADD 
    is_flash_sale BIT DEFAULT 0, 
    flash_sale_start_hour INT, 
    flash_sale_end_hour INT;


-- Thêm 2 cột lưu thông tin giao dịch SePay thực tế
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Orders') AND name = 'transfer_content')
    ALTER TABLE Orders ADD transfer_content NVARCHAR(500) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Orders') AND name = 'reference_code')
    ALTER TABLE Orders ADD reference_code NVARCHAR(100) NULL;
