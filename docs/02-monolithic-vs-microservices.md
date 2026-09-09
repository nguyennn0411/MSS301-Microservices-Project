# SO SÁNH MONOLITHIC VÀ MICROSERVICES

## 1. Monolithic Architecture

Monolithic là kiến trúc trong đó phần lớn chức năng của hệ thống được xây dựng, đóng gói và triển khai trong **một ứng dụng duy nhất**.

Ví dụ với hệ thống thương mại điện tử, các chức năng User, Product, Order và Payment có thể cùng nằm trong một project và được deploy dưới một ứng dụng Spring Boot.

### Ưu điểm

- Cấu trúc ban đầu đơn giản.
- Dễ phát triển với hệ thống nhỏ.
- Dễ chạy và debug khi số lượng chức năng còn ít.
- Giao tiếp giữa các module đơn giản vì cùng một process.
- Triển khai ban đầu nhanh.

### Nhược điểm

- Codebase tăng nhanh khi hệ thống lớn.
- Các module bị phụ thuộc chặt chẽ hơn.
- Chỉ sửa một chức năng nhỏ vẫn có thể phải build/deploy toàn bộ ứng dụng.
- Khó scale riêng một chức năng.
- Một lỗi nghiêm trọng có thể ảnh hưởng toàn hệ thống.
- Khó chia công việc độc lập khi nhiều thành viên cùng phát triển.

---

## 2. Microservices Architecture

Microservices chia hệ thống thành nhiều service nhỏ. Mỗi service đảm nhiệm một phạm vi nghiệp vụ cụ thể và có thể chạy/triển khai độc lập.

Trong dự án MSS301 này, phần nghiệp vụ được chia thành:

- `user-service`
- `product-catalog-service`
- `order-service`
- `payment-service`

Các service kết hợp với Eureka Server, API Gateway và Config Server để tạo thành hệ thống phân tán.

### Ưu điểm

- Mỗi service có trách nhiệm rõ ràng.
- Dễ bảo trì hơn khi hệ thống mở rộng.
- Có thể phát triển và deploy từng service độc lập.
- Có thể scale riêng service có tải cao.
- Giảm mức độ ảnh hưởng khi một service gặp lỗi.
- Phù hợp với chia việc theo nhóm.
- Cho phép thay đổi hoặc mở rộng từng miền nghiệp vụ dễ dàng.
- Có thể lựa chọn chiến lược lưu trữ phù hợp cho từng service.

### Nhược điểm

- Kiến trúc phức tạp hơn Monolithic.
- Phải xử lý giao tiếp qua mạng.
- Cần Service Discovery và API Gateway.
- Quản lý cấu hình nhiều service phức tạp hơn.
- Debug luồng xuyên nhiều service khó hơn.
- Triển khai và giám sát phức tạp hơn.
- Cần xử lý vấn đề nhất quán dữ liệu giữa các service.

---

## 3. Bảng so sánh

| Tiêu chí | Monolithic | Microservices |
|---|---|---|
| Cấu trúc | Một ứng dụng lớn | Nhiều service nhỏ |
| Triển khai | Deploy toàn bộ hệ thống | Deploy từng service |
| Mức độ phụ thuộc | Cao hơn | Thấp hơn nếu thiết kế tốt |
| Khả năng mở rộng | Thường scale toàn ứng dụng | Scale từng service |
| Database | Thường dùng database chung | Có thể tách database theo service |
| Giao tiếp | Gọi nội bộ trong ứng dụng | REST, message broker,... |
| Phát triển nhóm | Dễ xung đột khi codebase lớn | Dễ chia theo service |
| Service Discovery | Không cần | Thường cần |
| API Gateway | Không bắt buộc | Thường sử dụng |
| Độ phức tạp vận hành | Thấp hơn | Cao hơn |
| Khả năng cô lập lỗi | Thấp hơn | Tốt hơn |
| Docker hóa | Có thể | Rất phù hợp |
| Mở rộng lâu dài | Khó hơn khi hệ thống lớn | Linh hoạt hơn |

---

## 4. Ví dụ trong bài toán thương mại điện tử

### Nếu sử dụng Monolithic

Có thể có cấu trúc:

```text
ecommerce-app
├── user
├── product
├── order
├── payment
└── EcommerceApplication.java
```

Tất cả chức năng chạy trong một ứng dụng.

Nếu module Payment thay đổi, nhóm có thể phải build và triển khai lại toàn bộ `ecommerce-app`.

### Khi sử dụng Microservices

Dự án hiện tại tách thành:

```text
services/
├── user-service/
├── product-catalog-service/
├── order-service/
└── payment-service/
```

Mỗi service có phạm vi riêng và có thể được đóng gói bằng Docker độc lập.

---

## 5. Lý do dự án lựa chọn Microservices

### 5.1. Phù hợp với yêu cầu môn MSS301

Project yêu cầu sử dụng:

- Spring Boot.
- Spring Cloud.
- Service Discovery.
- API Gateway.
- RESTful Microservices.
- Docker và Docker Compose.

Microservices là kiến trúc phù hợp trực tiếp với các yêu cầu trên.

### 5.2. Phân chia nghiệp vụ rõ ràng

Bốn miền nghiệp vụ chính có tính độc lập tương đối:

- Người dùng.
- Danh mục sản phẩm.
- Đơn hàng.
- Thanh toán.

Vì vậy có thể chia thành bốn service riêng.

### 5.3. Dễ mở rộng

Ví dụ Product Catalog có thể nhận số lượng request lớn hơn Payment Service. Với Microservices, Product Catalog Service có thể được scale riêng mà không bắt buộc scale toàn bộ hệ thống.

### 5.4. Dễ triển khai độc lập

Mỗi service có Dockerfile riêng. Khi một service thay đổi, về nguyên tắc có thể build và triển khai lại riêng service đó.

### 5.5. Hỗ trợ giao tiếp phân tán

Dự án thể hiện hai cách giao tiếp:

- **Đồng bộ:** REST API giữa các service.
- **Bất đồng bộ:** RabbitMQ trong luồng cập nhật trạng thái thanh toán.

Điều này giúp sinh viên làm quen với các đặc điểm thực tế của hệ thống Microservices.

---

## 6. Kết luận

Monolithic phù hợp với ứng dụng nhỏ và giai đoạn phát triển ban đầu vì đơn giản hơn. Tuy nhiên khi phạm vi hệ thống tăng, việc triển khai, mở rộng và bảo trì có thể trở nên khó khăn.

Đối với MSS301 E-commerce Platform, Microservices được lựa chọn vì hệ thống có các miền nghiệp vụ tách biệt rõ ràng và yêu cầu trực tiếp về Spring Cloud, Service Discovery, API Gateway, Docker và giao tiếp giữa các service.

Do đó, kiến trúc Microservices phù hợp cả về mặt kỹ thuật lẫn chuẩn đầu ra của project.
