/*
===========================================================================
   MIGRATION: Thêm bảng Nhà Cung Cấp & Phiếu Nhập Kho
   DATE: 17/03/2026
   DESCRIPTION:
   - Tạo bảng Suppliers (Nhà cung cấp)
   - Tạo bảng InventoryReceipts (Phiếu nhập kho)
   - Tạo bảng InventoryReceiptDetails (Chi tiết phiếu nhập)
   - Chèn dữ liệu mẫu cho nhà cung cấp (CÔNG TY TNHH MEO LEO)
   
   HƯỚNG DẪN: Chạy script này trên database ShopOMG đã tồn tại.
   Nếu bạn đã chạy ShopOMG_DATABASE_TONG.sql mới nhất thì KHÔNG CẦN chạy file này.
===========================================================================
*/

USE ShopOMG;
GO

-- =============================================
-- 1. Bảng Nhà cung cấp (Suppliers)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Suppliers')
BEGIN
    CREATE TABLE Suppliers (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NOT NULL,
        phone VARCHAR(20),
        email VARCHAR(100),
        address NVARCHAR(500),
        tax_code VARCHAR(50),
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
    PRINT N'✅ Đã tạo bảng Suppliers thành công.';
END
ELSE
    PRINT N'⚠️ Bảng Suppliers đã tồn tại, bỏ qua.';
GO

-- =============================================
-- 2. Thêm dữ liệu nhà cung cấp từ hình ảnh
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Suppliers WHERE tax_code = '10309979486')
BEGIN
    INSERT INTO Suppliers (name, phone, email, address, tax_code, is_active)
    VALUES (
        N'CÔNG TY TNHH MEO LEO', 
        '0309979486', 
        'mleo123@gmail.com', 
        N'436 Võ Văn Tần, Quận 3, TP.HCM', 
        '10309979486', 
        1
    );
    PRINT N'✅ Đã chèn dữ liệu nhà cung cấp MEO LEO.';
END
ELSE
    PRINT N'⚠️ Dữ liệu nhà cung cấp này đã tồn tại, không chèn thêm.';
GO

-- =============================================
-- 3. Bảng Phiếu nhập kho (InventoryReceipts)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'InventoryReceipts')
BEGIN
    CREATE TABLE InventoryReceipts (
        id INT IDENTITY(1,1) PRIMARY KEY,
        receipt_code VARCHAR(50) NOT NULL UNIQUE,
        supplier_id INT,
        account_id INT,
        total_amount FLOAT,
        status VARCHAR(50) DEFAULT 'PENDING',
        note NVARCHAR(500),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        CONSTRAINT FK_InventoryReceipts_Suppliers FOREIGN KEY (supplier_id) REFERENCES Suppliers(id),
        CONSTRAINT FK_InventoryReceipts_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id)
    );
    PRINT N'✅ Đã tạo bảng InventoryReceipts thành công.';
END
ELSE
    PRINT N'⚠️ Bảng InventoryReceipts đã tồn tại, bỏ qua.';
GO

-- =============================================
-- 4. Bảng Chi tiết phiếu nhập (InventoryReceiptDetails)
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'InventoryReceiptDetails')
BEGIN
    CREATE TABLE InventoryReceiptDetails (
        id INT IDENTITY(1,1) PRIMARY KEY,
        receipt_id INT NOT NULL,
        variant_id INT NOT NULL,
        quantity INT NOT NULL,
        import_price FLOAT,
        CONSTRAINT FK_ReceiptDetails_Receipts FOREIGN KEY (receipt_id) REFERENCES InventoryReceipts(id) ON DELETE CASCADE,
        CONSTRAINT FK_ReceiptDetails_Variants FOREIGN KEY (variant_id) REFERENCES ProductVariants(id)
    );
    PRINT N'✅ Đã tạo bảng InventoryReceiptDetails thành công.';
END
ELSE
    PRINT N'⚠️ Bảng InventoryReceiptDetails đã tồn tại, bỏ qua.';
GO

PRINT N'';
PRINT N'🎉 Migration hoàn tất! Các bảng nghiệp vụ và dữ liệu mẫu đã sẵn sàng.';
GO