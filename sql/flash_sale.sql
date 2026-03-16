ALTER TABLE Vouchers ADD 
    is_flash_sale BIT DEFAULT 0, 
    flash_sale_start_hour INT, 
    flash_sale_end_hour INT;
