package com.ecommerce.productcatalog.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.productcatalog.api.dto.CategoryResponse;
import com.ecommerce.productcatalog.api.dto.CreateCategoryRequest;
import com.ecommerce.productcatalog.application.ProductCatalogUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final ProductCatalogUseCase productCatalogUseCase;

    public CategoryController(ProductCatalogUseCase productCatalogUseCase) {
        this.productCatalogUseCase = productCatalogUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(productCatalogUseCase.createCategory(request), "Category created"));
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> listCategories() {
        return ApiResponse.ok(productCatalogUseCase.listCategories());
    }
}
