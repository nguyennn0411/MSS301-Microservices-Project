INSERT INTO products(id,name,brand,description,category_id,base_price,status) VALUES
('aaaaaaaa-0000-4000-8000-000000000003','Áo sơ mi Linen Blend','MSS Studio','THE LINEN SHIRT','cccccccc-0000-4000-8000-000000000001',420000,'ACTIVE'),
('aaaaaaaa-0000-4000-8000-000000000004','Áo khoác Weekend Utility','MSS Studio','THE WEEKEND LAYER','cccccccc-0000-4000-8000-000000000001',890000,'ACTIVE'),
('aaaaaaaa-0000-4000-8000-000000000005','Quần jeans Straight Denim','MSS Studio','THE STRAIGHT DENIM','cccccccc-0000-4000-8000-000000000001',690000,'ACTIVE'),
('aaaaaaaa-0000-4000-8000-000000000006','Quần short Summer Chino','MSS Studio','THE SUMMER SHORT','cccccccc-0000-4000-8000-000000000001',390000,'ACTIVE');

INSERT INTO product_images(id,product_id,image_url,is_main) VALUES
(UUID(),'aaaaaaaa-0000-4000-8000-000000000003','https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=900&q=85',true),
(UUID(),'aaaaaaaa-0000-4000-8000-000000000004','https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=900&q=85',true),
(UUID(),'aaaaaaaa-0000-4000-8000-000000000005','https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=900&q=85',true),
(UUID(),'aaaaaaaa-0000-4000-8000-000000000006','https://images.unsplash.com/photo-1591195853828-11db59a44f6b?auto=format&fit=crop&w=900&q=85',true);

INSERT INTO product_variants(id,product_id,size,color,sku)
SELECT UUID(), c.product_id, s.size, c.color, CONCAT(RIGHT(c.product_id,2),'-',s.size,'-',c.color)
FROM (SELECT 'S' size UNION ALL SELECT 'M' UNION ALL SELECT 'L' UNION ALL SELECT 'XL') s
CROSS JOIN (
  SELECT 'aaaaaaaa-0000-4000-8000-000000000003' product_id, 'Trắng' color
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000003', 'Be'
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000004', 'Đen'
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000004', 'Nâu'
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000005', 'Xanh denim'
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000005', 'Đen'
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000006', 'Be'
  UNION ALL SELECT 'aaaaaaaa-0000-4000-8000-000000000006', 'Đen'
) c;
