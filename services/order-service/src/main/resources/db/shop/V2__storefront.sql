ALTER TABLE orders ADD buyer_phone VARCHAR(25), ADD shipping_address VARCHAR(500), ADD customer_note VARCHAR(500), ADD payment_method VARCHAR(20) NOT NULL DEFAULT 'PAYOS', ADD shipping_fee DECIMAL(19,2) NOT NULL DEFAULT 0, ADD request_key CHAR(36), ADD UNIQUE KEY uk_order_request (user_id,request_key);
ALTER TABLE order_items ADD variant_id CHAR(36), ADD variant_size VARCHAR(20), ADD variant_color VARCHAR(50);
CREATE TABLE shop_carts (user_id CHAR(36) PRIMARY KEY, items_json TEXT NOT NULL, updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6));
CREATE TABLE shop_settings (setting_key VARCHAR(100) PRIMARY KEY, setting_value VARCHAR(255) NOT NULL);
INSERT INTO shop_settings VALUES ('shipping_fee','30000');
