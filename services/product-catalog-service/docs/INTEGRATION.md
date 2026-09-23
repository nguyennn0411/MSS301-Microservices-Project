# Product Catalog Service — Tài liệu tích hợp

> **Owner:** Long (product-catalog-service)
> **Đọc bởi:** Phúc (order-service), Frontend  
> **Base URL (local):** `http://localhost:8082`  
> **API prefix:** `/api/v1/products`, `/api/v1/categories`

---

## 1. Service này làm gì?

Quản lý **thông tin sản phẩm catalog** (quần áo):

- Tên, brand, mô tả, giá (`base_price`), category, status
- Variants **size / color**
- Ảnh sản phẩm
- **Validate** product + giá cho order saga

**Không làm:** đơn hàng, thanh toán.

---

## 2. Dữ liệu sản phẩm

```text
product_id (catalog)     →  "Essential Oversized T-Shirt" — chung cho mọi size/màu
product_variants         →  size/color có bán (catalog)
```

Order gửi `productId + size + color + unitPrice` khi đặt hàng.  
Catalog xác nhận **product tồn tại + ACTIVE + giá đúng**.

---

## 3. Seed dev

| product_id | name | base_price | variants |
|---|---|---:|---|
| `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | Essential Oversized T-Shirt | 1000 | S/Black, M/Black, L/White |
| `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb` | Relaxed Linen Shirt | 1000 | M/Beige |

Category seed: `cccccccc-cccc-cccc-cccc-cccccccccccc` (T-Shirts).

---

## 4. API

### 4.1 Health

```
GET /api/v1/products/ping
```

### 4.2 Categories

```
POST /api/v1/categories
GET  /api/v1/categories
```

Response bọc `ApiResponse`.

### 4.3 Products CRUD / search

| Method | Path | Mô tả |
|---|---|---|
| `POST` | `/api/v1/products` | Tạo sản phẩm (+ variants, images) |
| `GET` | `/api/v1/products` | Search: `q`, `categoryId`, `status` |
| `GET` | `/api/v1/products/{productId}` | Chi tiết (kèm variants) |
| `PUT` | `/api/v1/products/{productId}` | Cập nhật (replace variants/images) |
| `DELETE` | `/api/v1/products/{productId}` | Soft delete → status `DISCONTINUED` |

CRUD responses bọc `ApiResponse`.

**Create example:**

```json
{
  "name": "Essential Oversized T-Shirt",
  "brand": "StepZone",
  "description": "Unisex heavyweight cotton oversized T-shirt",
  "categoryId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "basePrice": 1000,
  "status": "ACTIVE",
  "variants": [
    { "size": "S", "color": "Black", "sku": "TEE-ESS-S-BLK" },
    { "size": "M", "color": "Black", "sku": "TEE-ESS-M-BLK" }
  ],
  "images": [
    { "imageUrl": "https://example.com/t-shirt.jpg", "main": true }
  ]
}
```

### 4.4 Validation (Order Feign) — raw body

```
POST /api/v1/products/validation
Content-Type: application/json
```

**Request** (khớp `order-service` Feign):

```json
{
  "items": [
    {
      "productId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "unitPrice": 1000,
      "quantity": 1
    }
  ]
}
```

**Response** (không bọc `ApiResponse`):

```json
{ "valid": true, "message": "All products are valid" }
```

Fail khi: product không tồn tại, không `ACTIVE`, hoặc `unitPrice` ≠ `base_price`.

---

## 5. Checklist team

### Order (Phúc)

- [ ] Gửi đúng `productId` + `unitPrice` = `base_price` seed
- [ ] Feign `POST /api/v1/products/validation` đã khai báo sẵn

### Frontend

- [ ] List/detail lấy variants từ `GET /api/v1/products/{id}`
- [ ] Khi checkout gửi `productId + size + color + unitPrice`

---

## 6. Local run

```bash
docker compose -f docker-compose.infra.yml up -d
# Eureka + Config (optional but recommended)
mvn -pl services/product-catalog-service -am spring-boot:run
```

DB: MySQL `localhost:3306` / `ecommerce_product_catalog_service` / user `root`.

Smoke:

```bash
curl http://localhost:8082/api/v1/products/ping
curl http://localhost:8082/api/v1/products
curl -X POST http://localhost:8082/api/v1/products/validation \
  -H "Content-Type: application/json" \
  -d "{\"items\":[{\"productId\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\",\"unitPrice\":1000,\"quantity\":1}]}"
```
