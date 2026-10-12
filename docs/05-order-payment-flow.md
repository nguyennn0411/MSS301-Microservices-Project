# Order Service và Payment Service

Tài liệu này mô tả hai service trong hệ thống MSS301 theo yêu cầu REST API, giao tiếp liên service, Eureka, Gateway và Docker Compose. Mọi API dưới đây có thể gọi qua `http://localhost:8080`.

## Luồng đặt hàng

1. Client đọc `/api/v1/products` và biến thể từ Product Catalog Service.
2. Client lưu giỏ bằng `PUT /api/shop/cart` với `X-Shop-Session` là UUID phiên mua sắm. Ứng dụng web local đặt cookie phiên và proxy thêm header này; khi gọi bằng Postman cần tự đặt header.
3. `POST /api/shop/checkout` nhận thông tin người mua, địa chỉ, `paymentMethod` là `COD` hoặc `PAYOS`, và `requestKey` là UUID để chống gửi trùng.
4. Order Service kiểm tra sản phẩm và biến thể qua `POST /api/v1/products/validation`; giá và phí vận chuyển lấy từ MySQL, không tin giá do client gửi. Đơn và dòng hàng lưu vào database `mss_order`.
5. Order Service gọi Payment Service qua REST. COD tạo payment trạng thái `PENDING`, đơn trạng thái `CONFIRMED`. PayOS tạo payment link và đơn trạng thái `PENDING_PAYMENT` nếu PayOS đã được cấu hình. Nếu tạo payment thất bại, đơn được giữ với trạng thái `PAYMENT_FAILED` để tra cứu.
6. PayOS gọi webhook đã được xác thực tại `POST /api/payments/payos/webhook`. Payment Service ghi trạng thái và phát sự kiện RabbitMQ. Order Service tiêu thụ sự kiện và cập nhật đơn. Webhook lặp không phát lại sự kiện thành công; webhook đến muộn không mở lại thanh toán đã hủy.
7. Khách hàng tra cứu qua `GET /api/shop/orders` hoặc `GET /api/shop/orders/{orderCode}`; hủy qua `POST /api/shop/orders/{orderCode}/cancel`. Order Service gọi Payment Service hủy giao dịch tương ứng.

Hai service có database độc lập; chúng không truy vấn bảng của nhau. Product Catalog dùng database riêng. Eureka đăng ký service để Gateway định tuyến theo tên `lb://...`. Order Service gọi Product và Payment qua REST bằng địa chỉ cấu hình cho từng môi trường.

## API chính

| Service | Endpoint | Vai trò |
|---|---|---|
| Order | `POST /api/v1/orders` | Tạo đơn qua REST, hỗ trợ COD/PayOS |
| Order | `GET /api/v1/orders/{id}` và `/code/{orderCode}` | Tra cứu đơn |
| Order | `POST /api/v1/orders/{id}/cancel` | Hủy đơn |
| Order | `GET/PUT /api/shop/cart` | Giỏ hàng lưu MySQL |
| Order | `POST /api/shop/checkout` | Đặt hàng từ giỏ, chống gửi trùng |
| Order | `GET /api/shop/orders` và `GET /api/shop/orders/{orderCode}` | Lịch sử và chi tiết theo phiên |
| Payment | `POST /api/payments/cod/create` | Tạo bản ghi COD |
| Payment | `POST /api/payments/payos/create` | Tạo link PayOS |
| Payment | `GET /api/payments/{id}`, `/order/{orderId}`, `/order-code/{orderCode}` | Tra cứu giao dịch |
| Payment | `POST /api/payments/payos/{orderCode}/cancel` | Hủy giao dịch |
| Payment | `POST /api/payments/payos/webhook` | Nhận webhook có chữ ký hợp lệ |
| Payment | `POST /api/payments/payos/webhook/confirm` | Đăng ký URL webhook PayOS |
| Payment | `GET /api/payments/capabilities` | Trạng thái bật PayOS/COD |

`requestKey` và giỏ hàng gắn với UUID phiên; đây là cơ chế demo, chưa phải xác thực người dùng. Không dùng nó làm quyền truy cập trong môi trường công khai. API `/api/v1/orders/**` và `/api/payments/**` cũng chưa có authentication/authorization. Trước triển khai Internet cần thêm xác thực, giới hạn quyền và bảo vệ endpoint tạo/cancel/confirm webhook.

## Chạy và xác minh

```powershell
docker compose up --build -d
node scripts/gateway-order-payment-smoke.mjs
node scripts/gateway-payos-link-smoke.mjs
```

Smoke test đầu tạo đơn COD thử qua Gateway, xác nhận tổng tiền, payment, rồi hủy đơn và payment. Smoke test thứ hai tạo link PayOS thật và hủy ngay, không thanh toán tiền. Test Java: `mvn -pl services/order-service,services/payment-service -am test`. Muốn nhận kết quả thanh toán PayOS, điền `.env` theo `.env.example` và `PAYOS_ENABLED=true`; webhook cần public HTTPS URL. Hai smoke test không xác minh một giao dịch đã trả tiền.
