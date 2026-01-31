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
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Áo Polo Nam Thoáng Khí', 'ao-polo-nam', 320000, 5, N'Nam', 1, N'Vải cá sấu cotton, thấm hút tốt.', N'https://dongphucsaigon.vn/wp-content/uploads/2024/06/ca-sau-cotton-100-5.jpg'),
(N'Áo Thun Oversize Streetwear', 'ao-thun-oversize', 280000, 0, N'Unisex', 1, N'Form rộng thoải mái, phong cách trẻ trung.', N'https://down-vn.img.susercontent.com/file/sg-11134201-22120-tqp6klf7cykv08'),
(N'Áo Thun Ba Lỗ Thể Thao', 'ao-ba-lo-the-thao', 120000, 0, N'Nam', 1, N'Mặc đi tập gym hoặc mặc nhà.', N'https://bizweb.dktcdn.net/100/469/063/products/ao-the-thao-nam-playwell-72311.jpg?v=1766052497440'),
(N'Áo Thun Dài Tay Thu Đông', 'ao-thun-dai-tay', 220000, 10, N'Unisex', 1, N'Giữ ấm nhẹ, thích hợp thời tiết se lạnh.', N'https://img.lazcdn.com/g/p/d3d78099cd2ae4bad2b2bada716f246f.jpg_960x960q80.jpg_.webp'),
(N'Áo Croptop Nữ Cá Tính', 'ao-croptop-nu', 180000, 0, N'Nữ', 1, N'Khoe eo thon, năng động.', N'https://product.hstatic.net/200000476257/product/upload_dcb733b385ba485ba1ddf3ce62f3e07b_master.jpg'),
(N'Áo Thun Kẻ Sọc Ngang', 'ao-thun-ke-soc', 190000, 0, N'Unisex', 1, N'Họa tiết kẻ sọc không lỗi mốt.', N'https://down-vn.img.susercontent.com/file/vn-11134208-7r98o-lmqbjaiaxgbj0e'),
(N'Áo Thun Raglan', 'ao-thun-raglan', 210000, 5, N'Nam', 1, N'Phối màu tay áo độc đáo.', N'https://s3.ap-southeast-1.amazonaws.com/thegmen.vn/2025/10/17596773417212ax6p.jpg');

-- 2. ÁO SƠ MI (Category 2) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Áo Sơ Mi Flannel Caro', 'so-mi-flannel', 350000, 0, N'Nam', 2, N'Họa tiết caro cổ điển, vải dạ mỏng.', N'https://5sfashion.vn/storage/upload/images/posts/4kxZ8LCxsK6CO3rY0rZhV5XAdplrJbXzzHsD9hHn.jpg'),
(N'Áo Sơ Mi Trắng Công Sở', 'so-mi-trang-cong-so', 400000, 10, N'Nữ', 2, N'Thanh lịch, chuyên nghiệp.', N'https://dongphucphuquy.com/wp-content/uploads/2025/09/dong-phuc-cong-so-pq01.jpg'),
(N'Áo Sơ Mi Denim Bụi Bặm', 'so-mi-denim', 480000, 0, N'Nam', 2, N'Chất bò mềm, phong cách nam tính.', N'https://rustico.vn/wp-content/uploads/2023/12/ao-somi-nam-kaki-tui-hop-scaled.jpg'),
(N'Áo Sơ Mi Cộc Tay Mùa Hè', 'so-mi-coc-tay', 250000, 5, N'Nam', 2, N'Thoáng mát, họa tiết nhiệt đới.', N'https://topcomshop.com/uploads/images/a-ao2019/4/11248127832-686905192.jpg'),
(N'Áo Sơ Mi Linen Form Rộng', 'so-mi-linen', 380000, 0, N'Nữ', 2, N'Vải đũi tự nhiên, nhẹ nhàng.', N'https://kamaka.vn/cdn/shop/files/3_597a4bab-b0e9-49f8-9979-ee2fc78b1531_1400x.jpg?v=1692606001'),
(N'Áo Sơ Mi Cổ Tàu', 'so-mi-co-tau', 320000, 0, N'Nam', 2, N'Thiết kế cổ trụ lạ mắt.', N'https://2885371169.e.cdneverest.net/catalog/product/8/t/8th23s002-sl126-2.webp'),
(N'Áo Sơ Mi Voan Nơ Cổ', 'so-mi-voan-no', 290000, 0, N'Nữ', 2, N'Nữ tính, điệu đà.', N'https://cdn.kkfashion.vn/15514-home_default/ao-so-mi-nu-coc-tay-co-that-no-asm10-21.jpg'),
(N'Áo Sơ Mi Đen Slimfit', 'so-mi-den-slimfit', 420000, 0, N'Nam', 2, N'Ôm dáng, sang trọng cho tiệc tối.', N'https://down-vn.img.susercontent.com/file/b1e45d9f3db852e9b6491f1bcaad703d');

-- 3. JEANS (Category 3) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Quần Jeans Rách Gối', 'jeans-rach-goi', 550000, 0, N'Nam', 3, N'Phong cách bụi bặm, đường phố.', N'https://4men.com.vn/thumbs/2019/12/quan-jean-rach-goi-qj1674-mau-xam-xanh-17230-p.jpg'),
(N'Quần Skinny Jeans Nữ', 'skinny-jeans-nu', 450000, 10, N'Nữ', 3, N'Ôm sát, tôn đôi chân dài.', N'https://product.hstatic.net/1000402464/product/fwjn22ss09c_blue__1__9a951be7d0f7423aab2f359cc8de51d1_master.jpg'),
(N'Quần Baggy Jeans', 'baggy-jeans', 420000, 5, N'Unisex', 3, N'Rộng rãi, thoải mái vận động.', N'https://zizoou.com/cdn/shop/products/Quan-Baggy-Jean-nam-nu-2b-1-Quan-ong-rong-xanh-classic-ZiZoou-Store_4472x.jpg?v=1680283265'),
(N'Quần Short Jeans Nam', 'short-jeans-nam', 320000, 0, N'Nam', 3, N'Ngắn ngang gối, năng động.', N'https://bizweb.dktcdn.net/thumb/grande/100/396/594/products/ruber-5.jpg?v=1712135435903'),
(N'Quần Short Jeans Nữ', 'short-jeans-nu', 280000, 0, N'Nữ', 3, N'Cạp cao, hack dáng.', N'https://onoff.vn/blog/wp-content/uploads/2019/03/Quan-short-Jeans-danh-cho-n%C6%B0.jpg'),
(N'Quần Jeans Đen Trơn', 'jeans-den-tron', 500000, 0, N'Nam', 3, N'Dễ phối đồ, màu đen bền màu.', N'https://4men.com.vn/images/thumbs/2015/08/quan-jean-skinny-den-qj1238-4868-slide-1.jpg'),
(N'Quần Jeans Trắng', 'jeans-trang', 520000, 15, N'Nữ', 3, N'Trẻ trung, nổi bật.', N'https://img.alicdn.com/imgextra/i2/1064800342/TB2l2R2o9BYBeNjy0FeXXbnmFXa_!!1064800342.jpg_400x400.jpg_.webp'),
(N'Quần Mom Jeans', 'mom-jeans', 490000, 0, N'Nữ', 3, N'Dáng cổ điển thập niên 90.', N'https://images2.thanhnien.vn/Uploaded/yennh/2022_05_03/image010-5902.jpg');

-- 4. VÁY ĐẦM (Category 4) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Váy Maxi Đi Biển', 'vay-maxi', 350000, 0, N'Nữ', 4, N'Dài thướt tha, họa tiết hoa.', N'https://vaydibiendep.com/wp-content/uploads/2019/07/dammaxidibien-lm85.jpg'),
(N'Đầm Bodycon Ôm Sát', 'dam-bodycon', 400000, 0, N'Nữ', 4, N'Quyến rũ, tôn đường cong.', N'https://cdn.kkfashion.vn/10516-large_default/dam-thun-om-body-sat-nach-day-rut-hong-hl17-25.jpg'),
(N'Chân Váy Xếp Ly', 'chan-vay-xep-ly', 250000, 5, N'Nữ', 4, N'Dài qua gối, phong cách Hàn Quốc.', N'https://product.hstatic.net/200000041406/product/e321ffe6-c908-4609-b3e8-cad1caf1b8f3_3f3098436bfb47e0b5dbc7f6363ea2ac_grande.png'),
(N'Đầm Công Sở Chữ A', 'dam-cong-so', 450000, 10, N'Nữ', 4, N'Lịch sự, kín đáo.', N'https://cdn.kkfashion.vn/23348-large_default/dam-cong-so-dang-chu-a-co-dan-tong-kk140-40.jpg'),
(N'Váy Hai Dây Lụa', 'vay-hai-day', 300000, 0, N'Nữ', 4, N'Mát mẻ, gợi cảm.', N'https://down-vn.img.susercontent.com/file/vn-11134207-7r98o-lsshrnbywukk99'),
(N'Chân Váy Jean Ngắn', 'chan-vay-jean', 220000, 0, N'Nữ', 4, N'Năng động, dễ phối áo thun.', N'https://pos.nvncdn.com/b153ea-53436/ps/20240515_Ov1xljEmoK.jpeg?v=1715766735'),
(N'Đầm Yếm Jean', 'dam-yem', 380000, 0, N'Nữ', 4, N'Cute, hack tuổi.', N'https://sakurafashion.vn/upload/sanpham/large/32512-vay-yem-nu-dang-dai-thu-dong-1.jpg'),
(N'Váy Vintage Cổ Điển', 'vay-vintage', 420000, 0, N'Nữ', 4, N'Phong cách retro nhẹ nhàng.', N'https://m.yodycdn.com/blog/vay-vintage-yody10.jpg');

-- 5. GIÀY SNEAKER (Category 5) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Converse Chuck Taylor All Star', 'converse-classic', 1500000, 0, N'Unisex', 5, N'Cổ cao, màu đen basic.', N'https://bizweb.dktcdn.net/100/407/286/products/m9160-d-08x1-jpg-sw-406.jpg?v=1685685359293'),
(N'Vans Old Skool', 'vans-old-skool', 1800000, 5, N'Unisex', 5, N'Đế bằng, trượt ván.', N'https://cdn.storims.com/api/v2/image/resize?path=https://storage.googleapis.com/storims_cdn/storims/uploads/47ab565da4d2c902dead0bd308949a32.jpeg&format=jpeg'),
(N'New Balance 530', 'new-balance-530', 2800000, 0, N'Unisex', 5, N'Phong cách Dad shoes cổ điển.', N'https://images.asos-media.com/products/new-balance-530-trainers-in-white-and-grey/204470705-1-white?$n_640w$&wid=513&fit=constrain'),
(N'MLB Chunky Liner', 'mlb-chunky', 3200000, 10, N'Unisex', 5, N'Đế cao, hầm hố.', N'https://product.hstatic.net/200000642007/product/50bks_3asxlmb3n_8_ac36c45964a94a96926af1c4afacb4ab_b5e0e970acd445139a4988a1804e61f4.jpg'),
(N'Giày Lười Slip-on', 'giay-slip-on', 500000, 0, N'Nam', 5, N'Không dây, tiện lợi.', N'https://cdn.dafc.com.vn/catalog/product/dafc/1190175_001_673ee56e8e2c0.jpg'),
(N'Giày Chạy Bộ Running', 'giay-chay-bo', 900000, 20, N'Nam', 5, N'Siêu nhẹ, êm chân.', N'https://supersports.com.vn/cdn/shop/files/DR2615-102-9_1024x1024.jpg?v=1768456774'),
(N'Giày Sneaker High-top', 'sneaker-high-top', 1200000, 0, N'Nam', 5, N'Cổ cao cá tính.', N'https://drake.vn/image/catalog/H%C3%ACnh%20content/Converse%20high%20top/converse-high-top-6.jpg'),
(N'Giày Thể Thao Nữ Hồng', 'sneaker-nu-hong', 850000, 0, N'Nữ', 5, N'Màu hồng pastel dễ thương.', N'https://bizweb.dktcdn.net/100/301/479/products/giay-sneaker-nu-hottrend-2021-co-thap-cao-cap-10.jpg?v=1635999955777');

-- 6. GIÀY DA (Category 6) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Giày Chelsea Boot Nam', 'chelsea-boot', 1500000, 0, N'Nam', 6, N'Cổ lửng, chun co giãn.', N'https://keedo.vn/wp-content/uploads/2021/01/Tay-Den-Chelsea-Boot-Zip-7-Square.png'),
(N'Giày Loafer Penny', 'loafer-penny', 1300000, 5, N'Nam', 6, N'Da bóng, không dây sang trọng.', N'https://madshoes.vn/wp-content/uploads/2022/06/giay-da-nam-penny-loafer.jpg'),
(N'Giày Derby Da Lộn', 'derby-da-lon', 1100000, 0, N'Nam', 6, N'Phong cách bụi bặm.', N'https://madshoes.vn/wp-content/uploads/2022/09/giay-oxford-nam-da-lon-1-scaled.jpg'),
(N'Giày Monk Strap', 'monk-strap', 1600000, 0, N'Nam', 6, N'Khóa gài kép cổ điển.', N'https://timan.vn/upload/products/112023/giay-nam-monk-strap-gt77-sang-trong.jpg'),
(N'Giày Mọi Lái Xe', 'driving-shoes', 950000, 10, N'Nam', 6, N'Đế mềm, thoải mái khi lái xe.', N'https://topcomshop.com/uploads/images/a-day5/3925309422-1620862535.jpg'),
(N'Giày Boot Da Nữ', 'boot-da-nu', 1200000, 0, N'Nữ', 6, N'Gót nhọn, tôn dáng.', N'https://tiemgiayboot.vn/thumbs/1080x1080x2/upload/product/1-5150.png'),
(N'Giày Cao Gót Da Thật', 'cao-got-da', 1400000, 0, N'Nữ', 6, N'Da mềm, đi êm chân.', N'https://giaydaneo.com/wp-content/uploads/2024/04/Giay-cao-got-da-that-cao-5cm-2.jpg'),
(N'Sandal Da Nam', 'sandal-da', 650000, 0, N'Nam', 6, N'Thoáng mát mùa hè.', N'https://www.gento.vn/wp-content/uploads/2023/05/dep-sandal-nam-hang-hieu-10.jpg'),
(N'Giày Brogue Đục Lỗ', 'brogue-shoes', 1350000, 0, N'Nam', 6, N'Họa tiết đục lỗ tinh tế.', N'https://fttleather.com/uploads/1026/news/2020/07/12/wingtip-oxford-1594517419.jpg');

-- 7. TÚI XÁCH (Category 7) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Balo Da Laptop', 'balo-da', 850000, 0, N'Nam', 7, N'Đựng vừa laptop 15 inch.', N'https://balotot.com/wp-content/uploads/2021/05/Balo-Da-BD81107.jpg'),
(N'Túi Tote Vải Canvas', 'tui-tote', 150000, 0, N'Unisex', 7, N'Đựng tài liệu, đi học.', N'https://bizweb.dktcdn.net/100/466/874/products/0517ss4698-lon-jpeg-1749009124947.jpg?v=1749009152277'),
(N'Ví Da Nam Cầm Tay', 'vi-da-cam-tay', 550000, 0, N'Nam', 7, N'Sang trọng, nhiều ngăn.', N'https://timan.vn/upload/products/122023/vi-da-nam-t743-tre-trung.jpg'),
(N'Ví Ngắn Nữ Mini', 'vi-nu-mini', 250000, 0, N'Nữ', 7, N'Nhỏ gọn, dễ thương.', N'https://pub-b30d4c98c76a47fcb3455c5fbb3ee778.r2.dev/2023/11/VI-NGAN-NU-MINI-NHIEU-HINH-11.jpg'),
(N'Túi Messenger Đeo Chéo', 'tui-messenger', 450000, 10, N'Nam', 7, N'Phong cách đưa thư.', N'https://zizoou.com/cdn/shop/products/Tui-deo-cheo-nam-nu-34-16-1-ZiZoou-Store_4472x.jpg?v=1630542338'),
(N'Túi Du Lịch Cỡ Lớn', 'tui-du-lich', 600000, 0, N'Unisex', 7, N'Thích hợp đi chơi xa.', N'https://mailinhmart.com/wp-content/uploads/2024/06/tui-xach-du-lich-co-lon-2.jpg'),
(N'Túi Đeo Hông Bao Tử', 'tui-deo-hong', 320000, 0, N'Unisex', 7, N'Thời trang, tiện lợi.', N'https://down-vn.img.susercontent.com/file/4e65fb45d2ac3935ab76e815217228bf'),
(N'Cặp Da Công Sở', 'cap-da-cong-so', 1200000, 5, N'Nam', 7, N'Đựng tài liệu, laptop.', N'https://www.gento.vn/wp-content/uploads/2021/04/cap-da-cong-so-cao-cap-G206-8.jpg'),
(N'Túi Satchel Cổ Điển', 'tui-satchel', 700000, 0, N'Nữ', 7, N'Kiểu dáng hộp cứng cáp.', N'https://cdn.vuahanghieu.com/unsafe/0x900/left/top/smart/filters:quality(90)/https://admin.vuahanghieu.com/upload/product/2024/08/tui-deo-cheo-nu-coach-satchel-crossbody-in-signature-colorblock-cv704-mau-trang-nau-66b5d65f730a5-09082024154207.jpg');

-- 8. PHỤ KIỆN (Category 8)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Thắt Lưng Da Bò', 'that-lung-da', 350000, 0, N'Nam', 8, N'Da thật, khóa kim loại.', N'https://product.hstatic.net/200000366789/product/dsc00720_15a73c2ebd0746b4af7cdbd60f2f8320.png'),
(N'Mũ Lưỡi Trai NY', 'mu-luoi-trai', 250000, 0, N'Unisex', 8, N'Che nắng, thời trang.', N'https://down-vn.img.susercontent.com/file/a5128515bd9b206192d2dc5eb30d8e4d'),
(N'Mũ Bucket Vành Tròn', 'mu-bucket', 180000, 0, N'Unisex', 8, N'Phong cách đường phố.', N'https://zerdio.com.vn/wp-content/uploads/2021/04/mu-rong-vanh-nam-1.jpg'),
(N'Kính Mát Thời Trang', 'kinh-mat', 450000, 10, N'Unisex', 8, N'Chống tia UV.', N'https://matkinhlb.com.vn/wp-content/uploads/2022/09/upload_0d328f6b2487401ebb13a6acedf33cac_master-1.jpg'),
(N'Đồng Hồ Dây Da', 'dong-ho-da', 1500000, 20, N'Nam', 8, N'Máy Quartz Nhật Bản.', N'https://product.hstatic.net/1000104930/product/dsc07129_2ca25b2fc14d4e51918b87c748d5374a.jpg'),
(N'Set 3 Đôi Tất Cổ Cao', 'tat-co-cao', 99000, 0, N'Unisex', 8, N'Cotton thấm hút mồ hôi.', N'https://cdn.vuahanghieu.com/unsafe/0x900/left/top/smart/filters:quality(90)/https://admin.vuahanghieu.com/upload/product/2025/07/set-3-doi-tat-adidas-co-cao-lot-dem-3-soc-ip2639-mau-xanh-xam-size-m-6870b32583492-11072025134557.jpg'),
(N'Khăn Choàng Cổ Len', 'khan-choang', 220000, 0, N'Nữ', 8, N'Giữ ấm mùa đông.', N'https://sakurafashion.vn/upload/sanpham/large/393170-khan-choang-len-nu-mau-tron-6.jpg'),
(N'Cà Vạt Lụa Cao Cấp', 'ca-vat', 150000, 0, N'Nam', 8, N'Phụ kiện vest lịch lãm.', N'https://cavat.com/wp-content/uploads/2017/01/cavat-den-8cm.jpg'),
(N'Vòng Tay Bạc', 'vong-tay', 550000, 0, N'Nữ', 8, N'Bạc 925 sáng bóng.', N'https://bactrangsuc.vn/sqb/images/vong-dong-tinh-khiet/l%E1%BA%AFc%20tay/z4701551018009_204cf8fafd166dddac0a513707860b70.jpg'),
(N'Nơ Cài Áo Vest', 'no-cai-ao', 120000, 0, N'Nam', 8, N'Điểm nhấn cho bộ vest.', N'https://vn-test-11.slatic.net/p/1024bb16e61403f07f5de416b91c0f8d.jpg');

-- 9. ÁO KHOÁC (Category 9) 
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Áo Hoodie Basic', 'ao-hoodie', 380000, 0, N'Unisex', 9, N'Có mũ, nỉ bông ấm áp.', N'https://product.hstatic.net/200000370449/product/hoodie_basic_logo_den_truoc_91873a48f86a4afab810f5389611483b_master.png'),
(N'Áo Sweater Trơn', 'ao-sweater', 350000, 5, N'Unisex', 9, N'Dễ phối đồ, vải da cá.', N'https://product.hstatic.net/1000308345/product/img_6130_a3548967fb57478f840201837c8e3127_master.jpg'),
(N'Áo Khoác Bomber', 'bomber-jacket', 550000, 0, N'Nam', 9, N'Phong cách phi công.', N'https://bizweb.dktcdn.net/thumb/1024x1024/100/399/392/products/ao-khoac-nam-bomber-basic-hiddle-4.jpg?v=1743844172117'),
(N'Áo Khoác Denim', 'denim-jacket', 600000, 0, N'Unisex', 9, N'Bụi bặm, bền bỉ.', N'https://images.unsplash.com/photo-1611312449408-fcece27cdbb7?auto=format&fit=crop&w=600&q=80'),
(N'Áo Blazer Hàn Quốc', 'ao-blazer', 750000, 10, N'Nam', 9, N'Khoác nhẹ, lịch sự.', N'https://down-vn.img.susercontent.com/file/vn-11134207-820l4-mht69nicd0jl63'),
(N'Áo Vest Công Sở', 'ao-vest', 1200000, 0, N'Nam', 9, N'Form chuẩn, đứng dáng.', N'https://vestdep.net/thumb/1000-0/upload/vestdep/2023/VEST/end_356%20(1).jpg'),
(N'Áo Cardigan Len', 'ao-cardigan', 320000, 0, N'Nữ', 9, N'Khoác nhẹ mùa thu.', N'https://sakurafashion.vn/upload/sanpham/large/1782360964-ao-khoac-len-cardigan-nu-5.jpg'),
(N'Áo Khoác Da Biker', 'ao-khoac-da', 1500000, 0, N'Nam', 9, N'Da PU cao cấp, ngầu.', N'https://product.hstatic.net/1000369857/product/ao_da_avn04_1200x1200_h8_746da8840b7c4319b4a540cf8aa65ef8.jpg'),
(N'Áo Măng Tô Dạ', 'ao-mang-to', 1800000, 15, N'Nữ', 9, N'Dài qua gối, sang trọng.', N'https://sakurafashion.vn/upload/sanpham/large/93982--3.jpg');

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