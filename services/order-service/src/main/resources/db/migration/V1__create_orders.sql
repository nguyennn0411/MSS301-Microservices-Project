CREATE TABLE IF NOT EXISTS orders (
    id CHAR(36) NOT NULL,
    order_code BIGINT NOT NULL,
    user_id CHAR(36) NOT NULL,
    buyer_name VARCHAR(150) NOT NULL,
    buyer_email VARCHAR(200) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status VARCHAR(30) NOT NULL,
    payment_id CHAR(36) NULL,
    payment_link_id VARCHAR(100) NULL,
    checkout_url VARCHAR(1000) NULL,
    status_reason VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_code UNIQUE (order_code),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_payment_id (payment_id),
    INDEX idx_orders_status (status)
);

CREATE TABLE IF NOT EXISTS order_items (
    id CHAR(36) NOT NULL,
    order_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON DELETE CASCADE
);
