-- =============================================
-- Migration: Add QR Payment Tracking to Orders
-- Date: 2026-03-12
-- Description: Adds payment_status, payment_confirmed_at,
--              and payment_confirmed_by columns to Orders table
--              to support admin QR payment confirmation workflow.
-- =============================================

-- Step 1: Add payment_status column
-- Values: NOT_REQUIRED (COD), QR_PENDING, QR_CONFIRMED, QR_REJECTED
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Orders' AND COLUMN_NAME = 'payment_status'
)
BEGIN
    ALTER TABLE Orders ADD payment_status NVARCHAR(30) DEFAULT 'NOT_REQUIRED';
    PRINT 'Added column: payment_status';
END
GO

-- Step 2: Add payment_confirmed_at timestamp
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Orders' AND COLUMN_NAME = 'payment_confirmed_at'
)
BEGIN
    ALTER TABLE Orders ADD payment_confirmed_at DATETIME2;
    PRINT 'Added column: payment_confirmed_at';
END
GO

-- Step 3: Add payment_confirmed_by (admin username)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Orders' AND COLUMN_NAME = 'payment_confirmed_by'
)
BEGIN
    ALTER TABLE Orders ADD payment_confirmed_by NVARCHAR(100);
    PRINT 'Added column: payment_confirmed_by';
END
GO

-- Step 4: Backfill existing QR orders (if any) to QR_PENDING
UPDATE Orders 
SET payment_status = 'QR_PENDING' 
WHERE payment_method = 'QR' 
  AND payment_status = 'NOT_REQUIRED'
  AND status NOT IN ('COMPLETED', 'CANCELLED');
GO

-- Step 5: Set NOT_REQUIRED for all non-QR orders
UPDATE Orders 
SET payment_status = 'NOT_REQUIRED' 
WHERE payment_status IS NULL;
GO

PRINT 'Migration complete: QR Payment Tracking columns added to Orders table.';
