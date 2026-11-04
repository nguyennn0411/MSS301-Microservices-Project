package com.ecommerce.order.client;

import com.ecommerce.order.client.feign.ProductGatewayFeignClient;
import com.ecommerce.order.dto.ProductReservationRequest;
import com.ecommerce.order.dto.ProductReservationResponse;
import com.ecommerce.order.exception.OrderIntegrationException;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductGatewayClient {
    private final ProductGatewayFeignClient feignClient;

    public ProductGatewayClient(ProductGatewayFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    public ProductReservationResponse reserve(ProductReservationRequest request) {
        try {
            return feignClient.reserve(request);
        } catch (FeignException exception) {
            throw new OrderIntegrationException("Product validation or inventory reservation failed", exception);
        }
    }

    public void release(UUID orderId) {
        try {
            feignClient.release(orderId);
        } catch (FeignException exception) {
            throw new OrderIntegrationException("Could not release inventory for order " + orderId, exception);
        }
    }
}
