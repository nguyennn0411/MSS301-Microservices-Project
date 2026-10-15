INSERT INTO categories(id, name) VALUES
('cccccccc-0000-4000-8000-000000000002', 'Áo'),
('cccccccc-0000-4000-8000-000000000003', 'Quần');

UPDATE products
SET category_id = 'cccccccc-0000-4000-8000-000000000002'
WHERE id IN (
    'aaaaaaaa-0000-4000-8000-000000000001',
    'aaaaaaaa-0000-4000-8000-000000000003',
    'aaaaaaaa-0000-4000-8000-000000000004'
);

UPDATE products
SET category_id = 'cccccccc-0000-4000-8000-000000000003'
WHERE id IN (
    'aaaaaaaa-0000-4000-8000-000000000002',
    'aaaaaaaa-0000-4000-8000-000000000005',
    'aaaaaaaa-0000-4000-8000-000000000006'
);
