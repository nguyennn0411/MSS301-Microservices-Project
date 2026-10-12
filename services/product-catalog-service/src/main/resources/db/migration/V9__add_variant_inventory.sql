ALTER TABLE product_variants
    ADD COLUMN stock_quantity INT NOT NULL DEFAULT 50 AFTER sku,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER stock_quantity;

CREATE TABLE inventory_reservations (
    id CHAR(36) PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    variant_id CHAR(36) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_inventory_reservation_quantity CHECK (quantity > 0),
    CONSTRAINT uq_inventory_reservation_order_variant UNIQUE (order_id, variant_id),
    CONSTRAINT fk_inventory_reservation_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    INDEX idx_inventory_reservation_order (order_id),
    INDEX idx_inventory_reservation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
