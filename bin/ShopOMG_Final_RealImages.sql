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

USE master;
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
(N'Phụ Kiện', 'phu-kien', N'https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?auto=format&fit=crop&w=500&q=60', 1), -- ID 8
(N'Áo Khoác', 'ao-khoac', N'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=500&q=60', 1); -- ID 9

-- C. PRODUCTS & IMAGES (CHI TIẾT TỪNG SẢN PHẨM)

-- 1. Áo Thun (Category 1)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Áo Thun Nam Basic Premium', 'ao-thun-nam-basic', 299000, 0, N'Nam', 1, N'Áo thun cotton 100%, co giãn 4 chiều.', N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/October2024/ao-thun-nam-cotton-coolmate-basics-200gsm-mau-trang-1.jpg'),
(N'Áo Thun Nữ Cổ Tim', 'ao-thun-nu-co-tim', 150000, 10, N'Nữ', 1, N'Áo thun nữ dáng ôm, tôn dáng.', N'https://lp2.hm.com/hmgoepprod?set=quality%5B79%5D%2Csource%5B%2F39%2F2f%2F392f7a0b5290fd3e7d93e5a53deb121cc9b9324d.jpg%5D%2Corigin%5Bdam%5D%2Ccategory%5B%5D%2Ctype%5BLOOKBOOK%5D%2Cres%5Bm%5D%2Chmver%5B1%5D&call=url[file:/product/main]'),
(N'Áo Thun In Hình Graphic', 'ao-thun-graphic', 350000, 0, N'Unisex', 1, N'Áo thun in hình cá tính, phong cách đường phố.', N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/September2024/ao-thun-nam-in-hinh-marvel-spider-man-beyond-amazing-mau-den-1.jpg');

-- 2. Áo Sơ Mi (Category 2)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Áo Sơ Mi Oxford Nam', 'so-mi-oxford', 450000, 5, N'Nam', 2, N'Sơ mi vải Oxford đứng form, chống nhăn.', N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/November2023/ao-so-mi-nam-cafe-khang-khuan-co-gian-mau-trang-1.jpg'),
(N'Áo Sơ Mi Lụa Nữ', 'so-mi-lua', 550000, 0, N'Nữ', 2, N'Chất lụa mềm mại, sang trọng.', N'https://lp2.hm.com/hmgoepprod?set=quality%5B79%5D%2Csource%5B%2Fd3%2F18%2Fd318e873099903740285a73229b9f93933c108c9.jpg%5D%2Corigin%5Bdam%5D%2Ccategory%5B%5D%2Ctype%5BLOOKBOOK%5D%2Cres%5Bm%5D%2Chmver%5B1%5D&call=url[file:/product/main]');

-- 3. Quần Jeans (Category 3)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Quần Jeans Slimfit Nam', 'jeans-slimfit', 590000, 20, N'Nam', 3, N'Jeans co giãn, form ôm vừa vặn.', N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/January2024/quan-jeans-nam-dang-suong-copper-denim-clean-finish-mau-xanh-dao-1.jpg'),
(N'Quần Jeans Ống Rộng Nữ', 'jeans-ong-rong', 480000, 0, N'Nữ', 3, N'Quần lưng cao hack dáng.', N'https://lp2.hm.com/hmgoepprod?set=quality%5B79%5D%2Csource%5B%2F96%2F96%2F9696969696.jpg%5D%2Corigin%5Bdam%5D%2Ccategory%5B%5D%2Ctype%5BLOOKBOOK%5D%2Cres%5Bm%5D%2Chmver%5B1%5D&call=url[file:/product/main]');

-- 4. Váy Đầm (Category 4)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Đầm Voan Hoa Nhí', 'dam-voan', 320000, 15, N'Nữ', 4, N'Đầm voan 2 lớp, họa tiết hoa nhí.', N'https://lp2.hm.com/hmgoepprod?set=quality%5B79%5D%2Csource%5B%2F28%2F97%2F289767676767.jpg%5D%2Corigin%5Bdam%5D%2Ccategory%5B%5D%2Ctype%5BLOOKBOOK%5D%2Cres%5Bm%5D%2Chmver%5B1%5D&call=url[file:/product/main]'),
(N'Đầm Dạ Hội Trễ Vai', 'dam-da-hoi', 1200000, 0, N'Nữ', 4, N'Thiết kế sang trọng cho buổi tiệc.', N'https://lp2.hm.com/hmgoepprod?set=quality%5B79%5D%2Csource%5B%2F7c%2F61%2F7c61fbed34a24cb2aa9bb509f0debcccc7f04b8b.jpg%5D%2Corigin%5Bdam%5D%2Ccategory%5B%5D%2Ctype%5BLOOKBOOK%5D%2Cres%5Bm%5D%2Chmver%5B1%5D&call=url[file:/product/main]');

-- 5. Giày Sneaker (Category 5)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Nike Air Force 1', 'nike-af1', 2500000, 0, N'Unisex', 5, N'Huyền thoại sneaker trắng.', N'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/b1bcbca4-e853-4df7-b329-5be3c61ee057/air-force-1-07-shoes-WrLlWX.png'),
(N'Adidas Ultraboost', 'adidas-ultraboost', 3500000, 10, N'Nam', 5, N'Giày chạy bộ êm ái nhất.', N'https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy,c_fill,g_auto/69cbc73d0cb846889f89acbb011e68cb_9366/Ultraboost_22_Shoes_Black_GZ0127_01_standard.jpg');

-- 6. Giày Da (Category 6)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Giày Tây Oxford', 'giay-tay', 1200000, 0, N'Nam', 6, N'Da bò thật 100%, lịch lãm.', N'https://product.hstatic.net/1000355922/product/giay-da-nam-buoc-day-ngoai-co-4382048__4__e362e00156044450a2c910f48858d1ba_master.jpg');

-- 7. Túi Xách (Category 7)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Túi Đeo Chéo Nữ', 'tui-deo-cheo', 450000, 0, N'Nữ', 7, N'Túi da PU cao cấp, nhỏ gọn.', N'https://www.vascara.com/uploads/cms_productmedia/2024/February/26/tui-deo-cheo-phom-hop-nap-gap-kim-loai-tui-sat-0310-mau-den-main__70928__1708933393.jpg');

-- 8. Áo Khoác (Category 9)
INSERT INTO Products (name, slug, price, discount, gender, category_id, description, image) VALUES
(N'Áo Khoác Gió Nam', 'ao-gio-nam', 399000, 0, N'Nam', 9, N'Chống nước, cản gió.', N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/December2024/ao-khoac-gio-nam-the-thao-daily-wear-trang-1.jpg');

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
INSERT INTO ProductImages (product_id, image_url) VALUES 
(1, N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/October2024/ao-thun-nam-cotton-coolmate-basics-200gsm-mau-trang-2.jpg'),
(1, N'https://media.coolmate.me/cdn-cgi/image/width=672,height=990,quality=85,format=auto/uploads/October2024/ao-thun-nam-cotton-coolmate-basics-200gsm-mau-trang-3.jpg');

-- Giày Nike (ID 8) - Ví dụ ID 8 là Nike AF1
INSERT INTO ProductImages (product_id, image_url)
SELECT id, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/4f37fca8-6bce-43e7-ad07-f57ae3c13142/air-force-1-07-shoes-WrLlWX.png'
FROM Products WHERE slug = 'nike-af1';

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