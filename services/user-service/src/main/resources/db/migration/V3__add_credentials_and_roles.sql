ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(100) NULL AFTER phone,
    ADD COLUMN role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER' AFTER password_hash;

-- Demo password is "password". Production users are always stored with a fresh BCrypt hash.
UPDATE users
SET password_hash = '$2a$10$oM9l00sdBdaH5IkVpCCv..S5wksu9X6Qyx5rAaxjMckuSXCVXoDrC'
WHERE password_hash IS NULL;

INSERT IGNORE INTO users (id, email, full_name, phone, password_hash, role, status)
VALUES (
    '70000000-0000-0000-0000-000000000099',
    'admin@stepzone.local',
    'StepZone Administrator',
    '0901000099',
    '$2a$10$oM9l00sdBdaH5IkVpCCv..S5wksu9X6Qyx5rAaxjMckuSXCVXoDrC',
    'ADMIN',
    'ACTIVE'
);

ALTER TABLE users
    MODIFY COLUMN password_hash VARCHAR(100) NOT NULL;

CREATE INDEX idx_users_role ON users(role);
