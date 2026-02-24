/*
===========================================================================
   COMPLETE DATABASE MIGRATION FOR ADMIN FEATURES
   Date: 2026-01-29
   Description: Add ALL missing columns to Accounts table
===========================================================================
*/

USE ShopOMG;
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
