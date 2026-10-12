INSERT INTO categories(id,name) VALUES ('cccccccc-0000-4000-8000-000000000001','Everyday Essentials');
INSERT INTO products(id,name,brand,description,category_id,base_price,status) VALUES
('aaaaaaaa-0000-4000-8000-000000000001','Áo thun Essential Cotton','MSS Studio','THE EVERYDAY TEE','cccccccc-0000-4000-8000-000000000001',290000,'ACTIVE'),
('aaaaaaaa-0000-4000-8000-000000000002','Quần tây Relaxed Fit','MSS Studio','THE RELAXED TROUSER','cccccccc-0000-4000-8000-000000000001',590000,'ACTIVE');
INSERT INTO product_images(id,product_id,image_url,is_main) VALUES
(UUID(),'aaaaaaaa-0000-4000-8000-000000000001','https://images.unsplash.com/photo-1651761179569-4ba2aa054997?auto=format&fit=crop&w=600&q=85',true),
(UUID(),'aaaaaaaa-0000-4000-8000-000000000002','https://media.arket.com/assets/006/02/8d/028dfccf96542e0820cf3dfdaf9e22e554ed4d60.jpg?imwidth=480',true);
INSERT INTO product_variants(id,product_id,size,color,sku)
SELECT UUID(),p.id,s.size,c.color,CONCAT(RIGHT(p.id,2),'-',s.size,'-',c.color)
FROM products p CROSS JOIN (SELECT 'S' size UNION ALL SELECT 'M' UNION ALL SELECT 'L' UNION ALL SELECT 'XL') s
CROSS JOIN (SELECT 'Trắng' color, 'aaaaaaaa-0000-4000-8000-000000000001' product_id UNION ALL SELECT 'Đen','aaaaaaaa-0000-4000-8000-000000000001' UNION ALL SELECT 'Đen','aaaaaaaa-0000-4000-8000-000000000002' UNION ALL SELECT 'Be','aaaaaaaa-0000-4000-8000-000000000002') c WHERE c.product_id=p.id;
