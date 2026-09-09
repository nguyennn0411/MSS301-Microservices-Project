CREATE TABLE IF NOT EXISTS saga_transaction_logs (
    id CHAR(36) PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    step VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_saga_transaction_logs_order_id (order_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
