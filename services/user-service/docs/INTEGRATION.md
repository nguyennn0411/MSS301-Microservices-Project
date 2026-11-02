# User Service — Tài liệu tích hợp

**Base URL local:** `http://localhost:8081`

User Service sở hữu hồ sơ khách hàng và địa chỉ giao hàng. UUID của hai khách hàng seed khớp với dữ liệu demo bên Order Service.

## User API

| Method | Path | Mô tả |
|---|---|---|
| `POST` | `/api/v1/users` | Tạo khách hàng |
| `GET` | `/api/v1/users` | Danh sách khách hàng |
| `GET` | `/api/v1/users/{id}` | Chi tiết khách hàng |
| `PUT` | `/api/v1/users/{id}` | Cập nhật hồ sơ và trạng thái |
| `DELETE` | `/api/v1/users/{id}` | Soft delete bằng trạng thái `INACTIVE` |
| `GET` | `/api/v1/users/me` | Hồ sơ của tài khoản đang đăng nhập |
| `PUT` | `/api/v1/users/me` | Sửa email, họ tên, điện thoại của chính mình |

`POST /api/v1/auth/register` và `POST /api/v1/auth/login` trả về JWT cùng hồ sơ. Gửi `Authorization: Bearer <token>` cho các API hồ sơ và địa chỉ. Khách hàng chỉ xem được hồ sơ và địa chỉ của mình; cập nhật trạng thái và xóa user theo UUID cần quyền `ADMIN`.

Ví dụ tạo khách hàng:

```json
{
  "email": "long@example.com",
  "fullName": "Nguyen Bao Long",
  "phone": "0901234567"
}
```

## Address API

| Method | Path | Mô tả |
|---|---|---|
| `POST` | `/api/addresses` | Tạo địa chỉ |
| `GET` | `/api/addresses?userId={uuid}` | Danh sách địa chỉ của user |
| `GET` | `/api/addresses/user/{uuid}` | Đường dẫn tương đương để lấy danh sách |
| `GET` | `/api/addresses/{id}` | Chi tiết địa chỉ |
| `PUT` | `/api/addresses/{id}` | Cập nhật địa chỉ |
| `DELETE` | `/api/addresses/{id}` | Xóa địa chỉ |

Ví dụ tạo địa chỉ:

```json
{
  "userId": "70000000-0000-0000-0000-000000000001",
  "receiverName": "Nguyen Van An",
  "receiverPhone": "0901000001",
  "addressLine": "123 Le Loi",
  "ward": "Ben Thanh",
  "district": "District 1",
  "city": "Ho Chi Minh City",
  "postalCode": "700000",
  "defaultAddress": true
}
```

Service tự chọn địa chỉ đầu tiên làm mặc định. Khi xóa địa chỉ mặc định, địa chỉ còn lại lâu nhất sẽ được chọn thay thế.

Tất cả response được bọc bởi `ApiResponse` và đi qua API Gateway bằng cùng đường dẫn trên.
Giao diện `/admin/users` dùng tài khoản `ADMIN` để liệt kê, tạo, cập nhật, vô hiệu hóa hoặc kích hoạt lại người dùng và quản lý địa chỉ của họ. Địa chỉ của khách hàng chỉ khách hàng đó hoặc quản trị viên mới được truy cập. Trang chủ dùng `GET/PUT /api/v1/users/me` cùng API địa chỉ sau khi đăng nhập.
