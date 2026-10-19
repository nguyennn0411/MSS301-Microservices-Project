# MSS Studio — cửa hàng kết nối backend

Chạy toàn bộ microservices ở thư mục gốc:

    docker compose up --build -d

Sau đó chạy giao diện (Node.js 20+, không cần cài package):

    cd frontend/order-ui
    npm start

Mở http://localhost:5173/. Trang chủ lấy sản phẩm và biến thể từ Product Catalog Service. Tìm kiếm, lọc, sắp xếp và xem nhanh chạy trên dữ liệu API. Giỏ hàng, phí giao hàng, checkout COD/PayOS, lịch sử đơn và hủy đơn đều gọi Order/Payment Service; dữ liệu được lưu trong MySQL. Mỗi trình duyệt có cookie phiên riêng, nên lịch sử đơn chỉ hiện với phiên đã tạo đơn.

Khi chọn PayOS và đặt hàng, trang tự chuyển tới trang QR do PayOS cung cấp. Sau khi quét mã, PayOS chuyển về `/payment/success` hoặc `/payment/cancel`; giao diện hỏi backend để xác minh trạng thái thực của PayOS và đơn hàng trước khi báo thanh toán thành công. Webhook vẫn nên cấu hình bằng URL HTTPS công khai qua `PAYOS_WEBHOOK_URL` trong file `.env` ở thư mục gốc để đơn hàng được cập nhật ngay cả khi khách đóng trình duyệt.

Chạy npm run check để kiểm tra cú pháp. Trang /admin là giao diện API cũ phục vụ kiểm thử kỹ thuật; khách mua hàng dùng trang chủ.
