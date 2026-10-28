# MSS Studio — cửa hàng kết nối backend

Chạy toàn bộ microservices ở thư mục gốc:

    docker compose up --build -d

Sau đó chạy giao diện (Node.js 20+, không cần cài package):

    cd frontend/order-ui
    npm start

Mở http://localhost:5173/. Trang chủ lấy sản phẩm và biến thể từ Product Catalog Service. Tìm kiếm, lọc, sắp xếp và xem nhanh chạy trên dữ liệu API. Giỏ hàng, phí giao hàng, checkout COD/PayOS, lịch sử đơn và hủy đơn đều gọi Order/Payment Service; dữ liệu được lưu trong MySQL. Mỗi trình duyệt có cookie phiên riêng, nên lịch sử đơn chỉ hiện với phiên đã tạo đơn.

Sau khi PayOS chuyển về /payment/success hoặc /payment/cancel, hãy xem trạng thái đơn. Trạng thái đã thanh toán chỉ được cập nhật qua webhook PayOS đã xác thực; việc quay lại frontend không tự xác nhận thanh toán. Muốn nhận webhook cần URL HTTPS công khai và thiết lập PAYOS_WEBHOOK_URL trong file .env ở thư mục gốc.

Tài khoản ở trang chủ dùng User Service: đăng ký, đăng nhập, sửa hồ sơ và lưu địa chỉ. Khi chọn một địa chỉ đã lưu, biểu mẫu checkout được điền sẵn. Phiên đăng nhập được lưu trong cookie `HttpOnly`; mã truy cập không được đưa vào JavaScript của trình duyệt. Đơn hàng của trang cửa hàng vẫn được tra cứu theo phiên trình duyệt của Order Service, không tự gắn vào tài khoản User Service.

Bộ lọc Áo/Quần dùng danh mục do Product Catalog Service trả về. Profile `shop` của Product Catalog cần migration `V4__categorize_apparel.sql` để phân loại dữ liệu seed hiện có.

Chạy `npm run check` để kiểm tra cú pháp. Đăng nhập duy nhất tại trang chủ. Tài khoản `ADMIN` được chuyển tới `/admin` sau khi đăng nhập; các trang `/admin`, `/admin/payment`, `/admin/products` và `/admin/users` kiểm tra quyền tại server giao diện trước khi trả nội dung. Khách hoặc người chưa đăng nhập được chuyển tới trang thông báo quyền truy cập.

Tài khoản mẫu trong môi trường local là `admin@stepzone.local` với mật khẩu `password` (đổi thông tin này khi triển khai ngoài môi trường thử nghiệm). Khu sản phẩm cho phép thêm, sửa, ngừng bán và kích hoạt lại sản phẩm; quản lý biến thể và danh mục. Khu người dùng cho phép thêm, sửa, vô hiệu hóa/kích hoạt lại tài khoản và quản lý địa chỉ giao hàng. Các thao tác quản trị gọi Product Catalog và User Service bằng JWT.
