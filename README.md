# E-commerce Platform — Maven Multi-module Spring Boot Microservices

Nền tảng thương mại điện tử cho shop quần áo gồm bốn business service: quản lý người dùng, danh mục sản phẩm, đơn hàng và thanh toán.

## Kiến trúc

```text
ecommerce-platform/
├── pom.xml
├── docker-compose.yml
├── docker-compose.infra.yml
├── infra/
│   ├── eureka-server/
│   ├── config-server/
│   ├── api-gateway/
│   └── mysql/
├── services/
│   ├── user-service/
│   ├── product-catalog-service/
│   ├── order-service/
│   └── payment-service/
├── shared/
│   ├── common-events/
│   └── common-web/
└── config-repo/
```

## Service và cổng mặc định

| Module | Port | Chức năng |
|---|---:|---|
| api-gateway | 8080 | Định tuyến API |
| user-service | 8081 | Người dùng và địa chỉ |
| product-catalog-service | 8082 | Sản phẩm, danh mục, giá và tìm kiếm |
| order-service | 8084 | Tạo và xử lý đơn hàng |
| payment-service | 8085 | Tích hợp và xử lý thanh toán |
| config-server | 8888 | Cấu hình tập trung |
| eureka-server | 8761 | Service discovery |

## Chạy toàn bộ bằng Docker Compose

```bash
docker compose up --build
```

Một lệnh này chạy MySQL, RabbitMQ, Eureka, Config Server, Gateway và bốn business service. Gateway ở `http://localhost:8080`. Order, Payment và Product Catalog dùng profile `shop`, Flyway và các database MySQL riêng. Có thể chạy `docker compose up --build -d` để chạy nền.

Để mở cửa hàng MSS Studio sau khi backend sẵn sàng, chạy `cd frontend/order-ui` rồi `npm start` và truy cập `http://localhost:5173/`. Trang chủ, danh mục, giỏ hàng, checkout COD/PayOS và lịch sử đơn kết nối trực tiếp tới các service local. Xem [hướng dẫn frontend](frontend/order-ui/README.md).

Nếu sử dụng PayOS, tạo `.env` ở thư mục gốc theo `.env.example`, điền `PAYOS_ENABLED=true`, ba key và URL callback. File `.env` không được đưa vào Git hoặc Docker build. Webhook cần một URL HTTPS công khai trỏ đến `/api/payments/payos/webhook`; callback localhost chỉ phục vụ thử giao diện, không cập nhật trạng thái thanh toán từ PayOS.

Để chạy và test hai service bằng Maven trên máy, dùng `mvn -pl services/order-service,services/payment-service -am test`. Hướng dẫn API và luồng request ở [docs/05-order-payment-flow.md](docs/05-order-payment-flow.md).

Mật khẩu MySQL local mặc định là `root`; thay `MYSQL_ROOT_PASSWORD` trong `.env` khi triển khai môi trường khác. Compose tạo các database `mss_order`, `mss_payment`, `mss_product_catalog` bằng Flyway. Không dùng đồng thời `docker-compose.infra.yml` vì hai Compose cùng mở cổng 3306 và 5672.

## PayOS webhook

PayOS cần gọi một public HTTPS URL. Mở tunnel tới API Gateway, đặt `PAYOS_WEBHOOK_URL` trong `.env`, rồi xác nhận URL với PayOS:

```bash
ngrok http 8080
# PAYOS_WEBHOOK_URL=https://<your-domain>/api/payments/payos/webhook
# POST http://localhost:8080/api/payments/payos/webhook/confirm
```

Gateway hỗ trợ cả `/api/payments/**` và `/api/v1/payments/**`; dùng đường dẫn đầu tiên cho PayOS callback.

## Smoke test

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
node scripts/gateway-order-payment-smoke.mjs
node scripts/gateway-payos-link-smoke.mjs
```

Chạy lệnh PayOS sau khi cấu hình key; lệnh này chỉ tạo rồi hủy payment link, không thực hiện chuyển tiền. Có bộ request Postman tại [docs/postman/order-payment.postman_collection.json](docs/postman/order-payment.postman_collection.json).

## Luồng đặt hàng

1. `order-service` kiểm tra sản phẩm và giá qua `product-catalog-service`.
2. `order-service` tạo giao dịch qua `payment-service`.
3. `payment-service` nhận kết quả PayOS và phát sự kiện qua RabbitMQ.
4. `order-service` cập nhật trạng thái đơn theo kết quả thanh toán.
