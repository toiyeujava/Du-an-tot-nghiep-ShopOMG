CREATE TABLE LookbookPosts (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    account_id      INT NOT NULL,
    product_id      INT NOT NULL,
    order_id        INT NOT NULL,
    image_path      NVARCHAR(500) NOT NULL,
    caption         NVARCHAR(500),
    status          NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    like_count      INT DEFAULT 0,
    created_at      DATETIME2 DEFAULT GETDATE(),

    CONSTRAINT FK_Lookbook_Account FOREIGN KEY (account_id) REFERENCES Accounts(id),
    CONSTRAINT FK_Lookbook_Product FOREIGN KEY (product_id) REFERENCES Products(id),
    CONSTRAINT FK_Lookbook_Order   FOREIGN KEY (order_id)   REFERENCES Orders(id)
);
