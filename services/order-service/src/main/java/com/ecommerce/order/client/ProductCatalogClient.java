package com.ecommerce.order.client;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.client.feign.ProductCatalogFeignClient;
import com.ecommerce.order.client.dto.ProductValidationRequest;
import com.ecommerce.order.client.dto.ProductValidationResponse;
import com.ecommerce.order.shop.dto.CreateOrderItemRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCatalogClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;

    public ProductCatalogClient(ProductCatalogFeignClient productCatalogFeignClient) {
        this.productCatalogFeignClient = productCatalogFeignClient;
    }

    public ProductValidationResponse validate(List<CreateOrderItemRequest> items) {
        ProductValidationRequest request = new ProductValidationRequest(
                items.stream()
                        .map(item -> new ProductValidationRequest.Item(
                                item.productId(),
                                item.quantity(), item.variantId()
                        ))
                        .toList()
        );

        ApiResponse<ProductValidationResponse> response = productCatalogFeignClient.validate(request);

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalArgumentException(
                    response != null ? response.message() : "Product validation service returned no data."
            );
        }

        return response.data();
    }
}
