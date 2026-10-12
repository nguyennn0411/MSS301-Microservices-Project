# MSS Studio — cửa hàng kết nối backend

Chạy toàn bộ microservices ở thư mục gốc:

    docker compose up --build -d

Sau đó chạy giao diện (Node.js 20+, không cần cài package):

    cd frontend/order-ui
    npm start

Mở http://localhost:5173/. Trang chủ lấy sản phẩm và biến thể từ Product Catalog Service. Tìm kiếm, lọc, sắp xếp và xem nhanh chạy trên dữ liệu API. Giỏ hàng, phí giao hàng, checkout COD/PayOS, lịch sử đơn và hủy đơn đều gọi Order/Payment Service; dữ liệu được lưu trong MySQL. Mỗi trình duyệt có cookie phiên riêng, nên lịch sử đơn chỉ hiện với phiên đã tạo đơn.

Sau khi PayOS chuyển về /payment/success hoặc /payment/cancel, hãy xem trạng thái đơn. Trạng thái đã thanh toán chỉ được cập nhật qua webhook PayOS đã xác thực; việc quay lại frontend không tự xác nhận thanh toán. Muốn nhận webhook cần URL HTTPS công khai và thiết lập PAYOS_WEBHOOK_URL trong file .env ở thư mục gốc.

Chạy npm run check để kiểm tra cú pháp. Trang /admin là giao diện API cũ phục vụ kiểm thử kỹ thuật; khách mua hàng dùng trang chủ.
