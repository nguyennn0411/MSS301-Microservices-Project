-- Fix product/category descriptions to proper Vietnamese (UTF-8).

UPDATE categories
SET description = 'Giày chạy bộ và hiệu năng'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccc01';

UPDATE categories
SET description = 'Giày thời trang hàng ngày'
WHERE id = 'cccccccc-cccc-cccc-cccc-cccccccccc02';

UPDATE products SET description = 'Giày chạy bộ êm, phù hợp tập luyện hàng ngày'
WHERE id = '10000000-0000-0000-0000-000000000001';

UPDATE products SET description = 'Đệm Boost đàn hồi, ôm chân khi chạy dài'
WHERE id = '10000000-0000-0000-0000-000000000002';

UPDATE products SET description = 'Kiểu dáng lifestyle cổ điển với đế ENCAP ổn định'
WHERE id = '10000000-0000-0000-0000-000000000003';

UPDATE products SET description = 'Canvas cao cấp, form cổ điển đường phố'
WHERE id = '10000000-0000-0000-0000-000000000004';

UPDATE products SET description = 'Sneaker skate biểu tượng với sọc bên đặc trưng'
WHERE id = '10000000-0000-0000-0000-000000000005';

UPDATE products SET description = 'Giày low-top lifestyle, dễ phối đồ'
WHERE id = '10000000-0000-0000-0000-000000000006';

UPDATE products SET description = 'Da lộn cổ điển, form thấp thanh lịch'
WHERE id = '10000000-0000-0000-0000-000000000007';

UPDATE products SET description = 'Ổn định cao, phù hợp runner cần hỗ trợ'
WHERE id = '10000000-0000-0000-0000-000000000008';

UPDATE products SET description = 'Sneaker da lộn lifestyle mang tính biểu tượng'
WHERE id = '10000000-0000-0000-0000-000000000009';

UPDATE products SET description = 'Form bóng rổ mid lifestyle trung tính'
WHERE id = '10000000-0000-0000-0000-000000000010';
