# E-commerce Platform — Maven Multi-module Spring Boot Microservices

Nền tảng thương mại điện tử gồm bốn business service: quản lý người dùng, danh mục sản phẩm, đơn hàng và thanh toán.

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

## Chạy local

```bash
mvn clean install -DskipTests
docker compose -f docker-compose.infra.yml up -d
docker compose up --build
```

Hạ tầng local chỉ gồm MySQL 8.4 và RabbitMQ. RabbitMQ truyền các sự kiện trạng thái thanh toán từ `payment-service` sang `order-service`.

Cấu hình mặc định dùng MySQL tại `localhost:3306` với tài khoản `root/root`. Có thể chạy database riêng bằng đúng lệnh:

```bash
docker run --name mysql-db -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.4
```

Các JDBC URL có `createDatabaseIfNotExist=true`, vì vậy bốn database sẽ được tạo khi service kết nối bằng tài khoản root. Nếu dùng `docker-compose.infra.yml`, script trong `infra/mysql/init` sẽ tạo chúng ngay khi container khởi tạo.

## PayOS webhook

PayOS cần gọi một public HTTPS URL. Mở tunnel tới API Gateway và cấu hình trước khi chạy `payment-service`:

```bash
ngrok http 8080
set PAYOS_WEBHOOK_URL=https://<your-ngrok-domain>.ngrok-free.app/api/payments/payos/webhook
set PAYOS_AUTO_CONFIRM_WEBHOOK=true
```

Gateway hỗ trợ cả `/api/payments/**` và `/api/v1/payments/**`; dùng đường dẫn đầu tiên cho PayOS callback.

## Smoke test

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

## Luồng đặt hàng

1. `order-service` kiểm tra sản phẩm và giá qua `product-catalog-service`.
2. `order-service` tạo giao dịch qua `payment-service`.
3. `payment-service` nhận kết quả PayOS và phát sự kiện qua RabbitMQ.
4. `order-service` cập nhật trạng thái đơn theo kết quả thanh toán.
