--New Table
-- Bảng lưu trữ tin nhắn chat
CREATE TABLE ChatMessages (
    id INT IDENTITY(1,1) PRIMARY KEY,
    sender NVARCHAR(255) NOT NULL,
    recipient NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    media_url NVARCHAR(500) NULL,
    media_type NVARCHAR(20)  NULL,
    sent_at DATETIME DEFAULT GETDATE()
);

-- Index để query nhanh theo cặp user
CREATE INDEX IX_ChatMessages_Users ON ChatMessages (sender, recipient);
