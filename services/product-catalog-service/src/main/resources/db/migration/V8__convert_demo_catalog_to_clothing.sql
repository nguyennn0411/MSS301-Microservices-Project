-- Convert the footwear demo catalog to clothing while preserving stable product IDs
-- used by order-service demo data and integration tests.
UPDATE categories SET name = 'T-Shirts', description = 'T-shirts, polos and everyday tops'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccccc';
UPDATE categories SET name = 'Outerwear', description = 'Jackets, hoodies and knitwear'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccc01';
UPDATE categories SET name = 'Bottoms', description = 'Jeans, trousers and cargo pants'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccc02';
INSERT IGNORE INTO categories (id, name, description)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccc03', 'Dresses', 'Casual and occasion dresses');

UPDATE products SET name = 'Essential Oversized T-Shirt', brand = 'StepZone', description = 'Unisex heavyweight cotton oversized T-shirt', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccccc' WHERE id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE products SET name = 'Relaxed Linen Shirt', brand = 'StepZone', description = 'Breathable linen-blend shirt with a relaxed fit', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccccc' WHERE id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';
UPDATE products SET name = 'Classic Denim Jacket', brand = 'Urban Thread', description = 'Mid-weight denim jacket for everyday layering', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc01' WHERE id = '10000000-0000-0000-0000-000000000001';
UPDATE products SET name = 'Everyday Zip Hoodie', brand = 'Urban Thread', description = 'Soft fleece zip hoodie with a regular fit', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc01' WHERE id = '10000000-0000-0000-0000-000000000002';
UPDATE products SET name = 'Straight Fit Jeans', brand = 'Blue District', description = 'Classic straight-leg denim jeans', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc02' WHERE id = '10000000-0000-0000-0000-000000000003';
UPDATE products SET name = 'Utility Cargo Pants', brand = 'Northline', description = 'Relaxed cargo pants with functional side pockets', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc02' WHERE id = '10000000-0000-0000-0000-000000000004';
UPDATE products SET name = 'Premium Cotton Polo', brand = 'StepZone', description = 'Pique cotton polo with a clean modern collar', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccccc' WHERE id = '10000000-0000-0000-0000-000000000005';
UPDATE products SET name = 'Lightweight Bomber Jacket', brand = 'Northline', description = 'Minimal bomber jacket for transitional weather', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc01' WHERE id = '10000000-0000-0000-0000-000000000006';
UPDATE products SET name = 'Wide Leg Trousers', brand = 'Form Studio', description = 'High-waisted wide-leg trousers with a fluid drape', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc02' WHERE id = '10000000-0000-0000-0000-000000000007';
UPDATE products SET name = 'Ribbed Knit Cardigan', brand = 'Form Studio', description = 'Soft ribbed cardigan designed for easy layering', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc01' WHERE id = '10000000-0000-0000-0000-000000000008';
UPDATE products SET name = 'Satin Midi Dress', brand = 'Maison Daily', description = 'Elegant satin midi dress with an adjustable waist', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccc03' WHERE id = '10000000-0000-0000-0000-000000000009';
UPDATE products SET name = 'Minimal Graphic T-Shirt', brand = 'StepZone', description = 'Combed cotton T-shirt with a minimal front graphic', category_id = 'cccccccc-cccc-cccc-cccc-cccccccccccc' WHERE id = '10000000-0000-0000-0000-000000000010';

UPDATE product_variants SET size = 'S', color = 'Black', sku = 'TEE-ESS-S-BLK' WHERE id = 'dddddddd-dddd-dddd-dddd-dddddddddd01';
UPDATE product_variants SET size = 'M', color = 'Black', sku = 'TEE-ESS-M-BLK' WHERE id = 'dddddddd-dddd-dddd-dddd-dddddddddd02';
UPDATE product_variants SET size = 'L', color = 'White', sku = 'TEE-ESS-L-WHT' WHERE id = 'dddddddd-dddd-dddd-dddd-dddddddddd03';
UPDATE product_variants SET size = 'M', color = 'Beige', sku = 'SHIRT-LIN-M-BGE' WHERE id = 'dddddddd-dddd-dddd-dddd-dddddddddd11';
UPDATE product_variants SET size = 'S', color = 'Blue', sku = 'DENIM-JKT-S-BLU' WHERE id = '20000000-0000-0000-0000-000000000101';
UPDATE product_variants SET size = 'M', color = 'Blue', sku = 'DENIM-JKT-M-BLU' WHERE id = '20000000-0000-0000-0000-000000000102';
UPDATE product_variants SET size = 'L', color = 'Black', sku = 'DENIM-JKT-L-BLK' WHERE id = '20000000-0000-0000-0000-000000000103';
UPDATE product_variants SET size = 'S', color = 'Grey', sku = 'HOODIE-S-GRY' WHERE id = '20000000-0000-0000-0000-000000000201';
UPDATE product_variants SET size = 'M', color = 'Black', sku = 'HOODIE-M-BLK' WHERE id = '20000000-0000-0000-0000-000000000202';
UPDATE product_variants SET size = 'L', color = 'Navy', sku = 'HOODIE-L-NVY' WHERE id = '20000000-0000-0000-0000-000000000203';
UPDATE product_variants SET size = '28', color = 'Indigo', sku = 'JEANS-28-IND' WHERE id = '20000000-0000-0000-0000-000000000301';
UPDATE product_variants SET size = '30', color = 'Indigo', sku = 'JEANS-30-IND' WHERE id = '20000000-0000-0000-0000-000000000302';
UPDATE product_variants SET size = '32', color = 'Black', sku = 'JEANS-32-BLK' WHERE id = '20000000-0000-0000-0000-000000000303';
UPDATE product_variants SET size = 'S', color = 'Khaki', sku = 'CARGO-S-KHK' WHERE id = '20000000-0000-0000-0000-000000000401';
UPDATE product_variants SET size = 'M', color = 'Black', sku = 'CARGO-M-BLK' WHERE id = '20000000-0000-0000-0000-000000000402';
UPDATE product_variants SET size = 'L', color = 'Olive', sku = 'CARGO-L-OLV' WHERE id = '20000000-0000-0000-0000-000000000403';
UPDATE product_variants SET size = 'S', color = 'White', sku = 'POLO-S-WHT' WHERE id = '20000000-0000-0000-0000-000000000501';
UPDATE product_variants SET size = 'M', color = 'Navy', sku = 'POLO-M-NVY' WHERE id = '20000000-0000-0000-0000-000000000502';
UPDATE product_variants SET size = 'L', color = 'Black', sku = 'POLO-L-BLK' WHERE id = '20000000-0000-0000-0000-000000000503';
UPDATE product_variants SET size = 'S', color = 'Black', sku = 'BOMBER-S-BLK' WHERE id = '20000000-0000-0000-0000-000000000601';
UPDATE product_variants SET size = 'M', color = 'Olive', sku = 'BOMBER-M-OLV' WHERE id = '20000000-0000-0000-0000-000000000602';
UPDATE product_variants SET size = 'L', color = 'Navy', sku = 'BOMBER-L-NVY' WHERE id = '20000000-0000-0000-0000-000000000603';
UPDATE product_variants SET size = 'S', color = 'Black', sku = 'TROUSER-S-BLK' WHERE id = '20000000-0000-0000-0000-000000000701';
UPDATE product_variants SET size = 'M', color = 'Beige', sku = 'TROUSER-M-BGE' WHERE id = '20000000-0000-0000-0000-000000000702';
UPDATE product_variants SET size = 'L', color = 'Grey', sku = 'TROUSER-L-GRY' WHERE id = '20000000-0000-0000-0000-000000000703';
UPDATE product_variants SET size = 'S', color = 'Cream', sku = 'CARDIGAN-S-CRM' WHERE id = '20000000-0000-0000-0000-000000000801';
UPDATE product_variants SET size = 'M', color = 'Brown', sku = 'CARDIGAN-M-BRN' WHERE id = '20000000-0000-0000-0000-000000000802';
UPDATE product_variants SET size = 'L', color = 'Black', sku = 'CARDIGAN-L-BLK' WHERE id = '20000000-0000-0000-0000-000000000803';
UPDATE product_variants SET size = 'S', color = 'Black', sku = 'DRESS-S-BLK' WHERE id = '20000000-0000-0000-0000-000000000901';
UPDATE product_variants SET size = 'M', color = 'Champagne', sku = 'DRESS-M-CHM' WHERE id = '20000000-0000-0000-0000-000000000902';
UPDATE product_variants SET size = 'L', color = 'Burgundy', sku = 'DRESS-L-BRG' WHERE id = '20000000-0000-0000-0000-000000000903';
UPDATE product_variants SET size = 'S', color = 'White', sku = 'TEE-GFX-S-WHT' WHERE id = '20000000-0000-0000-0000-000000001001';
UPDATE product_variants SET size = 'M', color = 'Black', sku = 'TEE-GFX-M-BLK' WHERE id = '20000000-0000-0000-0000-000000001002';
UPDATE product_variants SET size = 'L', color = 'Green', sku = 'TEE-GFX-L-GRN' WHERE id = '20000000-0000-0000-0000-000000001003';

UPDATE product_images SET image_url = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=800&q=80' WHERE product_id IN ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '10000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000010');
UPDATE product_images SET image_url = 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?auto=format&fit=crop&w=800&q=80' WHERE product_id IN ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '10000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000006');
UPDATE product_images SET image_url = 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=800&q=80' WHERE product_id IN ('10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000007');
UPDATE product_images SET image_url = 'https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?auto=format&fit=crop&w=800&q=80' WHERE product_id IN ('10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000009');
