USE [ShopOMG]
GO

CREATE TABLE [dbo].[Notifications] (
    [Id] INT IDENTITY(1,1) PRIMARY KEY,
    [AccountId] INT NOT NULL,
    [OrderId] INT NULL,
    [Type] NVARCHAR(50) NOT NULL,
    [Title] NVARCHAR(255) NOT NULL,
    [Content] NVARCHAR(MAX) NOT NULL,
    [Link] NVARCHAR(255) NULL,
    [ImageUrls] NVARCHAR(MAX) NULL,
    [IsRead] BIT NOT NULL DEFAULT 0,
    [CreatedAt] DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [FK_Notifications_Accounts] FOREIGN KEY ([AccountId]) REFERENCES [dbo].[Accounts]([Id]),
    CONSTRAINT [FK_Notifications_Orders] FOREIGN KEY ([OrderId]) REFERENCES [dbo].[Orders]([Id])
);
GO

-- Xóa index cũ (nếu có)
IF EXISTS (SELECT name FROM sys.indexes WHERE name = N'IX_Notifications_AccountId')
    DROP INDEX IX_Notifications_AccountId ON [dbo].[Notifications];
GO

-- Tạo index để truy vấn nhanh
CREATE INDEX IX_Notifications_AccountId ON [dbo].[Notifications]([AccountId], [CreatedAt] DESC);
GO

CREATE INDEX IX_Notifications_AccountId_IsRead ON [dbo].[Notifications]([AccountId], [IsRead]);
GO
