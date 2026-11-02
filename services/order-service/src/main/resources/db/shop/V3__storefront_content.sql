CREATE TABLE storefront_content (
    content_key VARCHAR(80) PRIMARY KEY,
    content_value TEXT NOT NULL
);

INSERT INTO storefront_content (content_key, content_value) VALUES
('announcement', 'Giao hàng toàn quốc · Thanh toán COD hoặc PayOS'),
('hero_tag', 'MSS STUDIO / EVERYDAY EDIT'),
('hero_title', 'Đẹp theo cách'),
('hero_accent', 'của riêng bạn.'),
('hero_description', 'Những thiết kế dễ mặc, dễ kết hợp và đủ thoải mái để đi cùng bạn mỗi ngày.'),
('hero_index', '01 / TỦ ĐỒ THIẾT YẾU'),
('collection_tag', 'THE COLLECTION / 2026'),
('collection_title', 'Chọn món đồ'),
('collection_accent', 'hợp với bạn.'),
('collection_description', 'Khám phá các thiết kế đang có tại MSS Studio.'),
('editorial_tag', 'LESS, BUT BETTER'),
('editorial_title', 'Mặc điều bạn yêu.'),
('editorial_subtitle', 'Thật tự nhiên.'),
('editorial_description', 'Bắt đầu với những món đồ có thể đi cùng bạn từ ngày thường đến cuối tuần.'),
('delivery_estimate', 'Dự kiến 3–5 ngày làm việc'),
('size_intro', 'Thông số minh họa theo số đo cơ thể (cm).'),
('size_guide', 'S|84–88|66–70|90–94
M|88–92|70–74|94–98
L|92–96|74–78|98–102
XL|96–100|78–82|102–106');
