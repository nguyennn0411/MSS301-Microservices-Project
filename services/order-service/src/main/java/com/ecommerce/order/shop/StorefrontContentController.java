package com.ecommerce.order.shop;

import com.ecommerce.common.web.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Profile("shop")
@RequestMapping("/api/shop/content")
public class StorefrontContentController {
    private final JdbcTemplate db;

    public StorefrontContentController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping
    public ApiResponse<Map<String, String>> get() {
        Map<String, String> content = new LinkedHashMap<>();
        db.query("SELECT content_key, content_value FROM storefront_content ORDER BY content_key",
                (rs, rowNum) -> Map.entry(rs.getString(1), rs.getString(2)))
                .forEach(entry -> content.put(entry.getKey(), entry.getValue()));
        return ApiResponse.ok(content);
    }

    @PutMapping
    @Transactional
    public ApiResponse<Map<String, String>> update(@RequestBody Map<String, String> content) {
        var keys = new HashSet<>(db.queryForList("SELECT content_key FROM storefront_content", String.class));
        if (!content.keySet().equals(keys)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách nội dung không hợp lệ.");
        }
        content.forEach((key, value) -> {
            if (value == null || value.isBlank() || value.length() > 4000) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung phải có từ 1 đến 4000 ký tự.");
            }
            if (key.equals("size_guide")) {
                String[] lines = value.strip().split("\\R");
                if (lines.length > 20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bảng size tối đa 20 dòng.");
                for (String line : lines) {
                    String[] columns = line.split("\\|", -1);
                    if (columns.length != 4 || java.util.Arrays.stream(columns).anyMatch(String::isBlank)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mỗi dòng bảng size cần Size|Ngực|Eo|Hông.");
                    }
                }
            }
            db.update("UPDATE storefront_content SET content_value=? WHERE content_key=?", value.trim(), key);
        });
        return get();
    }
}
