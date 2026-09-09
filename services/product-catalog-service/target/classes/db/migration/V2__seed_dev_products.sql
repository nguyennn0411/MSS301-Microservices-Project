-- Stable development product IDs for integration tests and local clients.
-- aaaaaaaa-... = Nike AF1 (sizes 42/43 Black, 42 White)
-- bbbbbbbb-... = Adidas Samba (size 40, no color)

INSERT IGNORE INTO categories (id, name, description)
VALUES (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'Sneakers',
    'Casual and lifestyle sneakers'
);

INSERT IGNORE INTO products (id, name, brand, description, category_id, base_price, status)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Nike Air Force 1',
        'Nike',
        'Classic low-top lifestyle sneaker',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        2500000.00,
        'ACTIVE'
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Adidas Samba',
        'Adidas',
        'Iconic indoor football inspired sneaker',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        1800000.00,
        'ACTIVE'
    );

INSERT IGNORE INTO product_variants (id, product_id, size, color, sku)
VALUES
    ('dddddddd-dddd-dddd-dddd-dddddddddd01', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '42', 'Black', 'AF1-42-BLK'),
    ('dddddddd-dddd-dddd-dddd-dddddddddd02', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '43', 'Black', 'AF1-43-BLK'),
    ('dddddddd-dddd-dddd-dddd-dddddddddd03', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '42', 'White', 'AF1-42-WHT'),
    ('dddddddd-dddd-dddd-dddd-dddddddddd11', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '40', NULL, 'SAMBA-40');

INSERT IGNORE INTO product_images (id, product_id, image_url, is_main)
VALUES
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'https://example.com/images/nike-af1-main.jpg',
        TRUE
    ),
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee11',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'https://example.com/images/adidas-samba-main.jpg',
        TRUE
    );
