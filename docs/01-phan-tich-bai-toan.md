# PHÂN TÍCH BÀI TOÁN

## 1. Thông tin chung

- **Tên dự án:** MSS301 Microservices Project
- **Chủ đề:** Nền tảng thương mại điện tử (E-commerce Platform)
- **Kiến trúc:** Microservices
- **Back-End:** Spring Boot + Spring Cloud
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Cấu hình tập trung:** Spring Cloud Config Server
- **Cơ sở dữ liệu:** MySQL
- **Message Broker:** RabbitMQ
- **Đóng gói và triển khai:** Docker, Docker Compose
- **Quản lý mã nguồn:** GitHub

Hệ thống được xây dựng dưới dạng Maven multi-module, gồm các service hạ tầng và bốn business microservice độc lập.

---

## 2. Bối cảnh bài toán

Trong một hệ thống thương mại điện tử, các chức năng như quản lý người dùng, quản lý sản phẩm, xử lý đơn hàng và thanh toán thường có nghiệp vụ, dữ liệu và tốc độ phát triển khác nhau.

Nếu toàn bộ chức năng được đặt trong một ứng dụng duy nhất, khi hệ thống phát triển lớn sẽ xuất hiện một số vấn đề:

- Mã nguồn ngày càng phức tạp và khó bảo trì.
- Một thay đổi nhỏ có thể yêu cầu build và triển khai lại toàn bộ hệ thống.
- Khó mở rộng riêng phần có tải cao như sản phẩm hoặc đơn hàng.
- Một lỗi nghiêm trọng trong một module có thể ảnh hưởng toàn bộ ứng dụng.
- Việc phát triển song song giữa các thành viên khó khăn hơn.

Vì vậy, dự án lựa chọn kiến trúc **Microservices**, chia hệ thống thành các service nhỏ theo từng miền nghiệp vụ.

---

## 3. Mục tiêu hệ thống

Hệ thống hướng tới các mục tiêu chính:

1. Quản lý thông tin người dùng và địa chỉ.
2. Quản lý danh mục và thông tin sản phẩm.
3. Cho phép tạo và quản lý đơn hàng.
4. Xử lý giao dịch thanh toán.
5. Cho phép client truy cập các API thông qua một đầu vào chung là API Gateway.
6. Các service có thể tự đăng ký và tìm thấy nhau thông qua Eureka Server.
7. Quản lý cấu hình tập trung bằng Config Server.
8. Các service có thể giao tiếp với nhau qua REST API và cơ chế bất đồng bộ khi phù hợp.
9. Có thể chạy toàn bộ hệ thống bằng Docker Compose.

---

## 4. Các tác nhân chính

### 4.1. Khách hàng

Khách hàng có thể:

- Xem thông tin sản phẩm.
- Tìm kiếm sản phẩm.
- Tạo đơn hàng.
- Thanh toán đơn hàng.
- Theo dõi trạng thái đơn hàng và thanh toán.
- Quản lý thông tin cá nhân và địa chỉ.

### 4.2. Quản trị viên

Quản trị viên có thể:

- Quản lý thông tin sản phẩm.
- Quản lý danh mục sản phẩm.
- Theo dõi và quản lý đơn hàng.
- Theo dõi trạng thái thanh toán.
- Quản lý thông tin người dùng khi cần.

---

## 5. Phân rã Microservices

### 5.1. User Service

**Port mặc định:** `8081`

**Vai trò:**

- Quản lý thông tin người dùng.
- Quản lý địa chỉ người dùng.
- Cung cấp dữ liệu người dùng cho các nghiệp vụ liên quan.

**Dữ liệu sở hữu:** dữ liệu người dùng và địa chỉ.

**Database:** `ecommerce_user_service`.

---

### 5.2. Product Catalog Service

**Port mặc định:** `8082`

**Vai trò:**

- Quản lý sản phẩm.
- Quản lý danh mục sản phẩm.
- Quản lý thông tin giá.
- Hỗ trợ tìm kiếm và truy vấn thông tin sản phẩm.
- Cung cấp thông tin sản phẩm cho Order Service khi tạo đơn.

**Dữ liệu sở hữu:** sản phẩm, danh mục và dữ liệu liên quan đến catalog.

**Database:** `ecommerce_product_catalog_service`.

---

### 5.3. Order Service

**Port mặc định:** `8084`

**Vai trò:**

- Tiếp nhận yêu cầu tạo đơn hàng.
- Quản lý thông tin và trạng thái đơn hàng.
- Kiểm tra thông tin sản phẩm/giá với Product Catalog Service.
- Khởi tạo giao dịch với Payment Service.
- Cập nhật trạng thái đơn hàng theo kết quả thanh toán.

**Dữ liệu sở hữu:** đơn hàng và chi tiết đơn hàng.

**Database:** `ecommerce_order_service`.

---

### 5.4. Payment Service

**Port mặc định:** `8085`

**Vai trò:**

- Khởi tạo và quản lý giao dịch thanh toán.
- Tích hợp cổng thanh toán PayOS.
- Tiếp nhận callback/webhook kết quả thanh toán.
- Phát sự kiện trạng thái thanh toán để Order Service cập nhật đơn hàng.

**Dữ liệu sở hữu:** giao dịch và trạng thái thanh toán.

**Database:** `ecommerce_payment_service`.

---

## 6. Các thành phần hạ tầng

### 6.1. Eureka Server

**Port:** `8761`

Eureka Server đóng vai trò **Service Registry / Service Discovery**.

Các service đăng ký thông tin khi khởi động. Nhờ đó, các thành phần trong hệ thống có thể tìm service theo tên logic thay vì phụ thuộc cứng vào địa chỉ IP hoặc port.

---

### 6.2. API Gateway

**Port:** `8080`

API Gateway là điểm truy cập chung từ client vào hệ thống.

Nhiệm vụ chính:

- Nhận request từ client.
- Định tuyến request tới đúng microservice.
- Che giấu cấu trúc nội bộ của hệ thống.
- Tạo vị trí tập trung để bổ sung các chức năng như logging, authentication, authorization, rate limiting trong tương lai.

---

### 6.3. Config Server

**Port:** `8888`

Config Server quản lý cấu hình tập trung cho các service.

Lợi ích:

- Hạn chế lặp cấu hình.
- Dễ thay đổi cấu hình theo môi trường.
- Tách cấu hình khỏi mã nguồn nghiệp vụ.
- Thuận tiện khi số lượng microservice tăng.

---

### 6.4. MySQL

Mỗi business service sử dụng database riêng theo phạm vi nghiệp vụ:

| Service | Database |
|---|---|
| User Service | `ecommerce_user_service` |
| Product Catalog Service | `ecommerce_product_catalog_service` |
| Order Service | `ecommerce_order_service` |
| Payment Service | `ecommerce_payment_service` |

Việc tách database giúp giảm phụ thuộc trực tiếp giữa các service và làm rõ quyền sở hữu dữ liệu.

---

### 6.5. RabbitMQ

RabbitMQ được sử dụng cho giao tiếp bất đồng bộ trong luồng thanh toán.

Sau khi Payment Service nhận kết quả thanh toán, service có thể phát sự kiện trạng thái thanh toán. Order Service nhận sự kiện và cập nhật trạng thái đơn hàng tương ứng.

---

## 7. Luồng nghiệp vụ chính: Đặt hàng và thanh toán

Luồng tổng quát:

1. Client gửi yêu cầu thông qua **API Gateway**.
2. Gateway định tuyến yêu cầu tới **Order Service**.
3. Order Service gọi **Product Catalog Service** để kiểm tra sản phẩm và giá.
4. Nếu dữ liệu hợp lệ, Order Service tạo đơn hàng.
5. Order Service yêu cầu **Payment Service** tạo giao dịch thanh toán.
6. Người dùng thực hiện thanh toán qua cổng thanh toán.
7. Payment Service nhận kết quả/callback thanh toán.
8. Payment Service phát sự kiện trạng thái thanh toán qua **RabbitMQ**.
9. Order Service nhận sự kiện và cập nhật trạng thái đơn hàng.

---

## 8. Yêu cầu chức năng

### User Service

- Tạo người dùng.
- Xem thông tin người dùng.
- Cập nhật thông tin người dùng.
- Quản lý địa chỉ.

### Product Catalog Service

- Tạo, cập nhật, xóa sản phẩm.
- Xem danh sách/chi tiết sản phẩm.
- Quản lý danh mục.
- Tìm kiếm sản phẩm.
- Cung cấp thông tin giá và sản phẩm cho service khác.

### Order Service

- Tạo đơn hàng.
- Xem chi tiết đơn hàng.
- Theo dõi trạng thái đơn.
- Giao tiếp với Product Catalog Service.
- Giao tiếp với Payment Service.
- Cập nhật trạng thái theo sự kiện thanh toán.

### Payment Service

- Tạo giao dịch thanh toán.
- Theo dõi trạng thái thanh toán.
- Tiếp nhận webhook từ PayOS.
- Phát sự kiện kết quả thanh toán.

---

## 9. Yêu cầu phi chức năng

- Các microservice có thể chạy độc lập.
- Client ưu tiên gọi API qua API Gateway.
- Service được đăng ký và khám phá thông qua Eureka.
- Cấu hình được quản lý tập trung.
- Dữ liệu giữa các miền nghiệp vụ được tách biệt.
- Hệ thống có khả năng đóng gói thành Docker image.
- Có thể khởi động hệ thống bằng Docker Compose.
- Mã nguồn được quản lý trên GitHub.
- Kiến trúc cho phép bổ sung service mới trong tương lai.

---

## 10. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 3.5.x |
| Microservices | Spring Cloud |
| Service Discovery | Netflix Eureka |
| Gateway | Spring Cloud Gateway |
| Config | Spring Cloud Config |
| Database | MySQL |
| Message Broker | RabbitMQ |
| Build Tool | Maven |
| Container | Docker |
| Orchestration local | Docker Compose |
| Version Control | Git + GitHub |

---

## 11. Kết luận

Việc phân chia hệ thống thương mại điện tử thành `user-service`, `product-catalog-service`, `order-service` và `payment-service` giúp mỗi service có phạm vi nghiệp vụ rõ ràng và có khả năng phát triển độc lập.

Eureka Server, API Gateway và Config Server cung cấp các thành phần hạ tầng cần thiết của Spring Cloud; MySQL lưu trữ dữ liệu theo từng service, trong khi RabbitMQ hỗ trợ giao tiếp bất đồng bộ trong luồng thanh toán.

Kiến trúc này đáp ứng mục tiêu của project MSS301 về xây dựng RESTful Microservices bằng Spring Boot/Spring Cloud và triển khai hệ thống bằng Docker Compose.
