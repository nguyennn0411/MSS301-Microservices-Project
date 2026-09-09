-- Extra categories + 10 products for FE/dev demo.
-- Also fix existing seed image URLs (example.com → real Unsplash).

-- 2 new categories
INSERT IGNORE INTO categories (id, name, description)
VALUES
    (
        'cccccccc-cccc-cccc-cccc-cccccccccc01',
        'Running',
        'Giày chạy bộ và hiệu năng'
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccc02',
        'Lifestyle',
        'Giày thời trang hàng ngày'
    );

-- Fix images for original seed products
UPDATE product_images
SET image_url = 'https://images.unsplash.com/photo-1512374382149-233c42b6a83b?auto=format&fit=crop&w=800&q=80'
WHERE id = 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01';

UPDATE product_images
SET image_url = 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&w=800&q=80'
WHERE id = 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee11';

-- 10 new products
INSERT IGNORE INTO products (id, name, brand, description, category_id, base_price, status)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        'Nike Pegasus 41',
        'Nike',
        'Giày chạy bộ êm, phù hợp tập luyện hàng ngày',
        'cccccccc-cccc-cccc-cccc-cccccccccc01',
        3200000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        'Adidas Ultraboost 22',
        'Adidas',
        'Đệm Boost đàn hồi, ôm chân khi chạy dài',
        'cccccccc-cccc-cccc-cccc-cccccccccc01',
        4500000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        'New Balance 574',
        'New Balance',
        'Kiểu dáng lifestyle cổ điển với đế ENCAP ổn định',
        'cccccccc-cccc-cccc-cccc-cccccccccc02',
        2800000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000004',
        'Converse Chuck 70',
        'Converse',
        'Canvas cao cấp, form cổ điển đường phố',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        1900000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000005',
        'Vans Old Skool',
        'Vans',
        'Sneaker skate biểu tượng với sọc bên đặc trưng',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        1700000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000006',
        'Nike Dunk Low',
        'Nike',
        'Giày low-top lifestyle, dễ phối đồ',
        'cccccccc-cccc-cccc-cccc-cccccccccc02',
        3100000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000007',
        'Adidas Gazelle',
        'Adidas',
        'Da lộn cổ điển, form thấp thanh lịch',
        'cccccccc-cccc-cccc-cccc-cccccccccc02',
        2400000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000008',
        'Asics Gel-Kayano 30',
        'Asics',
        'Ổn định cao, phù hợp runner cần hỗ trợ',
        'cccccccc-cccc-cccc-cccc-cccccccccc01',
        4200000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000009',
        'Puma Suede Classic',
        'Puma',
        'Sneaker da lộn lifestyle mang tính biểu tượng',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        2100000.00,
        'ACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000010',
        'Jordan 1 Mid',
        'Nike',
        'Form bóng rổ mid lifestyle trung tính',
        'cccccccc-cccc-cccc-cccc-cccccccccc02',
        3900000.00,
        'ACTIVE'
    );

-- Variants (2 sizes each, mixed colors)
INSERT IGNORE INTO product_variants (id, product_id, size, color, sku)
VALUES
    ('20000000-0000-0000-0000-000000000101', '10000000-0000-0000-0000-000000000001', '41', 'Black', 'PEG-41-BLK'),
    ('20000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000001', '42', 'Black', 'PEG-42-BLK'),
    ('20000000-0000-0000-0000-000000000103', '10000000-0000-0000-0000-000000000001', '42', 'White', 'PEG-42-WHT'),

    ('20000000-0000-0000-0000-000000000201', '10000000-0000-0000-0000-000000000002', '41', 'Black', 'UB22-41-BLK'),
    ('20000000-0000-0000-0000-000000000202', '10000000-0000-0000-0000-000000000002', '42', 'White', 'UB22-42-WHT'),
    ('20000000-0000-0000-0000-000000000203', '10000000-0000-0000-0000-000000000002', '43', 'Black', 'UB22-43-BLK'),

    ('20000000-0000-0000-0000-000000000301', '10000000-0000-0000-0000-000000000003', '40', 'Grey', 'NB574-40-GRY'),
    ('20000000-0000-0000-0000-000000000302', '10000000-0000-0000-0000-000000000003', '42', 'Grey', 'NB574-42-GRY'),
    ('20000000-0000-0000-0000-000000000303', '10000000-0000-0000-0000-000000000003', '43', 'Navy', 'NB574-43-NVY'),

    ('20000000-0000-0000-0000-000000000401', '10000000-0000-0000-0000-000000000004', '40', 'White', 'CHK70-40-WHT'),
    ('20000000-0000-0000-0000-000000000402', '10000000-0000-0000-0000-000000000004', '41', 'Black', 'CHK70-41-BLK'),
    ('20000000-0000-0000-0000-000000000403', '10000000-0000-0000-0000-000000000004', '42', 'Black', 'CHK70-42-BLK'),

    ('20000000-0000-0000-0000-000000000501', '10000000-0000-0000-0000-000000000005', '41', 'Black', 'VANS-41-BLK'),
    ('20000000-0000-0000-0000-000000000502', '10000000-0000-0000-0000-000000000005', '42', 'Black', 'VANS-42-BLK'),
    ('20000000-0000-0000-0000-000000000503', '10000000-0000-0000-0000-000000000005', '43', 'White', 'VANS-43-WHT'),

    ('20000000-0000-0000-0000-000000000601', '10000000-0000-0000-0000-000000000006', '40', 'White', 'DUNK-40-WHT'),
    ('20000000-0000-0000-0000-000000000602', '10000000-0000-0000-0000-000000000006', '42', 'Black', 'DUNK-42-BLK'),
    ('20000000-0000-0000-0000-000000000603', '10000000-0000-0000-0000-000000000006', '43', 'Green', 'DUNK-43-GRN'),

    ('20000000-0000-0000-0000-000000000701', '10000000-0000-0000-0000-000000000007', '41', 'Blue', 'GAZ-41-BLU'),
    ('20000000-0000-0000-0000-000000000702', '10000000-0000-0000-0000-000000000007', '42', 'Blue', 'GAZ-42-BLU'),
    ('20000000-0000-0000-0000-000000000703', '10000000-0000-0000-0000-000000000007', '43', 'Red', 'GAZ-43-RED'),

    ('20000000-0000-0000-0000-000000000801', '10000000-0000-0000-0000-000000000008', '41', 'Black', 'KAY-41-BLK'),
    ('20000000-0000-0000-0000-000000000802', '10000000-0000-0000-0000-000000000008', '42', 'Blue', 'KAY-42-BLU'),
    ('20000000-0000-0000-0000-000000000803', '10000000-0000-0000-0000-000000000008', '44', 'Black', 'KAY-44-BLK'),

    ('20000000-0000-0000-0000-000000000901', '10000000-0000-0000-0000-000000000009', '40', 'Red', 'SUE-40-RED'),
    ('20000000-0000-0000-0000-000000000902', '10000000-0000-0000-0000-000000000009', '42', 'Black', 'SUE-42-BLK'),
    ('20000000-0000-0000-0000-000000000903', '10000000-0000-0000-0000-000000000009', '43', 'Blue', 'SUE-43-BLU'),

    ('20000000-0000-0000-0000-000000001001', '10000000-0000-0000-0000-000000000010', '41', 'Black', 'J1M-41-BLK'),
    ('20000000-0000-0000-0000-000000001002', '10000000-0000-0000-0000-000000000010', '42', 'White', 'J1M-42-WHT'),
    ('20000000-0000-0000-0000-000000001003', '10000000-0000-0000-0000-000000000010', '43', 'Red', 'J1M-43-RED');

-- Main images (distinct Unsplash URLs)
INSERT IGNORE INTO product_images (id, product_id, image_url, is_main)
VALUES
    ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002',
     'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003',
     'https://images.unsplash.com/photo-1539185441755-769473a23570?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004',
     'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005',
     'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000006',
     'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000007',
     'https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000008',
     'https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009',
     'https://images.unsplash.com/photo-1605348532760-6753d2c43329?auto=format&fit=crop&w=800&q=80', TRUE),
    ('30000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000010',
     'https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=800&q=80', TRUE);
