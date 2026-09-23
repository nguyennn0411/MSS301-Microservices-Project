ALTER TABLE order_items
    ADD COLUMN variant_id CHAR(36) NULL AFTER product_id;

CREATE INDEX idx_order_items_variant_id ON order_items(variant_id);
