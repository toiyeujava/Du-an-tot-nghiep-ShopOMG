/*
===========================================================================
   MIGRATION: Thêm cột supplier_id vào bảng Products
   DATE: 17/03/2026
===========================================================================
*/
USE ShopOMG;
GO

IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'[dbo].[Products]') 
    AND name = 'supplier_id'
)
BEGIN
    ALTER TABLE Products ADD supplier_id INT;
    PRINT N'✅ Đã thêm cột supplier_id vào bảng Products.';
END
ELSE
BEGIN
    PRINT N'⚠️ Cột supplier_id đã tồn tại trong bảng Products.';
END
GO

IF NOT EXISTS (
    SELECT * FROM sys.foreign_keys 
    WHERE object_id = OBJECT_ID(N'[dbo].[FK_Products_Suppliers]') 
    AND parent_object_id = OBJECT_ID(N'[dbo].[Products]')
)
BEGIN
    ALTER TABLE Products ADD CONSTRAINT FK_Products_Suppliers FOREIGN KEY (supplier_id) REFERENCES Suppliers(id);
    PRINT N'✅ Đã thêm khóa ngoại FK_Products_Suppliers.';
END
ELSE
BEGIN
    PRINT N'⚠️ Khóa ngoại FK_Products_Suppliers đã tồn tại.';
END
GO

-- Cập nhật tất cả sản phẩm hiện tại thuộc về nhà cung cấp "CÔNG TY TNHH THỜI TRANG NAM M.LEO" (ID = 1, nếu đã có)
UPDATE Products SET supplier_id = 1 WHERE supplier_id IS NULL;
PRINT N'✅ Đã cập nhật nhà cung cấp mặc định (ID = 1) cho tất cả sản phẩm hiện có.';
GO
