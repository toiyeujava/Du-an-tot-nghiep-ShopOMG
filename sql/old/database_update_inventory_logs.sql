USE ShopOMG;
GO

-- 1. Tạo bảng InventoryLogs (Lịch sử Nhập/Xuất kho)
CREATE TABLE InventoryLogs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    timestamp DATETIME2 NOT NULL,
    variant_id INT NOT NULL,
    type VARCHAR(10) NOT NULL,
    old_quantity INT NOT NULL,
    change_amount INT NOT NULL,
    new_quantity INT NOT NULL,
    note NVARCHAR(500),
    account_id INT NOT NULL,
    CONSTRAINT FK_InventoryLog_Variant FOREIGN KEY (variant_id) REFERENCES ProductVariants(id),
    CONSTRAINT FK_InventoryLog_Account FOREIGN KEY (account_id) REFERENCES Accounts(id)
);
GO

-- 2. Tạo các chỉ mục (indexes) để tăng tốc độ truy vấn
CREATE INDEX idx_invlog_timestamp ON InventoryLogs (timestamp);
CREATE INDEX idx_invlog_variant ON InventoryLogs (variant_id);
GO
