package com.ecommerce.productcatalog.api;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.productcatalog.api.dto.CreateProductRequest;
import com.ecommerce.productcatalog.api.dto.ProductResponse;
import com.ecommerce.productcatalog.api.dto.ProductValidationResponse;
import com.ecommerce.productcatalog.api.dto.UpdateProductRequest;
import com.ecommerce.productcatalog.api.dto.InventoryReservationRequest;
import com.ecommerce.productcatalog.api.dto.InventoryReservationResponse;
import com.ecommerce.productcatalog.api.dto.ValidateProductsRequest;
import com.ecommerce.productcatalog.application.ProductCatalogUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductCatalogUseCase productCatalogUseCase;

    public ProductController(ProductCatalogUseCase productCatalogUseCase) {
        this.productCatalogUseCase = productCatalogUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(productCatalogUseCase.createProduct(request), "Product created"));
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> searchProducts(
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(productCatalogUseCase.searchProducts(keyword, categoryId, status));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable UUID productId) {
        return ApiResponse.ok(productCatalogUseCase.getProduct(productId));
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable UUID productId,
                                                      @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.ok(productCatalogUseCase.updateProduct(productId, request), "Product updated");
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<ProductResponse> discontinueProduct(@PathVariable UUID productId) {
        return ApiResponse.ok(productCatalogUseCase.discontinueProduct(productId), "Product discontinued");
    }

    @PostMapping("/validation")
    public ProductValidationResponse validateProducts(@Valid @RequestBody ValidateProductsRequest request) {
        return productCatalogUseCase.validateProducts(request);
    }

    @PostMapping("/inventory/reservations")
    public InventoryReservationResponse reserveInventory(
            @Valid @RequestBody InventoryReservationRequest request) {
        return productCatalogUseCase.reserveInventory(request);
    }

    @DeleteMapping("/inventory/reservations/{orderId}")
    public InventoryReservationResponse releaseInventory(@PathVariable UUID orderId) {
        return productCatalogUseCase.releaseInventory(orderId);
    }
}
