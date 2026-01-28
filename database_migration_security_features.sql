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
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Accounts') 
      AND name = 'email_verified'
)
BEGIN
    ALTER TABLE dbo.Accounts ADD email_verified BIT DEFAULT 0;
    UPDATE dbo.Accounts SET email_verified = 1;
END


-- 1.2. Thêm field failed_login_attempts (Login Attempt Limiting)
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('dbo.Accounts') 
      AND name = 'failed_login_attempts'
)
BEGIN
    ALTER TABLE dbo.Accounts ADD failed_login_attempts INT NOT NULL DEFAULT 0;
END


-- 1.3. Thêm field account_locked_until (Login Attempt Limiting)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Accounts') AND name = 'account_locked_until')
BEGIN
    ALTER TABLE dbo.Accounts ADD account_locked_until DATETIME NULL;
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
    ALTER TABLE dbo.Accounts ADD last_login DATETIME NULL;

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
