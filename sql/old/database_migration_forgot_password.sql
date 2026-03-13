/*
===========================================================================
   MIGRATION: Thêm Tính Năng Quên Mật Khẩu
   DATE: 05/01/2026
   DESCRIPTION: Tạo bảng PasswordResetTokens để quản lý token reset password
===========================================================================
*/

USE ShopOMG;
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
