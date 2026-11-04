package com.ecommerce.order.client.feign;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.client.dto.ProductValidationRequest;
import com.ecommerce.order.client.dto.ProductValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-catalog-service", url = "${product-service.base-url}")
public interface ProductCatalogFeignClient {

    @PostMapping("/api/v1/products/validation")
    ApiResponse<ProductValidationResponse> validate(@RequestBody ProductValidationRequest request);
}
