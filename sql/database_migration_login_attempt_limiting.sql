/*
===========================================================================
   MIGRATION: Thêm Tính Năng Login Attempt Limiting (Chống Brute-Force)
   DATE: 05/01/2026
   DESCRIPTION: 
   - Thêm các fields tracking login attempts vào bảng Accounts
   - Giới hạn 5 lần đăng nhập sai, khóa tài khoản 15 phút
===========================================================================
*/

USE ShopOMG;
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
