CREATE TABLE FlashSaleClaims (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    account_id  INT NOT NULL,
    voucher_id  INT NOT NULL,
    claimed_at  DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_FlashSaleClaims_Accounts FOREIGN KEY (account_id) REFERENCES Accounts(id),
    CONSTRAINT FK_FlashSaleClaims_Vouchers FOREIGN KEY (voucher_id) REFERENCES Vouchers(id)
);