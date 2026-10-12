-- IDs and emails intentionally match the demo orders in order-service.
INSERT IGNORE INTO users (id, email, full_name, phone, status)
VALUES
    ('70000000-0000-0000-0000-000000000001', 'customer1@stepzone.local', 'Nguyen Van An', '0901000001', 'ACTIVE'),
    ('70000000-0000-0000-0000-000000000002', 'customer2@stepzone.local', 'Tran Minh Thu', '0901000002', 'ACTIVE');

INSERT IGNORE INTO addresses (
    id, user_id, receiver_name, receiver_phone, address_line, ward, district, city, postal_code, is_default
)
VALUES
    (1, '70000000-0000-0000-0000-000000000001', 'Nguyen Van An', '0901000001', '123 Le Loi', 'Ben Thanh', 'District 1', 'Ho Chi Minh City', '700000', TRUE),
    (2, '70000000-0000-0000-0000-000000000002', 'Tran Minh Thu', '0901000002', '45 Nguyen Hue', 'Ben Nghe', 'District 1', 'Ho Chi Minh City', '700000', TRUE);
