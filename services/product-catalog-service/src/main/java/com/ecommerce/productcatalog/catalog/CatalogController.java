package com.ecommerce.productcatalog.catalog;

import com.ecommerce.common.web.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@org.springframework.context.annotation.Profile("shop")
@RestController
@RequestMapping("/api/v1/products")
public class CatalogController {
    private final JdbcTemplate db;
    public CatalogController(JdbcTemplate db) { this.db = db; }
    public record Variant(UUID id, String size, String color) {}
    public record Product(UUID id, String name, String collection, String category, BigDecimal price,
                          String image, List<Variant> variants) {}
    public record Item(@NotNull UUID productId, @Min(1) @Max(10) int quantity, UUID variantId) {}
    public record ValidationRequest(@NotEmpty List<@Valid Item> items) {}
    public record ValidatedItem(UUID productId, String productName, BigDecimal unitPrice, boolean available, String reason, UUID variantId, String size, String color) {}
    public record ValidationResult(boolean valid, List<ValidatedItem> items, String message) {}

    @GetMapping
    public ApiResponse<List<Product>> list() {
        return ApiResponse.ok(db.query("SELECT p.*, c.name AS category_name FROM products p "
                + "LEFT JOIN categories c ON c.id=p.category_id "
                + "WHERE p.status='ACTIVE' ORDER BY p.created_at,p.id", (rs,n) -> {
            UUID id = UUID.fromString(rs.getString("id"));
            var images = db.queryForList("SELECT image_url FROM product_images WHERE product_id=? ORDER BY is_main DESC,id LIMIT 1", String.class,id.toString());
            var variants = db.query("SELECT id,size,color FROM product_variants WHERE product_id=? ORDER BY color,FIELD(size,'S','M','L','XL'),size",(v,i)->new Variant(UUID.fromString(v.getString("id")),v.getString("size"),v.getString("color")),id.toString());
            return new Product(id,rs.getString("name"),rs.getString("description"),
                    rs.getString("category_name"),rs.getBigDecimal("base_price"),
                    images.isEmpty()?null:images.getFirst(),variants);
        }));
    }

    @PostMapping("/validation")
    public ApiResponse<ValidationResult> validate(@Valid @RequestBody ValidationRequest request) {
        List<ValidatedItem> result = new ArrayList<>();
        for (Item item : request.items()) {
            var rows = db.queryForList("SELECT * FROM products WHERE id=? AND status='ACTIVE'",item.productId().toString());
            if(rows.isEmpty()) { result.add(new ValidatedItem(item.productId(),null,null,false,"Sản phẩm không còn bán.",item.variantId(),null,null)); continue; }
            var p=rows.getFirst();
            var variants = item.variantId()==null ? List.<Map<String,Object>>of() : db.queryForList("SELECT * FROM product_variants WHERE id=? AND product_id=?",item.variantId().toString(),item.productId().toString());
            boolean available = item.variantId()==null || !variants.isEmpty();
            var v=variants.isEmpty()?Map.<String,Object>of():variants.getFirst();
            result.add(new ValidatedItem(item.productId(),(String)p.get("name"),(BigDecimal)p.get("base_price"),available,available?null:"Size hoặc màu không tồn tại.",item.variantId(),(String)v.get("size"),(String)v.get("color")));
        }
        boolean valid=result.stream().allMatch(ValidatedItem::available);
        return ApiResponse.ok(new ValidationResult(valid,result,valid?"OK":"Sản phẩm hoặc biến thể không hợp lệ."));
    }
}
