/*
===========================================================================
   UPDATE ADMIN ACCOUNTS - Set email_verified = 1
   Date: 2026-01-29
   Description: Ensure all admin accounts can login without email verification
===========================================================================
*/

USE ShopOMG;
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
