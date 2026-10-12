# Test Order Service và Payment Service bằng Postman

Import `order-payment.postman_collection.json`. Collection chứa 14 request, có sẵn base URL và script lưu ID sau khi tạo thành công.

## Khởi động lại

```powershell
docker compose -f docker-compose.infra.yml up -d
& 'C:/Program Files/JetBrains/IntelliJ IDEA 2025.3.1/plugins/maven/lib/maven3/bin/mvn.cmd' -B -pl services/order-service,services/payment-service -am install '-Dmaven.test.skip=true'
./docs/postman/start-smoke.ps1
```

Script dùng database riêng `ecommerce_order_postman_smoke` và `ecommerce_payment_postman_smoke`, tắt Flyway, dùng Hibernate update và tắt Eureka. Đây là cấu hình smoke test; migration mặc định chưa được kiểm chứng. Log tại `logs/order-payment/`. Chỉ chạy script khi cổng 8084 và 8085 trống.

## Thứ tự test

1. Chạy hai request health, kỳ vọng HTTP 200 và status UP.
2. Chạy folder Validation, kỳ vọng HTTP 400.
3. Muốn test thanh toán riêng: cấu hình PAYOS_ENABLED=true và thông tin PayOS hợp lệ trong môi trường trước khi khởi động. Chạy folder Standalone Payment lần lượt. Request tạo sinh orderId/orderCode mới mỗi lần; không tạo bản ghi Order tương ứng. Mở standaloneCheckoutUrl nếu muốn thanh toán. Request cancel chỉ chạy trước khi trả tiền.
4. Muốn test Order → Payment: cần Product Catalog tại localhost:8082 có POST /api/v1/products/validation đúng hợp đồng của Order Service; đặt collection variable productId thành UUID sản phẩm thật. Chạy folder Order to Payment flow lần lượt. Các ID và checkoutUrl tự lưu sau khi tạo thành công. Mở checkoutUrl để thanh toán; bỏ qua request cancel nếu đã thanh toán.

PayOS mặc định tắt. Khi tắt, request tạo payment hợp lệ sẽ lỗi và payment được ghi FAILED; đây không phải kết quả tạo link thành công. Collection không giả lập webhook: endpoint yêu cầu chữ ký PayOS hợp lệ. Kiểm tra cập nhật trạng thái sau thanh toán cần webhook HTTPS và RabbitMQ.

## Vướng mắc mã hiện tại

- Mỗi service có hai migration V1 trong cùng db/migration.
- Test Payment Service tham chiếu lớp cũ không còn tồn tại, nên build test thất bại. Bản smoke bỏ qua biên dịch test.
- Product Catalog hiện chưa có endpoint validation mà Order Service gọi.

## Kết quả đã chạy

- Health Order 8084 và Payment 8085: HTTP 200, UP.
- Body rỗng ở cả hai API tạo: HTTP 500 (lỗi hiện tại). Assertion HTTP 400 trong folder Validation sẽ báo fail để chỉ ra lỗi này.
- Tạo Payment với PayOS tắt: HTTP 500. GET payment theo orderId trả HTTP 400 vì thiếu metadata tên tham số khi compile (compiler chưa bật -parameters, @PathVariable chưa chỉ định tên). Trạng thái lưu DB chưa xác minh qua API được.
- Luồng Order → Payment thành công chưa chạy được vì thiếu Product validation API và PayOS chưa bật.

Chi tiết: smoke-results.json. Hai service được để chạy để bạn test ngay.


Cập nhật khi thêm giao diện: đã sửa tên @PathVariable trong hai controller và khởi động lại service. Tra cứu Payment theo Order ID hiện trả HTTP 200; giao dịch test 10.000 VND có trạng thái FAILED do PayOS tắt. Kết quả lỗi metadata trong smoke-results.json là lần kiểm tra trước bản sửa này. Giao diện tại http://localhost:5173; mã nguồn ở frontend/order-ui.
