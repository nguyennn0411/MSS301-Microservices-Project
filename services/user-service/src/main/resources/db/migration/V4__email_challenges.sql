CREATE TABLE email_challenges (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    purpose VARCHAR(20) NOT NULL,
    code_hash VARCHAR(100) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    full_name VARCHAR(150),
    phone VARCHAR(20),
    password_hash VARCHAR(100),
    CONSTRAINT uq_email_challenge UNIQUE (email, purpose)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
