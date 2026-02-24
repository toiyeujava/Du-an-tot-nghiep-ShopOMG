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
    CONSTRAINT FK_Orders_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id)
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
    CONSTRAINT FK_Reviews_Products FOREIGN KEY (product_id) REFERENCES Products(id),
    CONSTRAINT FK_Reviews_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id)
);
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

-- C. PRODUCTS & IMAGES (CHI TIẾT TỪNG SẢN PHẨM)

-- 1. ÁO THUN (Category 1) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Áo Polo Nam Thoáng Khí', 'ao-polo-nam', 320000, 5, N'Nam', 1, N'Vải cá sấu cotton, thấm hút tốt.'),
(N'Áo Thun Oversize Streetwear', 'ao-thun-oversize', 280000, 0, N'Unisex', 1, N'Form rộng thoải mái, phong cách trẻ trung.'),
(N'Áo Thun Ba Lỗ Thể Thao', 'ao-ba-lo-the-thao', 120000, 0, N'Nam', 1, N'Mặc đi tập gym hoặc mặc nhà.'),
(N'Áo Thun Dài Tay Thu Đông', 'ao-thun-dai-tay', 220000, 10, N'Unisex', 1, N'Giữ ấm nhẹ, thích hợp thời tiết se lạnh.'),
(N'Áo Croptop Nữ Cá Tính', 'ao-croptop-nu', 180000, 0, N'Nữ', 1, N'Khoe eo thon, năng động.'),
(N'Áo Thun Kẻ Sọc Ngang', 'ao-thun-ke-soc', 190000, 0, N'Unisex', 1, N'Họa tiết kẻ sọc không lỗi mốt.'),
(N'Áo Thun Raglan', 'ao-thun-raglan', 210000, 5, N'Nam', 1, N'Phối màu tay áo độc đáo.');

-- 2. ÁO SƠ MI (Category 2) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Áo Sơ Mi Flannel Caro', 'so-mi-flannel', 350000, 0, N'Nam', 2, N'Họa tiết caro cổ điển, vải dạ mỏng.'),
(N'Áo Sơ Mi Trắng Công Sở', 'so-mi-trang-cong-so', 400000, 10, N'Nữ', 2, N'Thanh lịch, chuyên nghiệp.'),
(N'Áo Sơ Mi Denim Bụi Bặm', 'so-mi-denim', 480000, 0, N'Nam', 2, N'Chất bò mềm, phong cách nam tính.'),
(N'Áo Sơ Mi Cộc Tay Mùa Hè', 'so-mi-coc-tay', 250000, 5, N'Nam', 2, N'Thoáng mát, họa tiết nhiệt đới.'),
(N'Áo Sơ Mi Linen Form Rộng', 'so-mi-linen', 380000, 0, N'Nữ', 2, N'Vải đũi tự nhiên, nhẹ nhàng.'),
(N'Áo Sơ Mi Cổ Tàu', 'so-mi-co-tau', 320000, 0, N'Nam', 2, N'Thiết kế cổ trụ lạ mắt.'),
(N'Áo Sơ Mi Voan Nơ Cổ', 'so-mi-voan-no', 290000, 0, N'Nữ', 2, N'Nữ tính, điệu đà.'),
(N'Áo Sơ Mi Đen Slimfit', 'so-mi-den-slimfit', 420000, 0, N'Nam', 2, N'Ôm dáng, sang trọng cho tiệc tối.');

-- 3. JEANS (Category 3) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Quần Jeans Rách Gối', 'jeans-rach-goi', 550000, 0, N'Nam', 3, N'Phong cách bụi bặm, đường phố.'),
(N'Quần Skinny Jeans Nữ', 'skinny-jeans-nu', 450000, 10, N'Nữ', 3, N'Ôm sát, tôn đôi chân dài.'),
(N'Quần Baggy Jeans', 'baggy-jeans', 420000, 5, N'Unisex', 3, N'Rộng rãi, thoải mái vận động.'),
(N'Quần Short Jeans Nam', 'short-jeans-nam', 320000, 0, N'Nam', 3, N'Ngắn ngang gối, năng động.'),
(N'Quần Short Jeans Nữ', 'short-jeans-nu', 280000, 0, N'Nữ', 3, N'Cạp cao, hack dáng.'),
(N'Quần Jeans Đen Trơn', 'jeans-den-tron', 500000, 0, N'Nam', 3, N'Dễ phối đồ, màu đen bền màu.'),
(N'Quần Jeans Trắng', 'jeans-trang', 520000, 15, N'Nữ', 3, N'Trẻ trung, nổi bật.'),
(N'Quần Mom Jeans', 'mom-jeans', 490000, 0, N'Nữ', 3, N'Dáng cổ điển thập niên 90.');

-- 4. VÁY ĐẦM (Category 4) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Váy Maxi Đi Biển', 'vay-maxi', 350000, 0, N'Nữ', 4, N'Dài thướt tha, họa tiết hoa.'),
(N'Đầm Bodycon Ôm Sát', 'dam-bodycon', 400000, 0, N'Nữ', 4, N'Quyến rũ, tôn đường cong.'),
(N'Chân Váy Xếp Ly', 'chan-vay-xep-ly', 250000, 5, N'Nữ', 4, N'Dài qua gối, phong cách Hàn Quốc.'),
(N'Đầm Công Sở Chữ A', 'dam-cong-so', 450000, 10, N'Nữ', 4, N'Lịch sự, kín đáo.'),
(N'Váy Hai Dây Lụa', 'vay-hai-day', 300000, 0, N'Nữ', 4, N'Mát mẻ, gợi cảm.'),
(N'Chân Váy Jean Ngắn', 'chan-vay-jean', 220000, 0, N'Nữ', 4, N'Năng động, dễ phối áo thun.'),
(N'Đầm Yếm Jean', 'dam-yem', 380000, 0, N'Nữ', 4, N'Cute, hack tuổi.'),
(N'Váy Vintage Cổ Điển', 'vay-vintage', 420000, 0, N'Nữ', 4, N'Phong cách retro nhẹ nhàng.');

-- 5. GIÀY SNEAKER (Category 5) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Converse Chuck Taylor All Star', 'converse-classic', 1500000, 0, N'Unisex', 5, N'Cổ cao, màu đen basic.'),
(N'Vans Old Skool', 'vans-old-skool', 1800000, 5, N'Unisex', 5, N'Đế bằng, trượt ván.'),
(N'New Balance 530', 'new-balance-530', 2800000, 0, N'Unisex', 5, N'Phong cách Dad shoes cổ điển.'),
(N'MLB Chunky Liner', 'mlb-chunky', 3200000, 10, N'Unisex', 5, N'Đế cao, hầm hố.'),
(N'Giày Lười Slip-on', 'giay-slip-on', 500000, 0, N'Nam', 5, N'Không dây, tiện lợi.'),
(N'Giày Chạy Bộ Running', 'giay-chay-bo', 900000, 20, N'Nam', 5, N'Siêu nhẹ, êm chân.'),
(N'Giày Sneaker High-top', 'sneaker-high-top', 1200000, 0, N'Nam', 5, N'Cổ cao cá tính.'),
(N'Giày Thể Thao Nữ Hồng', 'sneaker-nu-hong', 850000, 0, N'Nữ', 5, N'Màu hồng pastel dễ thương.');

-- 6. GIÀY DA (Category 6) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Giày Chelsea Boot Nam', 'chelsea-boot', 1500000, 0, N'Nam', 6, N'Cổ lửng, chun co giãn.'),
(N'Giày Loafer Penny', 'loafer-penny', 1300000, 5, N'Nam', 6, N'Da bóng, không dây sang trọng.'),
(N'Giày Derby Da Lộn', 'derby-da-lon', 1100000, 0, N'Nam', 6, N'Phong cách bụi bặm.'),
(N'Giày Monk Strap', 'monk-strap', 1600000, 0, N'Nam', 6, N'Khóa gài kép cổ điển.'),
(N'Giày Mọi Lái Xe', 'driving-shoes', 950000, 10, N'Nam', 6, N'Đế mềm, thoải mái khi lái xe.'),
(N'Giày Boot Da Nữ', 'boot-da-nu', 1200000, 0, N'Nữ', 6, N'Gót nhọn, tôn dáng.'),
(N'Giày Cao Gót Da Thật', 'cao-got-da', 1400000, 0, N'Nữ', 6, N'Da mềm, đi êm chân.'),
(N'Sandal Da Nam', 'sandal-da', 650000, 0, N'Nam', 6, N'Thoáng mát mùa hè.'),
(N'Giày Brogue Đục Lỗ', 'brogue-shoes', 1350000, 0, N'Nam', 6, N'Họa tiết đục lỗ tinh tế.');

-- 7. TÚI XÁCH (Category 7) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Balo Da Laptop', 'balo-da', 850000, 0, N'Nam', 7, N'Đựng vừa laptop 15 inch.'),
(N'Túi Tote Vải Canvas', 'tui-tote', 150000, 0, N'Unisex', 7, N'Đựng tài liệu, đi học.'),
(N'Ví Da Nam Cầm Tay', 'vi-da-cam-tay', 550000, 0, N'Nam', 7, N'Sang trọng, nhiều ngăn.'),
(N'Ví Ngắn Nữ Mini', 'vi-nu-mini', 250000, 0, N'Nữ', 7, N'Nhỏ gọn, dễ thương.'),
(N'Túi Messenger Đeo Chéo', 'tui-messenger', 450000, 10, N'Nam', 7, N'Phong cách đưa thư.'),
(N'Túi Du Lịch Cỡ Lớn', 'tui-du-lich', 600000, 0, N'Unisex', 7, N'Thích hợp đi chơi xa.'),
(N'Túi Đeo Hông Bao Tử', 'tui-deo-hong', 320000, 0, N'Unisex', 7, N'Thời trang, tiện lợi.'),
(N'Cặp Da Công Sở', 'cap-da-cong-so', 1200000, 5, N'Nam', 7, N'Đựng tài liệu, laptop.'),
(N'Túi Satchel Cổ Điển', 'tui-satchel', 700000, 0, N'Nữ', 7, N'Kiểu dáng hộp cứng cáp.');

-- 8. PHỤ KIỆN (Category 8)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Thắt Lưng Da Bò', 'that-lung-da', 350000, 0, N'Nam', 8, N'Da thật, khóa kim loại.'),
(N'Mũ Lưỡi Trai NY', 'mu-luoi-trai', 250000, 0, N'Unisex', 8, N'Che nắng, thời trang.'),
(N'Mũ Bucket Vành Tròn', 'mu-bucket', 180000, 0, N'Unisex', 8, N'Phong cách đường phố.'),
(N'Kính Mát Thời Trang', 'kinh-mat', 450000, 10, N'Unisex', 8, N'Chống tia UV.'),
(N'Đồng Hồ Dây Da', 'dong-ho-da', 1500000, 20, N'Nam', 8, N'Máy Quartz Nhật Bản.'),
(N'Set 3 Đôi Tất Cổ Cao', 'tat-co-cao', 99000, 0, N'Unisex', 8, N'Cotton thấm hút mồ hôi.'),
(N'Khăn Choàng Cổ Len', 'khan-choang', 220000, 0, N'Nữ', 8, N'Giữ ấm mùa đông.'),
(N'Cà Vạt Lụa Cao Cấp', 'ca-vat', 150000, 0, N'Nam', 8, N'Phụ kiện vest lịch lãm.'),
(N'Vòng Tay Bạc', 'vong-tay', 550000, 0, N'Nữ', 8, N'Bạc 925 sáng bóng.'),
(N'Nơ Cài Áo Vest', 'no-cai-ao', 120000, 0, N'Nam', 8, N'Điểm nhấn cho bộ vest.');

-- 9. ÁO KHOÁC (Category 9) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
(N'Áo Hoodie Basic', 'ao-hoodie', 380000, 0, N'Unisex', 9, N'Có mũ, nỉ bông ấm áp.'),
(N'Áo Sweater Trơn', 'ao-sweater', 350000, 5, N'Unisex', 9, N'Dễ phối đồ, vải da cá.'),
(N'Áo Khoác Bomber', 'bomber-jacket', 550000, 0, N'Nam', 9, N'Phong cách phi công.'),
(N'Áo Khoác Denim', 'denim-jacket', 600000, 0, N'Unisex', 9, N'Bụi bặm, bền bỉ.'),
(N'Áo Blazer Hàn Quốc', 'ao-blazer', 750000, 10, N'Nam', 9, N'Khoác nhẹ, lịch sự.'),
(N'Áo Vest Công Sở', 'ao-vest', 1200000, 0, N'Nam', 9, N'Form chuẩn, đứng dáng.'),
(N'Áo Cardigan Len', 'ao-cardigan', 320000, 0, N'Nữ', 9, N'Khoác nhẹ mùa thu.'),
(N'Áo Khoác Da Biker', 'ao-khoac-da', 1500000, 0, N'Nam', 9, N'Da PU cao cấp, ngầu.'),
(N'Áo Măng Tô Dạ', 'ao-mang-to', 1800000, 15, N'Nữ', 9, N'Dài qua gối, sang trọng.');

-- 10. THÊM ĐỂ ĐỦ 100 PRODUCTS 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description) VALUES
-- 1. ÁO THUN (Category 1) 
(N'Áo Thun Polo Basic', 'ao-thun-polo-basic', 250000, 0, N'Nam', 1, N'Chất vải cotton 100%, co giãn 4 chiều.'),
(N'Áo Thun Graphic Tee', 'ao-thun-graphic-tee', 220000, 5, N'Unisex', 1, N'In hình họa tiết cá tính, bền màu.'),
(N'Áo Thun Tay Lỡ Form Rộng', 'ao-thun-tay-lo', 195000, 0, N'Unisex', 1, N'Phong cách Hàn Quốc, vải dày dặn.'),
-- 2. ÁO SƠ MI (Category 2) 
(N'Áo Sơ Mi Oxford Nam', 'so-mi-oxford-nam', 450000, 10, N'Nam', 2, N'Vải Oxford cao cấp, ít nhăn.'),
(N'Áo Sơ Mi Kiểu Nữ Voan', 'so-mi-voan-nu', 320000, 0, N'Nữ', 2, N'Thiết kế điệu đà, thoáng mát.'),
(N'Áo Sơ Mi Kaki Túi Hộp', 'so-mi-kaki-tui-hop', 380000, 5, N'Nam', 2, N'Phong cách quân đội, mạnh mẽ.'),
-- 3. JEANS (Category 3) 
(N'Quần Jeans Ống Suông Nam', 'jeans-ong-suong-nam', 580000, 0, N'Nam', 3, N'Form suông thoải mái, thời trang.'),
(N'Quần Shorts Jean Rách Nữ', 'short-jean-rach-nu', 290000, 0, N'Nữ', 3, N'Năng động, cá tính mùa hè.'),
(N'Quần Jeans Slim Fit Xám', 'jeans-slim-fit-xam', 520000, 15, N'Nam', 3, N'Màu xám khói hiện đại, tôn dáng.'),
-- 4. VÁY ĐẦM (Category 4) 
(N'Đầm Suông Chữ A Tối Giản', 'dam-suong-chu-a', 360000, 0, N'Nữ', 4, N'Thiết kế đơn giản, dễ mặc.'),
(N'Chân Váy Jean Dáng Dài', 'chan-vay-jean-dai', 310000, 5, N'Nữ', 4, N'Phối cúc giữa, phong cách retro.'),
(N'Đầm Dự Tiệc Trễ Vai', 'dam-du-tiec-tre-vai', 550000, 10, N'Nữ', 4, N'Quyến rũ, chất liệu lụa satin.'),
-- 5. GIÀY SNEAKER (Category 5) 
(N'Giày Sneaker Canvas Trắng', 'sneaker-canvas-trang', 450000, 0, N'Unisex', 5, N'Chất vải canvas bền bỉ, dễ phối đồ.'),
(N'Giày Sneaker Da Lộn Retro', 'sneaker-da-lon-retro', 1250000, 10, N'Unisex', 5, N'Phong cách vintage, đế cao su êm.'),
(N'Giày Sneaker Đế Bánh Mì Nữ', 'sneaker-banh-mi-nu', 850000, 0, N'Nữ', 5, N'Hack chiều cao, màu trắng basic.'),
-- 6. GIÀY DA (Category 6) 
(N'Giày Oxford Classic Nam', 'giay-oxford-classic', 1850000, 0, N'Nam', 6, N'Da thật nguyên tấm, sang trọng.'),
(N'Giày Mules Da Nữ', 'giay-mules-nu', 650000, 5, N'Nữ', 6, N'Hở gót tiện lợi, da mềm mại.'),
-- 7. TÚI XÁCH (Category 7) 
(N'Balo Thời Trang Mini Nữ', 'balo-mini-nu', 420000, 0, N'Nữ', 7, N'Nhỏ gọn, đựng vừa máy tính bảng.'),
(N'Túi Đeo Chéo Canvas Unisex', 'tui-deo-cheo-canvas', 280000, 5, N'Unisex', 7, N'Nhiều ngăn tiện lợi, đi học đi chơi.'),
-- 8. PHỤ KIỆN (Category 8)
(N'Kính Mát Phi Công', 'kinh-mat-phi-cong', 350000, 0, N'Unisex', 8, N'Chống tia UV400, gọng kim loại.'),
(N'Thắt Lưng Vải Canvas', 'that-lung-vai-canvas', 120000, 0, N'Nam', 8, N'Khóa d-ring, phong cách trẻ trung.'),
-- 9. ÁO KHOÁC (Category 9) 
(N'Áo Khoác Gió Chống Nước', 'ao-khoac-gio-chong-nuoc', 450000, 10, N'Unisex', 9, N'Vải dù chống thấm, lót lưới thoáng khí.'),
(N'Áo Khoác Jean Oversize', 'ao-khoac-jean-oversize', 620000, 0, N'Unisex', 9, N'Chất bò dày, form rộng thoải mái.'),
(N'Áo Khoác Dù 2 Lớp', 'ao-khoac-du-2-lop', 390000, 5, N'Nam', 9, N'Chống nắng và gió hiệu quả.');

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
