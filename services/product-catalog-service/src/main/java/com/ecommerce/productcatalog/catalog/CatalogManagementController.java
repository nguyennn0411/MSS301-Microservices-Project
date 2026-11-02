package com.ecommerce.productcatalog.catalog;

import com.ecommerce.common.web.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Profile("shop")
@RestController
public class CatalogManagementController {
    private final JdbcTemplate db;

    public CatalogManagementController(JdbcTemplate db) { this.db = db; }

    public record Category(UUID id, String name, String description) {}
    public record CategoryInput(@NotBlank @Size(max = 100) String name, String description) {}
    public record Variant(UUID id, String size, String color, String sku) {}
    public record VariantInput(UUID id, @NotBlank @Size(max = 20) String size,
                               @Size(max = 50) String color, @Size(max = 100) String sku) {}
    public record Product(UUID id, String name, String brand, String collection, UUID categoryId,
                          String category, BigDecimal price, String status, String image, List<Variant> variants) {}
    public record ProductInput(@NotBlank @Size(max = 255) String name, @Size(max = 100) String brand,
                               String collection, @NotNull UUID categoryId,
                               @NotNull @DecimalMin("0.01") BigDecimal price,
                               @NotBlank String status, String image,
                               @NotNull List<@Valid VariantInput> variants) {}

    @GetMapping("/api/v1/categories")
    public ApiResponse<List<Category>> categories() {
        return ApiResponse.ok(db.query("SELECT id,name,description FROM categories ORDER BY name,id",
                (rs, n) -> new Category(UUID.fromString(rs.getString("id")),
                        rs.getString("name"), rs.getString("description"))));
    }

    @PostMapping("/api/v1/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryInput input) {
        checkCategoryName(input.name(), null);
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO categories(id,name,description) VALUES(?,?,?)", id.toString(), input.name().trim(), input.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(new Category(id, input.name().trim(), input.description())));
    }

    @PutMapping("/api/v1/categories/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryInput input) {
        checkCategoryName(input.name(), id);
        if (db.update("UPDATE categories SET name=?,description=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                input.name().trim(), input.description(), id.toString()) == 0) throw missing("Danh mục không tồn tại.");
        return ApiResponse.ok(new Category(id, input.name().trim(), input.description()));
    }

    @DeleteMapping("/api/v1/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable UUID id) {
        if (count("SELECT COUNT(*) FROM products WHERE category_id=?", id.toString()) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Danh mục đang được sản phẩm sử dụng.");
        if (db.update("DELETE FROM categories WHERE id=?", id.toString()) == 0) throw missing("Danh mục không tồn tại.");
        return ApiResponse.ok(null, "Category deleted");
    }

    @GetMapping("/api/v1/products/admin")
    public ApiResponse<List<Product>> products() {
        return ApiResponse.ok(db.query("SELECT p.*,c.name AS category_name FROM products p LEFT JOIN categories c ON c.id=p.category_id ORDER BY p.created_at DESC,p.id",
                (rs, n) -> product(rs)));
    }

    @PostMapping("/api/v1/products")
    @Transactional
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductInput input) {
        validateProduct(input);
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO products(id,name,brand,description,category_id,base_price,status) VALUES(?,?,?,?,?,?,?)",
                id.toString(), input.name().trim(), input.brand(), input.collection(), input.categoryId().toString(), input.price(), input.status());
        saveImage(id, input.image());
        saveVariants(id, input.variants());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(findProduct(id)));
    }

    @PutMapping("/api/v1/products/{id}")
    @Transactional
    public ApiResponse<Product> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductInput input) {
        validateProduct(input);
        if (db.update("UPDATE products SET name=?,brand=?,description=?,category_id=?,base_price=?,status=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                input.name().trim(), input.brand(), input.collection(), input.categoryId().toString(), input.price(), input.status(), id.toString()) == 0)
            throw missing("Sản phẩm không tồn tại.");
        db.update("DELETE FROM product_images WHERE product_id=?", id.toString());
        saveImage(id, input.image());
        saveVariants(id, input.variants());
        return ApiResponse.ok(findProduct(id));
    }

    @DeleteMapping("/api/v1/products/{id}")
    public ApiResponse<Product> discontinueProduct(@PathVariable UUID id) {
        if (db.update("UPDATE products SET status='DISCONTINUED',updated_at=CURRENT_TIMESTAMP WHERE id=?", id.toString()) == 0)
            throw missing("Sản phẩm không tồn tại.");
        return ApiResponse.ok(findProduct(id));
    }

    private Product findProduct(UUID id) {
        return db.query("SELECT p.*,c.name AS category_name FROM products p LEFT JOIN categories c ON c.id=p.category_id WHERE p.id=?",
                (rs, n) -> product(rs), id.toString()).stream().findFirst().orElseThrow(() -> missing("Sản phẩm không tồn tại."));
    }

    private Product product(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String categoryId = rs.getString("category_id");
        List<String> images = db.queryForList("SELECT image_url FROM product_images WHERE product_id=? ORDER BY is_main DESC,id LIMIT 1", String.class, id.toString());
        List<Variant> variants = db.query("SELECT id,size,color,sku FROM product_variants WHERE product_id=? ORDER BY color,size,id",
                (v, n) -> new Variant(UUID.fromString(v.getString("id")), v.getString("size"), v.getString("color"), v.getString("sku")), id.toString());
        return new Product(id, rs.getString("name"), rs.getString("brand"), rs.getString("description"),
                categoryId == null ? null : UUID.fromString(categoryId), rs.getString("category_name"),
                rs.getBigDecimal("base_price"), rs.getString("status"), images.isEmpty() ? null : images.getFirst(), variants);
    }

    private void validateProduct(ProductInput input) {
        if (!List.of("ACTIVE", "INACTIVE", "DISCONTINUED").contains(input.status()))
            throw new IllegalArgumentException("Trạng thái sản phẩm không hợp lệ.");
        if (count("SELECT COUNT(*) FROM categories WHERE id=?", input.categoryId().toString()) == 0)
            throw new IllegalArgumentException("Danh mục không tồn tại.");
        if (input.image() != null && !input.image().isBlank() &&
                (!input.image().matches("(?i)^https?://[^\\s]+$") || input.image().length() > 2000))
            throw new IllegalArgumentException("URL ảnh phải bắt đầu bằng http hoặc https và ngắn hơn 2000 ký tự.");
        for (int i = 0; i < input.variants().size(); i++) {
            VariantInput variant = input.variants().get(i);
            for (int j = 0; j < i; j++) {
                VariantInput prior = input.variants().get(j);
                if (variant.size().trim().equalsIgnoreCase(prior.size().trim()) &&
                        String.valueOf(variant.color()).trim().equalsIgnoreCase(String.valueOf(prior.color()).trim()))
                    throw new IllegalArgumentException("Size và màu của biến thể bị trùng.");
            }
        }
    }

    private void saveImage(UUID productId, String image) {
        if (image != null && !image.isBlank()) db.update("INSERT INTO product_images(id,product_id,image_url,is_main) VALUES(?,?,?,true)",
                UUID.randomUUID().toString(), productId.toString(), image.trim());
    }

    private void saveVariants(UUID productId, List<VariantInput> input) {
        List<String> existing = db.queryForList("SELECT id FROM product_variants WHERE product_id=?", String.class, productId.toString());
        for (VariantInput variant : input) {
            if (variant.id() != null && !existing.contains(variant.id().toString()))
                throw new IllegalArgumentException("Biến thể không thuộc sản phẩm này.");
        }
        for (String id : existing) {
            if (input.stream().noneMatch(v -> v.id() != null && v.id().toString().equals(id)))
                db.update("DELETE FROM product_variants WHERE id=?", id);
        }
        for (VariantInput variant : input) {
            if (variant.id() == null) db.update("INSERT INTO product_variants(id,product_id,size,color,sku) VALUES(?,?,?,?,?)",
                    UUID.randomUUID().toString(), productId.toString(), variant.size().trim(), variant.color(), variant.sku());
            else db.update("UPDATE product_variants SET size=?,color=?,sku=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                    variant.size().trim(), variant.color(), variant.sku(), variant.id().toString());
        }
    }

    private void checkCategoryName(String name, UUID except) {
        if (count("SELECT COUNT(*) FROM categories WHERE LOWER(name)=LOWER(?) AND (? IS NULL OR id<>?)",
                name.trim(), except == null ? null : except.toString(), except == null ? null : except.toString()) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên danh mục đã tồn tại.");
    }

    private int count(String sql, Object... args) {
        return db.queryForObject(sql, Integer.class, args);
    }

    private ResponseStatusException missing(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
