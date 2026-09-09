CREATE TABLE IF NOT EXISTS payments (
    id CHAR(36) PRIMARY KEY,
    order_id CHAR(36) NOT NULL UNIQUE,
    order_code BIGINT NOT NULL UNIQUE,
    user_id CHAR(36) NOT NULL,
    buyer_name VARCHAR(255),
    buyer_email VARCHAR(255),
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_link_id VARCHAR(255),
    checkout_url TEXT,
    qr_code TEXT,
    failure_reason TEXT,
    paid_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    INDEX idx_payments_status (status),
    INDEX idx_payments_payment_link_id (payment_link_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_transactions (
    id CHAR(36) PRIMARY KEY,
    payment_id CHAR(36) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    provider_reference TEXT,
    provider_response TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_payment_transactions_payment_id (payment_id),
    INDEX idx_payment_transactions_created_at (created_at),
    INDEX idx_payment_transactions_provider_reference (provider_reference(191)),
    CONSTRAINT fk_payment_transactions_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
