/*
===========================================================================
   MIGRATION: Thêm Tính Năng Email Verification
   DATE: 05/01/2026
   DESCRIPTION: 
   - Thêm field email_verified vào bảng Accounts
   - Tạo bảng EmailVerificationTokens để quản lý token xác thực email
===========================================================================
*/

USE ShopOMG;
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
