package com.ecommerce.order.client;

import com.ecommerce.common.web.ApiResponse;
import com.ecommerce.order.client.dto.ProductValidationRequest;
import com.ecommerce.order.client.dto.ProductValidationResponse;
import com.ecommerce.order.config.ProductServiceProperties;
import com.ecommerce.order.dto.CreateOrderItemRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProductCatalogClient {

    private final RestClient restClient;
    private final ProductServiceProperties properties;

    public ProductCatalogClient(
            RestClient.Builder builder,
            ProductServiceProperties properties
    ) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
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

        ApiResponse<ProductValidationResponse> response = restClient.post()
                .uri(properties.validationPath())
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalArgumentException(
                    response != null ? response.message() : "Product validation service returned no data."
            );
        }

        return response.data();
    }
}
